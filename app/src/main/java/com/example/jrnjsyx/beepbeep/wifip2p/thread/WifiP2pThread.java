package com.example.jrnjsyx.beepbeep.wifip2p.thread;

import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.wifip2p.NetworkMsgListener;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WifiP2pThread extends Thread {

    public Map<String,NetworkMsgListener> listeners = new HashMap<String,NetworkMsgListener>();
    public List<String> listenersToBeRemoved = new LinkedList<String>();
    protected boolean isRunning = true;
    private List<String> infosToWrite = new LinkedList<String>();



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

    public void setMessage(String str){
        synchronized (infosToWrite){
            infosToWrite.add(str);
        }
//        Common.println("wifip2p thread setMessage");
    };

    public void stopRunning(){
        isRunning = false;
    };


    protected void listenersHandleMsg(String info){
        synchronized (listeners) {
            for (NetworkMsgListener listener : listeners.values()) {
                listener.handleMsg(info);
            }
            synchronized (listenersToBeRemoved) {
                for (String str : listenersToBeRemoved) {
                    listeners.remove(str);
                }
                listenersToBeRemoved.clear();
            }
        }
    }

    protected void sendMessage(PrintWriter printWriter){
        synchronized (infosToWrite) {
            if(infosToWrite.size() != 0) {
                for (String str : infosToWrite) {
                    printWriter.write(str + "\n");
                    printWriter.flush();
//                    Common.println(str);
                }
                infosToWrite.clear();
            }
        }
    }

    protected void doPerTurn(){
        synchronized (listeners){
            for (NetworkMsgListener listener : listeners.values()) {
                listener.doPerTurn();
            }
        }
    }
}
