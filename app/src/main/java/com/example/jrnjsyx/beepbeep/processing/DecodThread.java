package com.example.jrnjsyx.beepbeep.processing;

import android.os.Handler;

import com.example.jrnjsyx.beepbeep.utils.JniUtils;

import java.util.LinkedList;
import java.util.List;


public class DecodThread extends Decoder implements Runnable {

    private static final String TAG = "DecodThread";
    private boolean isThreadRunning = true;
    private Integer mLoopCounter = 0;
    private Handler mHandler;
    public List<short[]> samplesList;
    public List<Integer> sampleCnts;


    private IndexMaxVarInfo mIndexMaxVarInfo;

    public DecodThread(Handler mHandler){
        mIndexMaxVarInfo = new IndexMaxVarInfo();
        samplesList = new LinkedList<short[]>();
        sampleCnts = new LinkedList<Integer>();
        this.mHandler = mHandler;
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
                    short[] buffer = new short[processBufferSize+startBeforeMaxCorr+ lChirp];
                    synchronized (samplesList) {
                        System.arraycopy(samplesList.get(0),0,buffer,0,processBufferSize);
                        System.arraycopy(samplesList.get(1),0,buffer,0, lChirp +startBeforeMaxCorr);

                        samplesList.remove(0);

                    }

                    synchronized (mLoopCounter) {
                        mLoopCounter++;
                    }
                    //compute the fft of the bufferedSamples, it will be used twice. It's computed here to reduce time cost.
                    float[] fft = JniUtils.fft(normalization(buffer), chirpCorrLen);

                    // 1. the first step is to check the existence of preamble either up or down
                    mIndexMaxVarInfo = getIndexMaxVarInfoFromFDomain(fft, upChirpFFT);

                    if(mIndexMaxVarInfo.isReferenceSignalExist) {
                        int sampleCnt = processBufferSize * mLoopCounter + mIndexMaxVarInfo.index;
                        synchronized (sampleCnts) {
                            sampleCnts.add(sampleCnt);
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
            synchronized (sampleCnts){
                sampleCnts.clear();
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
