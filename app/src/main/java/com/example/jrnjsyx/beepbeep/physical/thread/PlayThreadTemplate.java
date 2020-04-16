package com.example.jrnjsyx.beepbeep.physical.thread;

import com.example.jrnjsyx.beepbeep.physical.PlayMethod;

public class PlayThreadTemplate extends Thread implements PlayMethod{

    @Override
    public void adjustSample(int cnt) {

    }

    protected boolean isRunning = true;
    @Override
    public void stopRunning() {
        isRunning = false;
    }
}
