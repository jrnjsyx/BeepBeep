package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.physical.thread.PlayThread;
import com.example.jrnjsyx.beepbeep.processing.Algorithm;
import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.processing.IndexMaxVarInfo;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;
import com.example.jrnjsyx.beepbeep.utils.JniUtils;
import com.example.jrnjsyx.beepbeep.wifip2p.NetworkMsgListener;
import com.example.jrnjsyx.beepbeep.wifip2p.thread.WifiP2pThread;

import java.util.LinkedList;
import java.util.List;


public class DecodeThread extends Decoder implements Runnable {

    private static final String TAG = "DecodeThread";
    private boolean isThreadRunning = true;
    private Integer mLoopCounter = 0;
    private Handler mHandler;
    public List<short[]> samplesList;
    public List<Integer> lowChirpPositions;
    public List<Integer> highChirpPositions;
    public List<Integer> remoteLowChirpPositions;
    public List<Integer> remoteHighChirpPositions;
    private WifiP2pThread currentP2pThread;
    public Integer basePos = 0;
    public Integer remoteBasePos = 0;
    public int lowChirpPosition;
    public int highChirpPosition;
    public int remoteLowChirpPosition;
    public int remoteHighChirpPosition;
    private int dataSavedSize = 409600;
    private PlayThread playThread;
    private boolean isLow = false;
    private boolean isAdjusted = false;
    private int diff = 0;
    private int remoteDiff = 0;
    public Boolean skip = false;
    public boolean dataOk = false;

    public short[] savedData = new short[dataSavedSize];



    private IndexMaxVarInfo mIndexMaxVarInfo;

    public DecodeThread(Handler mHandler, int size, WifiP2pThread currentP2pThread,PlayThread playThread,boolean isLow){
        mIndexMaxVarInfo = new IndexMaxVarInfo();
        samplesList = new LinkedList<short[]>();
        lowChirpPositions = new LinkedList<Integer>();
        highChirpPositions = new LinkedList<Integer>();
        remoteLowChirpPositions = new LinkedList<Integer>();
        remoteHighChirpPositions = new LinkedList<Integer>();
        this.mHandler = mHandler;
        this.playThread = playThread;
        this.isLow = isLow;
        initialize(size);
        this.currentP2pThread = currentP2pThread;
        currentP2pThread.addListener(FlagVar.recordStr,networkMsgListener);
    }

    /**
     * copy the samples from the audio buffer
     * @param s - input coming from the audio buffer
     */
    public void fillSamples(short[] s){

        synchronized (samplesList) {
            samplesList.add(s);
        }

    }

    @Override
    public void run() {
        try {
            while (isThreadRunning) {
                if (samplesList.size() >= 2) {
                    if(skip){
                        skip = false;
                        synchronized (samplesList) {
                            samplesList.remove(0);
                        }
                        continue;
                    }
                    short[] buffer = new short[processBufferSize+ FlagVar.startBeforeMaxCorr+ FlagVar.lChirp];
                    synchronized (samplesList) {
                        System.arraycopy(samplesList.get(0),0,buffer,0,processBufferSize);
                        System.arraycopy(samplesList.get(1),0,buffer,0, FlagVar.lChirp +FlagVar.startBeforeMaxCorr);

                        samplesList.remove(0);

                    }
                    if(mLoopCounter<dataSavedSize/processBufferSize) {
                        System.arraycopy(samplesList.get(0), 0, savedData, mLoopCounter * FlagVar.recordBufferSize, processBufferSize);
                    }

                    synchronized (mLoopCounter) {
                        mLoopCounter++;
                        basePos = mLoopCounter*processBufferSize;
                        String str = FlagVar.basePosStr +" "+basePos;
                        currentP2pThread.setMessage(str);
                    }
                    float[] fft = JniUtils.fft(normalization(buffer), chirpCorrLen);

                    IndexMaxVarInfo infoLow = getIndexMaxVarInfoFromFDomain2(fft, lowChirpFFT);
                    IndexMaxVarInfo infoHigh = getIndexMaxVarInfoFromFDomain2(fft, highChirpFFT);


                    lowChirpPosition = processBufferSize * mLoopCounter + infoLow.index;
                    highChirpPosition = processBufferSize * mLoopCounter + infoHigh.index;
//                    constructDiffs();


                    if(infoLow.isReferenceSignalExist) {
                        synchronized (lowChirpPositions) {
                            if(lowChirpPositions.size() > 0) {
                                Common.println("low chirp pos:" + lowChirpPosition + "    lowdiff:" + (lowChirpPositions.get(lowChirpPositions.size() - 1) - lowChirpPosition));
                            }
                            lowChirpPositions.add(lowChirpPosition);
                            if(lowChirpPositions.size() > FlagVar.minPosDataCapacity){
                                lowChirpPositions.remove(0);
                            }
                        }
                        String msg = FlagVar.lowPosStr+" "+lowChirpPosition;
                        currentP2pThread.setMessage(msg);
                    }

                    if(infoHigh.isReferenceSignalExist) {
                        synchronized (highChirpPositions) {
                            if(highChirpPositions.size() > 0) {
                                Common.println("high chirp pos:" + highChirpPosition + "    highdiff:" + (highChirpPositions.get(highChirpPositions.size() - 1) - highChirpPosition));
                            }
                            highChirpPositions.add(highChirpPosition);
                            if(highChirpPositions.size() > FlagVar.minPosDataCapacity){
                                highChirpPositions.remove(0);
                            }
                        }
                        String msg = FlagVar.highPosStr+" "+highChirpPosition;
                        currentP2pThread.setMessage(msg);
                    }

                    isAdjusted = adJustSamples();
                    if(isLow && isAdjusted){
                        skip = true;
                        String msg = FlagVar.skipStr+" "+"true";
                        currentP2pThread.setMessage(msg);
                    }
                    sendDebugMsg();
                    dataOk = true;

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getPos(List<Integer> positions){
        int pos = 0;
        synchronized (positions){
            pos = Algorithm.getMedian(positions);
        }
        return pos;
    }

    private void clearPostisons(List<Integer> positions){
        synchronized (positions){
            positions.clear();
        }
    }

    //不确定是recordBufferSize还是playBufferSize
    private boolean adJustSamples(){
        if(!isLow){
            return false;
        }

        int lowPos = 0,highPos = 0,remoteLowPos = 0,remoteHighPos = 0;
        synchronized (lowChirpPositions){
            lowPos = Algorithm.getMedian(lowChirpPositions);
        }
        synchronized (highChirpPositions){
            highPos = Algorithm.getMedian(highChirpPositions);
        }
        synchronized (remoteLowChirpPositions){
            remoteLowPos = Algorithm.getMedian(remoteLowChirpPositions);
        }
        synchronized (remoteHighChirpPositions){
            remoteHighPos = Algorithm.getMedian(remoteHighChirpPositions);
        }

        diff = lowPos-highPos;
        remoteDiff = remoteLowPos-remoteHighPos;
        diff = Algorithm.moveIntoRange(diff,0-FlagVar.recordBufferSize/2,FlagVar.recordBufferSize);
        remoteDiff = Algorithm.moveIntoRange(remoteDiff,0-FlagVar.recordBufferSize/2,FlagVar.recordBufferSize);
        int offset = 0;
        if(remoteHighChirpPositions.size() >= 1 && lowChirpPositions.size() >= 1){
            int diffDiff = diff-remoteDiff;
            offset = (FlagVar.recordBufferSize+diffDiff)/2;
            Common.println("diff:"+diff +" remoteDiff:"+remoteDiff+" offset:"+offset);
            if((diff > 0-FlagVar.lChirp && diff <= FlagVar.lChirp) || (remoteDiff > 0-FlagVar.lChirp && remoteDiff <= FlagVar.lChirp)){
                clearPostisons(lowChirpPositions);
                clearPostisons(highChirpPositions);
                clearPostisons(remoteHighChirpPositions);
                clearPostisons(remoteLowChirpPositions);
                playThread.adjustSample(offset);
                Common.println("adjust.");
                return true;
            }

        }
        return false;


    }

    private void constructDiffs(){
        diff = lowChirpPosition-highChirpPosition;
        int remoteLowPos = 0;
        synchronized (remoteLowChirpPositions) {
            if(remoteLowChirpPositions.size() > 0) {
                remoteLowPos = remoteLowChirpPositions.get(remoteLowChirpPositions.size() - 1);
            }
        }
        int remoteHighPos = 0;
        synchronized (remoteHighChirpPositions) {
            if(remoteHighChirpPositions.size() > 0) {
                remoteHighPos = remoteHighChirpPositions.get(remoteHighChirpPositions.size() - 1);
            }
        }
        remoteDiff = remoteLowPos-remoteHighPos;



    }

    private void sendDebugMsg(){
        Message msg = new Message();
        msg.what = FlagVar.DEBUG_TEXT;
        msg.arg1 = diff;
        msg.arg2 = remoteDiff;
        msg.obj = isAdjusted;
        mHandler.sendMessage(msg);
    }

    public void decodeStart(){
        try {
            synchronized (lowChirpPositions){
                lowChirpPositions.clear();
            }
            synchronized (highChirpPositions){
                highChirpPositions.clear();
            }
            synchronized (mLoopCounter) {
                mLoopCounter = 0;
            }
            synchronized (samplesList) {
                samplesList.clear();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * shutdown the thread
     */
    public void stopRunning() {
        synchronized (this){
            isThreadRunning = false;
            //Thread.currentThread().join();
        }
    }

    private NetworkMsgListener networkMsgListener = new NetworkMsgListener() {
        @Override
        public void handleMsg(String msg) {
            String[] strs = msg.split(" ");
            if(strs.length == 2){
                if(strs[0].equals(FlagVar.lowPosStr)){
                    synchronized (remoteLowChirpPositions){
                        remoteLowChirpPositions.add(Integer.parseInt(strs[1]));
                        if(remoteLowChirpPositions.size() > FlagVar.minPosDataCapacity){
                            remoteLowChirpPositions.remove(0);
                        }
                        remoteLowChirpPosition = Integer.parseInt(strs[1]);
                    }
                }
                else if(strs[0].equals(FlagVar.highPosStr)){
                    synchronized (remoteHighChirpPositions){
                        remoteHighChirpPositions.add(Integer.parseInt(strs[1]));
                        if(remoteHighChirpPositions.size() > FlagVar.minPosDataCapacity){
                            remoteHighChirpPositions.remove(0);
                        }
                        remoteHighChirpPosition = Integer.parseInt(strs[1]);
                    }
                }
                else if(strs[0].equals(FlagVar.basePosStr)){
                    synchronized (remoteBasePos){
                        remoteBasePos = Integer.parseInt(strs[1]);
                    }
                }
                else if(strs[0].equals(FlagVar.skipStr)){
                    synchronized (skip){
                        skip = Boolean.parseBoolean(strs[1]);
                    }
                }
            }
        }
    };


}
