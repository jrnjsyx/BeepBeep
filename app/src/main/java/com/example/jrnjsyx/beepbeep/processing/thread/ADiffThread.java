package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.processing.Algorithm;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import org.ejml.data.DMatrixRMaj;
import org.ejml.equation.Equation;
import org.ejml.equation.Sequence;


public class ADiffThread extends Thread{
    private DecodeThread decodeThread;
    private Handler handler;
    private boolean isRunning = true;
    private int step;
    private DMatrixRMaj F = new DMatrixRMaj(2,2);
    private DMatrixRMaj x = new DMatrixRMaj(2,1);
    private DMatrixRMaj z = new DMatrixRMaj(2,1);
    private DMatrixRMaj P = new DMatrixRMaj(2,2);
    private DMatrixRMaj Q;
    private DMatrixRMaj R;
    private DMatrixRMaj K = new DMatrixRMaj(2,2);
    Sequence predictX;
    Sequence predictP;
    Sequence updateK;
    Sequence updateX;
    Sequence updateP;

    Equation eq = new Equation();

    private float distance;
    private float speed;
    //A 发出低频chirp信号
    public ADiffThread(DecodeThread decodeThread, Handler handler,int step){
        this.decodeThread = decodeThread;
        this.handler = handler;
        this.step = step;
        F.set(0,0,1);
        F.set(1,1,1);
        F.set(0,1,FlagVar.chirpIntervalTime);
        x.set(0,0,100);
        x.set(1,0,2);
        P.set(0,0,10);
        P.set(1,1,10);
        Q = FlagVar.Q;
        R = FlagVar.R;

        eq.alias(x,"x",F,"F",z,"z",P,"P",Q,"Q",R,"R",K,"K");
        predictX = eq.compile("x = F*x");
        predictP = eq.compile("P = F*P*F' + Q");
        updateK = eq.compile("K = P*inv( P + R )");
        updateX = eq.compile("x = x + K*(z-x)");
        updateP = eq.compile("P = P-K*P");



    }

    @Override
    public void run() {

        Common.println("aDiff start.");
        try{
            while (isRunning){
                if(decodeThread.dataOk) {
                    decodeThread.dataOk = false;
                    int unprocessedDistanceCnt = decodeThread.highChirpPosition-decodeThread.lowChirpPosition-(decodeThread.remoteHighChirpPosition-decodeThread.remoteLowChirpPosition);
                    int distanceCnt = Algorithm.moveIntoRange(unprocessedDistanceCnt,0,step);
                    Common.println("distanceCnt:"+distanceCnt);
                    distance = FlagVar.cSample * distanceCnt;
                    speed = decodeThread.speed;

                    if(FlagVar.currentRangingMode != FlagVar.BEEP_BEEP_MODE) {
                        computeUsingKalman();
                    }


                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("oldSpeed",decodeThread.speed);
                    bundle.putInt("oldDistanceCnt",distanceCnt);
                    bundle.putFloat("speed",speed);
                    bundle.putFloat("distance",distance);

                    msg.what = FlagVar.DISTANCE_TEXT;
                    msg.setData(bundle);
                    handler.sendMessage(msg);

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void stopRunning(){
        isRunning = false;
    }

    private void computeUsingKalman(){
        z.set(0,0,distance);
        z.set(1,0,speed);
        Q = FlagVar.Q;
        R = FlagVar.R;
        eq.alias(Q,"Q",R,"R");
        predictX.perform();
        predictP.perform();
        updateK.perform();
        updateX.perform();
        updateP.perform();
        distance = (float) x.get(0,0);
        speed = (float)x.get(1,0);


    }


}
