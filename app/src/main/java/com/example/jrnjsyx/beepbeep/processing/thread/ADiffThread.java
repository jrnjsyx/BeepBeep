package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.activity.MainActivity;
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
    private int distanceAbnormalCnt = 0;
    private int speedAbnormalCnt = 0;
    private float oldDistance;
    private float oldSpeed;
    private float unhandledDistance;
    private float unhandledSpeed;
    private DMatrixRMaj Q0 = new DMatrixRMaj(2,2);
    private DMatrixRMaj R0 = new DMatrixRMaj(2,2);
    double[] curveMetric = Algorithm.threePointDeterminationCurve(new double[]{60,20,0},new double[]{1,2,10});


    Equation eq = new Equation();

    private float distance;
    private float speed;
    //A 发出低频chirp信号
    public ADiffThread(DecodeThread decodeThread, Handler handler,int step){
        this.decodeThread = decodeThread;
        this.handler = handler;
        this.step = step;

        Q0.set(0,0,10);
        Q0.set(1,1,10);
        R0.set(0,0,20);
        R0.set(0,1,-10);
        R0.set(1,0,-10);
        R0.set(1,1,40);

        x.set(0,0,100);
        x.set(1,0,2);
        P.set(0,0,100);
        P.set(1,1,100);
        Q = new DMatrixRMaj(FlagVar.Q);
        R = new DMatrixRMaj(FlagVar.R);

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
                    unhandledDistance = FlagVar.cSample * distanceCnt;
                    unhandledSpeed = decodeThread.speed;
                    if(Math.abs(oldDistance+FlagVar.chirpIntervalTime*oldSpeed-unhandledDistance) > FlagVar.distanceThreshold && distanceAbnormalCnt < 4){
                        distanceAbnormalCnt++;
                    }
                    else{
                        distanceAbnormalCnt = 0;
                        distance = unhandledDistance;
                    }
                    if(Math.abs(oldSpeed-unhandledSpeed) > FlagVar.speedThreshold && speedAbnormalCnt < 4){
                        speedAbnormalCnt++;
                    }
                    else{
                        speedAbnormalCnt = 0;
                        speed = unhandledSpeed;
                    }

                    if(FlagVar.currentRangingMode != FlagVar.BEEP_BEEP_MODE) {
                        computeUsingKalman();
                    }

                    oldDistance = distance;
                    oldSpeed = speed;
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("unhandledSpeed",unhandledSpeed);
                    bundle.putFloat("unhandledDistance",unhandledDistance);
                    bundle.putInt("unhandledDistanceCnt",distanceCnt);
                    bundle.putFloat("speed",this.speed);
                    bundle.putFloat("distance",this.distance);

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
        setF(1);
        if(FlagVar.currentSelfAdaptionMode == FlagVar.FREE_MODE) {
            Q = new DMatrixRMaj(FlagVar.Q);
            R = new DMatrixRMaj(FlagVar.R);
            System.out.println(Q);
            System.out.println(R);
        }
        else if(FlagVar.currentSelfAdaptionMode == FlagVar.SELF_ADAPTION_MODE){
            Q = new DMatrixRMaj(Q0);
            double ratio = curveMetric[2]/(Math.abs(oldSpeed)-curveMetric[0])+curveMetric[1];
            if(ratio < 0.5){
                ratio = 0.5;
            }
            double r11 = R0.get(0,0)*ratio;
            double r12 = 0-Math.sqrt(r11*R0.get(1,1)/8);
            R.set(0,0,r11);
            R.set(0,1,r12);
            R.set(1,0,r12);
            R.set(1,1,R0.get(1,1));

        }
        eq.alias(Q,"Q",R,"R");
        predictX.perform();
        predictP.perform();
        updateK.perform();
        updateX.perform();
        updateP.perform();
        distance = (float) x.get(0,0);
        speed = (float)x.get(1,0);


    }

    private void setF(int cnt){
        F.set(0,0,1);
        F.set(1,1,1);
        F.set(0,1,cnt*FlagVar.chirpIntervalTime);

    }


}
