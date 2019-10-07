package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import android.app.Application;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.wifip2p.NetworkMsgListener;

public class ConnetJudgeThread extends Thread{

    private boolean isHostConnectable;
    private String host;
    private int port;
    private NetworkMsgListener listener;
    public ConnetJudgeThread(String host, int port,NetworkMsgListener listener){
        this.host = host;
        this.port = port;
        this.listener = listener;
        isHostConnectable = false;
    }

    public void run(){
        while (!isHostConnectable){
            isHostConnectable = Common.isHostConnectable(host,port);
            try {
                this.sleep(100);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        listener.handleMsg(null);
    }

    public boolean isHostConnectable(){

        return isHostConnectable;
    }

}
