package com.example.jrnjsyx.beepbeep.processing.thread;

import com.example.jrnjsyx.beepbeep.processing.DecodeMethod;
import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.utils.Common;

import java.util.LinkedList;
import java.util.List;

public class DecodeThreadTemplate extends Decoder implements Runnable,DecodeMethod{
    protected boolean isRunning = true;
    public List<short[]> samplesList = new LinkedList<short[]>();
    @Override
    public void run() {
        Common.println("DecodeThreadTemplate run().");
    }


    @Override
    public void stopRunning() {
        isRunning = false;
        onThreadStop();
    }

    protected void onThreadStop(){
        Common.println("DecodeThreadTemplate onThreadStop().");
    }

    @Override
    public void fillSamples(short[] data) {
        synchronized (samplesList) {
            samplesList.add(data);
        }
    }
}
