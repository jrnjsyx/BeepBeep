package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends WifiP2pThread {

    private boolean isAccepting = true;

    private Handler mHandler;
    private boolean isPausing = false;

    private OutputStream outputStream;
    private PrintWriter printWriter;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private ServerSocket serverSocket;
    private Socket socket;


    private long writeReadInterval = 20000;


    public ServerThread(Handler mHandler){
        this.mHandler = mHandler;
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        try{
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            if(!serverSocket.isBound()) {
                serverSocket.bind(new InetSocketAddress(FlagVar.PORT));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Common.println("server create");
    }

    public void run(){
        super.run();
        try {

            socket = serverSocket.accept();
            Common.println("--------accept 1--------");
            this.sleep(100);
            socket = serverSocket.accept();
            Common.println("--------accept 2--------");
            inputStream = socket.getInputStream();
            inputStreamReader =new InputStreamReader(inputStream);
            bufferedReader  = new BufferedReader(inputStreamReader);
            outputStream  = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream);
            String info = null;
            while (isRunning ) {
                doPerTurn();
                if (bufferedReader.ready() && (info = bufferedReader.readLine()) != null) {
//                    Common.println("server:"+info);
                    listenersHandleMsg(info);
                    Message msg = new Message();
                    msg.what = FlagVar.NETWORK_TEXT;
                    msg.obj = info;
                    mHandler.sendMessage(msg);
                }

                sendMessage(printWriter);
//                sleep(0, (int) writeReadInterval);
            }
        }catch (Exception e){
            isAccepting = false;
            isRunning = false;
            e.printStackTrace();
        }finally {
            Message msg = new Message();
            msg.what = FlagVar.NETWORK_TEXT;
            msg.obj = FlagVar.connectionThreadEndStr;
            mHandler.sendMessage(msg);
            close();
        }

    }

    @Override
    public void stopRunning(){
        super.stopRunning();
        Common.println("server thread stop running");

    }

    private void close(){
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (inputStreamReader != null) {
            try {
                inputStreamReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (printWriter != null) {
            try {
                printWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
