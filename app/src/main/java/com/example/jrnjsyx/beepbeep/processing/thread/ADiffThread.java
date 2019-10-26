package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.processing.Algorithm;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.util.List;


public class ADiffThread extends Thread{
    private DecodeThread decodeThread;
    private Handler handler;
    private boolean isRunning = true;
    private int step;
    //A 发出低频chirp信号
    public ADiffThread(DecodeThread decodeThread, Handler handler,int step){
        this.decodeThread = decodeThread;
        this.handler = handler;
        this.step = step;
    }

    @Override
    public void run() {

        Common.println("aDiff start.");
        try{
            while (isRunning){
                if(decodeThread.dataOk) {
                    decodeThread.dataOk = false;
//                    int basePosNow = decodeThread.basePos;
//                    int remoteBasePosNow = decodeThread.remoteBasePos;
//                    int aStart = computeOnce(decodeThread.lowChirpPositions,basePosNow);
//                    int aEnd = computeOnce(decodeThread.highChirpPositions,basePosNow);
//                    int bStart = computeOnce(decodeThread.remoteLowChirpPositions,remoteBasePosNow);
//                    int bEnd = computeOnce(decodeThread.remoteHighChirpPositions,remoteBasePosNow);
//                    Common.println("aStart:"+aStart+" aEnd:"+aEnd+" bStart:"+bStart+" bEnd:"+bEnd);
                    int unprocessedDistanceCnt = decodeThread.highChirpPosition-decodeThread.lowChirpPosition-(decodeThread.remoteHighChirpPosition-decodeThread.remoteLowChirpPosition);
                    int distanceCnt = Algorithm.moveIntoRange(unprocessedDistanceCnt,0,step);
                    Common.println("distanceCnt:"+distanceCnt);
                    Message msg = new Message();
                    msg.what = FlagVar.DISTANCE_TEXT;
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

        if(decodeThread.lowChirpPositions.size() >= 1 &&
                decodeThread.remoteLowChirpPositions.size() >= 1 &&
                decodeThread.highChirpPositions.size() >= 1 &&
                decodeThread.remoteHighChirpPositions.size() >= 1){
            Common.println("aDiff capacity ok.  "+decodeThread.lowChirpPositions.size()+" "
                    +decodeThread.highChirpPositions.size()+" "
                    +decodeThread.remoteLowChirpPositions.size()+" "
                    +decodeThread.remoteHighChirpPositions.size());
            return true;
        }
        else {
            return false;
        }
    }

    private int computeOnce(List<Integer> data,int base){
        int res;
        synchronized (data) {
            res = Algorithm.findNearestPosOnBase(data, base);
        }
        return res;
    }


}
