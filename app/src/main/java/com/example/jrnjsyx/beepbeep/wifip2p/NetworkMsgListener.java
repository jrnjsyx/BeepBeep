package com.example.jrnjsyx.beepbeep.wifip2p;

public abstract class NetworkMsgListener {
    public boolean needRemoved = false;
    public abstract void handleMsg(String msg);
    public void doPerTurn(){};
}
