package com.example.jrnjsyx.beepbeep.physical.thread;

import com.example.jrnjsyx.beepbeep.physical.RecordMethod;

public class RecordThreadTemplate extends Thread implements RecordMethod {

    protected boolean isRunning = true;
    @Override
    public void stopRunning() {
        isRunning = false;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }
}
