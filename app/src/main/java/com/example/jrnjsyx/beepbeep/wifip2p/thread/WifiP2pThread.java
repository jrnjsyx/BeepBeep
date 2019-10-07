package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.wifip2p.NetworkMsgListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WifiP2pThread extends Thread {

    public Map<String,NetworkMsgListener> listeners = new HashMap<String,NetworkMsgListener>();
    public List<String> listenersToBeRemoved = new LinkedList<String>();

    public void addListener(String key,NetworkMsgListener listener){
        synchronized (listener) {
            listeners.put(key, listener);
        }
    }

    public void removeListener(String key){
        synchronized (listeners) {
            listenersToBeRemoved.add(key);
        }

    }

    public void setStr(String str){
        Common.println("wifip2p thread setStr");
    };

    public void stopRunning(){
        Common.println("wifip2p thread stop running");
    };

    protected class WriteInfo{
        public boolean hasData = false;
        public String str;
    }
}
