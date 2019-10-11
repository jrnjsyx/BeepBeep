package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Handler;

import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.processing.IndexMaxVarInfo;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;
import com.example.jrnjsyx.beepbeep.utils.JniUtils;
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

                    synchronized (mLoopCounter) {
                        mLoopCounter++;
                    }
                    float[] fft = JniUtils.fft(normalization(buffer), chirpCorrLen);

                    String msg = "";
                    mIndexMaxVarInfo = getIndexMaxVarInfoFromFDomain(fft, lowChirpFFT);
                    int lowChirpPosition;
                    if(mIndexMaxVarInfo.isReferenceSignalExist) {
                        lowChirpPosition = processBufferSize * mLoopCounter + mIndexMaxVarInfo.index;
                        Common.println("low chirp pos:"+lowChirpPosition);
                        synchronized (lowChirpPositions) {
                            lowChirpPositions.add(lowChirpPosition);
                        }
                        msg += "lowPos: "+lowChirpPosition+" ";
                    }

                    mIndexMaxVarInfo = getIndexMaxVarInfoFromFDomain(fft, highChirpFFT);
                    int highChirpPosition;
                    if(mIndexMaxVarInfo.isReferenceSignalExist) {
                        highChirpPosition = processBufferSize * mLoopCounter + mIndexMaxVarInfo.index;
                        Common.println("high chirp pos:"+highChirpPosition);
                        synchronized (highChirpPositions) {
                            highChirpPositions.add(highChirpPosition);
                        }
                        msg += "highPos: "+highChirpPosition;


                    }







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


}
