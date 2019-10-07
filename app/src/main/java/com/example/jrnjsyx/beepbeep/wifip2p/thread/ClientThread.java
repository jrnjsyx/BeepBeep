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
import java.net.Socket;

public class ClientThread extends WifiP2pThread{

    private Handler mHandler;

    private String host;

    private boolean isRunning = true;
    private WriteInfo writeInfo;
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
        writeInfo = new WriteInfo();
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    }
    @Override
    public void setStr(String str) {
        Common.println("client thread setStr");
        synchronized (writeInfo){
            writeInfo.hasData = true;
            writeInfo.str = str;
        }
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

                if (bufferedReader.ready() && (info = bufferedReader.readLine()) != null) {
                    Common.println("client:"+info);
                    synchronized (listeners) {
                        for (NetworkMsgListener listener : listeners.values()) {
                            listener.handleMsg(info);
                        }
                        for(String str:listenersToBeRemoved){
                            listeners.remove(str);
                        }
                    }
                    Message msg = new Message();
                    msg.obj = info;
                    mHandler.sendMessage(msg);
                }

                if (writeInfo.hasData == true) {
                    synchronized (writeInfo) {
                        writeInfo.hasData = false;
                        printWriter.write(writeInfo.str+"\n");
                        printWriter.flush();
                        Common.println(writeInfo.str);
                    }
                }
                cnt++;
                if(cnt == times){
                    cnt = 0;
                    socket.sendUrgentData(0xFF);
                }
                sleep(0,(int)writeReadInterval);
            }
        }catch (Exception e){
            isRunning = false;
            e.printStackTrace();
        }
        finally {
            Message msg = new Message();
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
        isRunning = false;
        Common.println("client thread stop running");
    }


}
