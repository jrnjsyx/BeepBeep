package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;
import com.example.jrnjsyx.beepbeep.wifip2p.NetworkMsgListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends WifiP2pThread {

    private boolean isRunning = true;
    private boolean isAccepting = true;

    private Handler mHandler;
    private WriteInfo writeInfo;
    private boolean isPausing = false;

    private OutputStream outputStream;
    private PrintWriter printWriter;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private ServerSocket serverSocket;
    private Socket socket;
    public boolean connected = false;


    private long writeReadInterval = 20000;


    public ServerThread(Handler mHandler){
        this.mHandler = mHandler;
        writeInfo = new WriteInfo();
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
    @Override
    public void setStr(String str) {
        Common.println("server thread setStr");
        synchronized (writeInfo){
            writeInfo.hasData = true;
            writeInfo.str = str;
        }
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

                while (isPausing) {
                    sleep(0, (int) writeReadInterval);
                }
                if (bufferedReader.ready() && (info = bufferedReader.readLine()) != null) {
                    Common.println("server:"+info);
                    for(NetworkMsgListener listener:listeners.values()){
                        listener.handleMsg(info);
                    }
                    Message msg = new Message();
                    msg.obj = info;
                    mHandler.sendMessage(msg);
                }

                if (writeInfo.hasData == true) {
                    synchronized (writeInfo) {
                        writeInfo.hasData = false;
                        printWriter.write(writeInfo.str + "\n");
                        printWriter.flush();
                        Common.println(writeInfo.str);
                    }
                }
                sleep(0, (int) writeReadInterval);
            }
        }catch (Exception e){
            isAccepting = false;
            isRunning = false;
            e.printStackTrace();
        }finally {
            Message msg = new Message();
            msg.obj = FlagVar.connectionThreadEndStr;
            mHandler.sendMessage(msg);
            close();
        }

    }

    @Override
    public void stopRunning(){
        isRunning = false;
        connected = false;
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



    public void pause(){
        isPausing = true;
    }
    public void stopPausing(){
        isPausing = false;
    }
}
