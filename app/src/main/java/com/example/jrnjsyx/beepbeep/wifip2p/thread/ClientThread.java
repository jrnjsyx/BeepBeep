package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientThread extends Thread{

    private Handler mHandler;

    private WifiP2pInfo wifiP2pInfo;

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

    public ClientThread(Handler mHandler, WifiP2pInfo wifiP2pInfo){
        this.mHandler = mHandler;
        this.wifiP2pInfo = wifiP2pInfo;
        writeInfo = new WriteInfo();
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    }

    public void setStr(String str) {
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
            socket.connect((new InetSocketAddress(wifiP2pInfo.groupOwnerAddress.getHostAddress(), FlagVar.PORT)), 10000);
            outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream);
            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String info = null;
            long cnt = 0;
            while (isRunning) {
                if (bufferedReader.ready() && (info = bufferedReader.readLine()) != null) {
                    System.out.println(info);
                    Message msg = new Message();
                    msg.obj = info;
                    mHandler.sendMessage(msg);
                }
                synchronized (writeInfo) {
                    if (writeInfo.hasData == true) {
                        writeInfo.hasData = false;
                        printWriter.println(writeInfo.str);
                        printWriter.flush();
                        System.out.println(writeInfo.str);
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

    public void stopRunning(){
        isRunning = false;

    }

    private class WriteInfo{
        public boolean hasData = false;
        public String str;
    }
}
