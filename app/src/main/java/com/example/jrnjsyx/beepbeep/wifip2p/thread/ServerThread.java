package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

    private boolean isRunning = true;

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

    private long writeReadInterval = 20000;


    public ServerThread(Handler mHandler){
        this.mHandler = mHandler;
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
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(FlagVar.PORT));
            socket = serverSocket.accept();

            inputStream = socket.getInputStream();
            inputStreamReader =new InputStreamReader(inputStream);
            bufferedReader  = new BufferedReader(inputStreamReader);
            outputStream  = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream);
            String info = null;
            while (isRunning ) {

                while (isPausing){
                    sleep(0,(int)writeReadInterval);
                }
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
                sleep(0,(int)writeReadInterval);
            }
        }catch (Exception e){
            isRunning = false;
            e.printStackTrace();
        }finally {
            close();
        }

    }

    public void stopRunning(){
        isRunning = false;

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

    private class WriteInfo{
        public boolean hasData = false;
        public String str;
    }

    public void pause(){
        isPausing = true;
    }
    public void stopPausing(){
        isPausing = false;
    }
}
