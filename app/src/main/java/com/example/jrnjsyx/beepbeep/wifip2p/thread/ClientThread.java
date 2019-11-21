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
import java.net.Socket;

public class ClientThread extends WifiP2pThread{

    private Handler mHandler;

    private String host;

    private OutputStream outputStream;
    private PrintWriter printWriter;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private Socket socket;
    private long urgentDataInterval = 2000;//milliseconds
    private long writeReadInterval = 20000;//nanoseconds


    public ClientThread(Handler mHandler, String host){
        Common.println("client create");
        this.mHandler = mHandler;
        this.host = host;
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    }

    public void run(){
        super.run();
        try {
            long times = urgentDataInterval*10000000/writeReadInterval;
            socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, FlagVar.PORT)), 10000);
            outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream);
            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String info = null;
            long cnt = 0;
            while (isRunning) {
                doPerTurn();
                if (bufferedReader.ready() && (info = bufferedReader.readLine()) != null) {
//                    Common.println("client:"+info);
                    listenersHandleMsg(info);

                    Message msg = new Message();
                    msg.what = FlagVar.NETWORK_TEXT;
                    msg.obj = info;
                    mHandler.sendMessage(msg);
                }

                sendMessage(printWriter);
//                sleep(0,(int)writeReadInterval);
            }
        }catch (Exception e){
            isRunning = false;
            e.printStackTrace();
        }
        finally {
            Message msg = new Message();
            msg.what = FlagVar.NETWORK_TEXT;
            msg.obj = FlagVar.connectionThreadEndStr;
            mHandler.sendMessage(msg);
            close();
        }
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
    }

    @Override
    public void stopRunning(){
        super.stopRunning();
        Common.println("client thread stop running");
    }


}
