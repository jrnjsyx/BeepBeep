package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import com.example.jrnjsyx.beepbeep.utils.Common;

public class WifiP2pThread extends Thread {

    public void setStr(String str){
        Common.println("wifip2p thread setStr");
    };

    public void stopRunning(){
        Common.println("wifip2p thread stop running");
    };
}
