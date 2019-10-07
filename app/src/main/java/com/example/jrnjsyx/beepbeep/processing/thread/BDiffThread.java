package com.example.jrnjsyx.beepbeep.processing.thread;


import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.physical.AudioRecorder;
import com.example.jrnjsyx.beepbeep.physical.thread.PlayThread;
import com.example.jrnjsyx.beepbeep.processing.thread.DecodeThread;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

public class BDiffThread implements Runnable {

    private DecodeThread decodeThread;
    private AudioRecorder audioRecorder;
    private Handler handler;
    private PlayThread playThread;
    public BDiffThread(DecodeThread decodeThread, AudioRecorder audioRecorder, PlayThread playThread, Handler handler){
        this.decodeThread = decodeThread;
        this.audioRecorder = audioRecorder;
        this.playThread = playThread;
        this.handler = handler;
    }
    @Override
    public void run() {
        decodeThread.decodeStart();
        audioRecorder.startRecord();
        while (decodeThread.lowChirpPos.size() < 1){
            try {
                Thread.sleep(1);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        while (decodeThread.lowChirpPos.size() < 2){
            try {
                Thread.sleep(1);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        int sampleDiff = 0;
        synchronized (decodeThread.lowChirpPos){
            sampleDiff = decodeThread.lowChirpPos.get(1)- decodeThread.lowChirpPos.get(0);
        }
        Message msg = new Message();
        msg.arg1 = sampleDiff;
        msg.arg2 = FlagVar.A_BUTTON_ENABLE;
        handler.sendMessage(msg);
        audioRecorder.finishRecord();
    }
}
