package com.example.jrnjsyx.beepbeep.processing;

import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.physical.AudioRecorder;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;


public class ADiffThread implements Runnable{
    private DecodThread decodThread;
    private Handler handler;
    private AudioRecorder audioRecorder;
    public ADiffThread(DecodThread decodThread, AudioRecorder audioRecorder, Handler handler){
        this.decodThread = decodThread;
        this.handler = handler;
        this.audioRecorder = audioRecorder;
    }

    @Override
    public void run() {
        while (decodThread.sampleCnts.size() < 2) {
        }
        int sampleDiff = 0;
        synchronized (decodThread.sampleCnts) {
            sampleDiff = decodThread.sampleCnts.get(1) - decodThread.sampleCnts.get(0);
        }
        Message msg = new Message();
        msg.arg1 = sampleDiff;
        msg.arg2 = FlagVar.B_BUTTON_ENABLE;
        handler.sendMessage(msg);
        audioRecorder.finishRecord();
    }
}
