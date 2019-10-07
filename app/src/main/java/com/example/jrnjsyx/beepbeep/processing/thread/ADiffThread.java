package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.physical.AudioRecorder;
import com.example.jrnjsyx.beepbeep.processing.thread.DecodeThread;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;


public class ADiffThread implements Runnable{
    private DecodeThread decodeThread;
    private Handler handler;
    private AudioRecorder audioRecorder;
    public ADiffThread(DecodeThread decodeThread, AudioRecorder audioRecorder, Handler handler){
        this.decodeThread = decodeThread;
        this.handler = handler;
        this.audioRecorder = audioRecorder;
    }

    @Override
    public void run() {
        while (decodeThread.lowChirpPos.size() < 2) {
            try {
                Thread.sleep(1);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        int sampleDiff = 0;
        synchronized (decodeThread.lowChirpPos) {
            sampleDiff = decodeThread.lowChirpPos.get(1) - decodeThread.lowChirpPos.get(0);
        }
        Message msg = new Message();
        msg.arg1 = sampleDiff;
        msg.arg2 = FlagVar.B_BUTTON_ENABLE;
        handler.sendMessage(msg);
        audioRecorder.finishRecord();
    }
}
