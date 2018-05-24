package processing;


import android.os.Handler;
import android.os.Message;

import physical.AudioRecorder;
import physical.PlayThread;
import utils.FlagVar;

public class BDiffThread implements Runnable {

    private DecodThread decodThread;
    private AudioRecorder audioRecorder;
    private Handler handler;
    private PlayThread playThread;
    public BDiffThread(DecodThread decodThread, AudioRecorder audioRecorder, PlayThread playThread, Handler handler){
        this.decodThread = decodThread;
        this.audioRecorder = audioRecorder;
        this.playThread = playThread;
        this.handler = handler;
    }
    @Override
    public void run() {
        decodThread.decodeStart();
        audioRecorder.startRecord();
        while (decodThread.sampleCnts.size() < 1){
        }
        playThread.omitChirpSignal();
        while (decodThread.sampleCnts.size() < 2){
        }
        int sampleDiff = 0;
        synchronized (decodThread.sampleCnts){
            sampleDiff = decodThread.sampleCnts.get(1)-decodThread.sampleCnts.get(0);
        }
        Message msg = new Message();
        msg.arg1 = sampleDiff;
        msg.arg2 = FlagVar.A_BUTTON_ENABLE;
        handler.sendMessage(msg);
        audioRecorder.finishRecord();
    }
}
