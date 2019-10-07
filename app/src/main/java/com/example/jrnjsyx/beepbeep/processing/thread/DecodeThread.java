package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Handler;

import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.processing.IndexMaxVarInfo;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;
import com.example.jrnjsyx.beepbeep.utils.JniUtils;

import java.util.LinkedList;
import java.util.List;


public class DecodeThread extends Decoder implements Runnable {

    private static final String TAG = "DecodeThread";
    private boolean isThreadRunning = true;
    private Integer mLoopCounter = 0;
    private Handler mHandler;
    public List<short[]> samplesList;
    public List<Integer> lowChirpPos;
    public List<Integer> highChirpPos;
    private boolean isOmittingLow;


    private IndexMaxVarInfo mIndexMaxVarInfo;

    public DecodeThread(Handler mHandler,int size,boolean isOmittingLow){
        mIndexMaxVarInfo = new IndexMaxVarInfo();
        samplesList = new LinkedList<short[]>();
        lowChirpPos = new LinkedList<Integer>();
        highChirpPos = new LinkedList<Integer>();
        this.mHandler = mHandler;
        initialize(size);
        this.isOmittingLow = isOmittingLow;
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

                    mIndexMaxVarInfo = getIndexMaxVarInfoFromFDomain(fft, lowChirpFFT);

                    if(mIndexMaxVarInfo.isReferenceSignalExist) {
                        int sampleCnt = processBufferSize * mLoopCounter + mIndexMaxVarInfo.index;
                        Common.println("low chirp pos:"+sampleCnt);
                        synchronized (lowChirpPos) {
                            lowChirpPos.add(sampleCnt);
                        }
                    }

                    mIndexMaxVarInfo = getIndexMaxVarInfoFromFDomain(fft, highChirpFFT);

                    if(mIndexMaxVarInfo.isReferenceSignalExist) {
                        int sampleCnt = processBufferSize * mLoopCounter + mIndexMaxVarInfo.index;
                        Common.println("high chirp pos:"+sampleCnt);
                        synchronized (highChirpPos) {
                            highChirpPos.add(sampleCnt);
                        }
                    }



                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void decodeStart(){
        try {
            synchronized (lowChirpPos){
                lowChirpPos.clear();
            }
            synchronized (highChirpPos){
                highChirpPos.clear();
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
