package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import com.example.jrnjsyx.beepbeep.utils.Common;

public class ConnetJudgeThread extends Thread{

    private boolean isHostConnectable;
    private String host;
    private int port;
    public ConnetJudgeThread(String host, int port){
        this.host = host;
        this.port = port;
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
    }

    public boolean isHostConnectable(){

        return isHostConnectable;
    }

}
