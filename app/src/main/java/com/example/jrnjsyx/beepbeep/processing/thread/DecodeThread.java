package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Handler;

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
    private boolean isOmittingLow;
    private WifiP2pThread currentP2pThread;
    public Integer basePos = 0;
    public Integer remoteBasePos = 0;
    private int lowChirpPosition;
    private int highChirpPosition;
    private int dataSavedSize = 204800;

    public short[] savedData = new short[dataSavedSize];



    private IndexMaxVarInfo mIndexMaxVarInfo;

    public DecodeThread(Handler mHandler, int size, boolean isOmittingLow, WifiP2pThread currentP2pThread){
        mIndexMaxVarInfo = new IndexMaxVarInfo();
        samplesList = new LinkedList<short[]>();
        lowChirpPositions = new LinkedList<Integer>();
        highChirpPositions = new LinkedList<Integer>();
        remoteLowChirpPositions = new LinkedList<Integer>();
        remoteHighChirpPositions = new LinkedList<Integer>();
        this.mHandler = mHandler;
        initialize(size);
        this.isOmittingLow = isOmittingLow;
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

                    mIndexMaxVarInfo = getIndexMaxVarInfoFromFDomain2(fft, lowChirpFFT);
//                    mIndexMaxVarInfo.isReferenceSignalExist = true;

                    if(mIndexMaxVarInfo.isReferenceSignalExist) {
                        lowChirpPosition = processBufferSize * mLoopCounter + mIndexMaxVarInfo.index;

                        synchronized (lowChirpPositions) {
                            if(lowChirpPositions.size() > 0) {
                                Common.println("low chirp pos:" + lowChirpPosition + "    lowdiff:" + (lowChirpPositions.get(lowChirpPositions.size() - 1) - lowChirpPosition));
                            }
                            lowChirpPositions.add(lowChirpPosition);
                        }
                        String msg = FlagVar.lowPosStr+" "+lowChirpPosition;
                        currentP2pThread.setMessage(msg);


                    }

                    mIndexMaxVarInfo = getIndexMaxVarInfoFromFDomain2(fft, highChirpFFT);
//                    mIndexMaxVarInfo.isReferenceSignalExist = true;


                    if(mIndexMaxVarInfo.isReferenceSignalExist) {
                        highChirpPosition = processBufferSize * mLoopCounter + mIndexMaxVarInfo.index;
                        synchronized (highChirpPositions) {
                            if(highChirpPositions.size() > 0) {
                                Common.println("high chirp pos:" + highChirpPosition + "    highdiff:" + (highChirpPositions.get(highChirpPositions.size() - 1) - highChirpPosition));
                            }
                            highChirpPositions.add(highChirpPosition);
                        }
                        String msg = FlagVar.highPosStr+" "+highChirpPosition;
                        currentP2pThread.setMessage(msg);
                    }
                    Common.println("diff:"+(lowChirpPosition-highChirpPosition));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
                    }
                }
                else if(strs[0].equals(FlagVar.highPosStr)){
                    synchronized (remoteHighChirpPositions){
                        remoteHighChirpPositions.add(Integer.parseInt(strs[1]));
                    }
                }
                else if(strs[0].equals(FlagVar.basePosStr)){
                    synchronized (remoteBasePos){
                        remoteBasePos = Integer.parseInt(strs[1]);
                    }
                }
            }
        }
    };


}
