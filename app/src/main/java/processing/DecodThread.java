package processing;

import android.os.Handler;

import java.util.LinkedList;
import java.util.List;


public class DecodThread extends Decoder implements Runnable {

    private static final String TAG = "DecodThread";
    private boolean isBufferReady = false;
    private boolean isThreadRunning = true;
    private boolean isTDOAObtained = false;
    private Integer mLoopCounter = 0;
    private int mTDOACounter = 0;
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

                    synchronized (samplesList) {
                        System.arraycopy(samplesList.get(0),processBufferSize-LPreamble,bufferedSamples,0,LPreamble);
                        System.arraycopy(samplesList.get(1),0,bufferedSamples,LPreamble,processBufferSize);

                        samplesList.remove(0);

                    }

                    synchronized (mLoopCounter) {
                        mLoopCounter++;
                    }
                    //compute the fft of the bufferedSamples, it will be used twice. It's computed here to reduce time cost.
                    float[] fft = getData1FFtFromSignals(bufferedSamples, upPreamble.length);

                    // 1. the first step is to check the existence of preamble either up or down
                    mIndexMaxVarInfo = getIndexMaxVarInfoFromFDomain(fft,upPreambleFFT);

                    if(mIndexMaxVarInfo.isReferenceSignalExist) {
                        if(mIndexMaxVarInfo.index < processBufferSize) {
                            int sampleCnt = processBufferSize * mLoopCounter + mIndexMaxVarInfo.index;
                            synchronized (sampleCnts) {
                                sampleCnts.add(sampleCnt);
                            }
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
    public void close() {
        synchronized (this){
            isThreadRunning = false;
            //Thread.currentThread().join();
        }
    }


}
