package com.example.jrnjsyx.beepbeep.wifip2p;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 作者：叶应是叶
 * 时间：2018/2/14 21:09
 * 描述：服务器端接收文件
 */
public class WifiServerService extends IntentService {

    private static final String TAG = "WifiServerService";


    private ServerSocket serverSocket;

    private InputStream inputStream;

    private ObjectInputStream objectInputStream;

    private FileOutputStream fileOutputStream;


    private static final int PORT = 4786;

    public class MyBinder extends Binder {
        public WifiServerService getService() {
            return WifiServerService.this;
        }
    }

    public WifiServerService() {
        super("WifiServerService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        clean();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clean();
    }


    private void clean() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (objectInputStream != null) {
            try {
                objectInputStream.close();
                objectInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
                fileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
