package com.example.jrnjsyx.beepbeep.processing.thread;


import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.physical.thread.PlayThread;
import com.example.jrnjsyx.beepbeep.processing.Algorithm;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.util.List;

public class BDiffThread extends Thread {

    private DecodeThread decodeThread;
    private Handler handler;
    private int step;
    private boolean isRunning = true;
    private int minCapacity = FlagVar.minPosDataCapacity;
    public BDiffThread(DecodeThread decodeThread, Handler handler,int step){
        this.decodeThread = decodeThread;
        this.handler = handler;
        this.step = step;
    }
    @Override
    public void run() {
        Common.println("bDiff start.");
        try{
            while (isRunning){
                if(isCapacityOk()) {
                    Common.println("bDiff capacity ok.  "+decodeThread.highChirpPositions.size()+" "
                            +decodeThread.lowChirpPositions.size()+" "
                            +decodeThread.remoteHighChirpPositions.size()+" "
                            +decodeThread.remoteLowChirpPositions.size());
                    int basePosNow = decodeThread.basePos;
                    int remoteBasePosNow = decodeThread.remoteBasePos;
                    int aStart = computeOnce(decodeThread.highChirpPositions,basePosNow);
                    int aEnd = computeOnce(decodeThread.lowChirpPositions,basePosNow);
                    int bStart = computeOnce(decodeThread.remoteHighChirpPositions,remoteBasePosNow);
                    int bEnd = computeOnce(decodeThread.remoteLowChirpPositions,remoteBasePosNow);
                    Common.println("aStart:"+aStart+" aEnd:"+aEnd+" bStart:"+bStart+" bEnd:"+bEnd);
                    int unprocessedDistanceCnt = aEnd-aStart-(bEnd-bStart);
                    int distanceCnt = Algorithm.moveIntoRange(unprocessedDistanceCnt,0,step);
                    Common.println("distanceCnt:"+distanceCnt);
                    Message msg = new Message();
                    msg.arg1 = FlagVar.DISTANCE_TEXT;
                    msg.arg2 = distanceCnt;
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

    private boolean isCapacityOk(){
        if(decodeThread.lowChirpPositions.size() >= minCapacity &&
                decodeThread.remoteLowChirpPositions.size() >= minCapacity &&
                decodeThread.highChirpPositions.size() >= minCapacity &&
                decodeThread.remoteHighChirpPositions.size() >= minCapacity){
            return true;
        }
        else {
            return false;
        }
    }


    private int computeOnce(List<Integer> data, int base){
        int res;
        synchronized (data) {
            res = Algorithm.findNearestPosOnBase(data,base);
            while (data.size() >= minCapacity) {
                data.remove(0);
            }
        }
        return res;
    }
}
