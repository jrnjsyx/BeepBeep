package com.example.jrnjsyx.beepbeep.processing.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.jrnjsyx.beepbeep.physical.thread.PlayThread;
import com.example.jrnjsyx.beepbeep.processing.Algorithm;
import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.processing.IndexMaxVarInfo;
import com.example.jrnjsyx.beepbeep.processing.KalmanFilter;
import com.example.jrnjsyx.beepbeep.processing.SpeedInfo;
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
    public Integer mLoopCounter = 0;
    private Handler mHandler;
    public List<short[]> samplesList;
    public List<Integer> lowChirpPositions;
    public List<Integer> highChirpPositions;
    public List<Integer> remoteLowChirpPositions;
    public List<Integer> remoteHighChirpPositions;
    private WifiP2pThread currentP2pThread;
    public int lowChirpPosition;
    public int highChirpPosition;
    public int highUpChirpPosition;
    public int remoteLowChirpPosition;
    public int remoteHighChirpPosition;
    public int remoteHighUpChirpPosition;
    private int dataSavedSize = 409600;
    private PlayThread playThread;
    private boolean isChirpFrequencyLow = false;
    private boolean isAdjusted = false;
    private int diff = 0;
    private int remoteDiff = 0;
    public Boolean skip = false;
    private int[] savednormalPos = {0,0,0,0};
    private int abnormalCnt = 0;
    private int adjustCnt = FlagVar.adjustThreshold;
    private IndexMaxVarInfo infoLow;
    private IndexMaxVarInfo infoHigh;
    private int lowIndex = 0;
    private int highIndex = 0;
    private int[] previousLowIndexes = {0,0};
    private int[] previousHighIndexes = {0,0};
    public SpeedInfo speedInfo;
    public float speed;
    private short[] speedBuffer = new short[FlagVar.lSine];
    public boolean isAbnormal;
    KalmanFilter kalmanFilter;
    public Bundle mainData = new Bundle();
    private boolean firstAdjust = true;
    public List<String> savedMainData = new LinkedList<String>();
    public int lowFoundCnt = 0;
    public int highFoundCnt = 0;
    public int frontFoundCnt = 0;
    public int backFoundCnt = 0;


    public short[] savedData = new short[dataSavedSize];



    private IndexMaxVarInfo mIndexMaxVarInfo;

    public DecodeThread(Handler mHandler, WifiP2pThread currentP2pThread,PlayThread playThread,boolean isChirpFrequencyLow){
        kalmanFilter = new KalmanFilter(this);
        mIndexMaxVarInfo = new IndexMaxVarInfo();
        samplesList = new LinkedList<short[]>();
        lowChirpPositions = new LinkedList<Integer>();
        highChirpPositions = new LinkedList<Integer>();
        remoteLowChirpPositions = new LinkedList<Integer>();
        remoteHighChirpPositions = new LinkedList<Integer>();
        this.mHandler = mHandler;
        this.playThread = playThread;
        this.isChirpFrequencyLow = isChirpFrequencyLow;
        initialize(FlagVar.recordBufferSize);
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
                if(FlagVar.currentRangingMode == FlagVar.BEEP_BEEP_MODE) {
                    runOnBeepbeepPerTurn();
                }
                else if(FlagVar.currentRangingMode == FlagVar.MY_NEW_MODE){
                    runOnMyNewPerTurn();
                }
                else if(FlagVar.currentRangingMode == FlagVar.MY_ORIGINAL_MODE){
                    runOnMyOriginalPerTurn();
                }
                else if(FlagVar.currentRangingMode == FlagVar.ORIGINAL_BEEP_BEEP_MODE){
                    runOnOriginalBeepbeepPerTurn();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean skip(){
        if(skip){
            skip = false;
            synchronized (samplesList) {
                samplesList.remove(0);
            }
            return true;
        }
        return false;
    }

    private void constructBuffer(){
        synchronized (samplesList) {
//            System.arraycopy(samplesList.get(0),processBufferSize-FlagVar.lChirp-FlagVar.startBeforeMaxCorr-FlagVar.lSine,buffer,
//                    0,FlagVar.lChirp+FlagVar.startBeforeMaxCorr+FlagVar.lSine);
//            System.arraycopy(samplesList.get(1),0,buffer,FlagVar.lChirp+FlagVar.startBeforeMaxCorr+FlagVar.lSine,processBufferSize-FlagVar.lSine);
//            System.arraycopy(samplesList.get(0),processBufferSize-FlagVar.lChirp-FlagVar.startBeforeMaxCorr-FlagVar.lSine,bufferAll,
//                    0,FlagVar.lChirp+FlagVar.startBeforeMaxCorr+FlagVar.lSine);
//            System.arraycopy(samplesList.get(1),0,bufferAll,FlagVar.lChirp+FlagVar.startBeforeMaxCorr+FlagVar.lSine,processBufferSize);

            System.arraycopy(samplesList.get(0),processBufferSize-FlagVar.lChirp-FlagVar.startBeforeMaxCorr,buffer,
                    0,FlagVar.lChirp+FlagVar.startBeforeMaxCorr);
            System.arraycopy(samplesList.get(1),0,buffer,FlagVar.lChirp+FlagVar.startBeforeMaxCorr,processBufferSize);
//            System.arraycopy(samplesList.get(0),processBufferSize-FlagVar.lChirp-FlagVar.startBeforeMaxCorr,bufferAll,
//                    0,FlagVar.lChirp+FlagVar.startBeforeMaxCorr);
//            System.arraycopy(samplesList.get(1),0,bufferAll,FlagVar.lChirp+FlagVar.startBeforeMaxCorr,processBufferSize);
            samplesList.remove(0);

        }
        if(mLoopCounter<dataSavedSize/processBufferSize&& Common.isDebug) {
            System.arraycopy(samplesList.get(0), 0, savedData, mLoopCounter * FlagVar.recordBufferSize, processBufferSize);
        }
    }

    private void saveAndSendPosition(int index,List<Integer> indexes,String flag){
        if(indexes != null) {
            synchronized (indexes) {
                indexes.add(index);
                if (indexes.size() > FlagVar.minPosDataCapacity) {
                    indexes.remove(0);
                }
            }
        }
        String msg = flag+" "+index;
        currentP2pThread.setMessage(msg);
    }

    private void saveAndSendLowPos(int index){
        saveAndSendPosition(index,lowChirpPositions,FlagVar.lowPosStr);
    }

    private void saveAndSendHighPos(int index){
        saveAndSendPosition(index,highChirpPositions,FlagVar.highPosStr);
    }

    private void saveAndSendHighUpPos(int index){
        saveAndSendPosition(index,null,FlagVar.highUpPosStr);
    }

    private void analysisDataForBecconDetection(){
        short[] dataFront = new short[FlagVar.lChirp+FlagVar.startBeforeMaxCorr+processBufferSize/2];
        short[] dataBack = new short[FlagVar.lChirp+FlagVar.startBeforeMaxCorr+processBufferSize/2];
        System.arraycopy(buffer,0,dataFront,0,dataFront.length);
        System.arraycopy(buffer,processBufferSize/2,dataBack,0,dataBack.length);
        float[] fftFront = JniUtils.fft(normalization(dataFront), halfChirpCorrLen);
        float[] fftBack = JniUtils.fft(normalization(dataBack), halfChirpCorrLen);
        IndexMaxVarInfo infoLowFront = getIndexMaxVarInfoFromFDomain2(fftFront, lowUpChirpFFT);
        if(infoLowFront.isReferenceSignalExist){
            frontFoundCnt++;
        }
        IndexMaxVarInfo infoBack = getIndexMaxVarInfoFromFDomain2(fftBack, lowUpChirpFFT);
        if(infoBack.isReferenceSignalExist){
            backFoundCnt++;
        }

    }

    private void analysisData(){
        float[] fft = JniUtils.fft(normalization(buffer), chirpCorrLen);
        infoLow = getIndexMaxVarInfoFromFDomain2(fft, lowUpChirpFFT);
        infoHigh = getIndexMaxVarInfoFromFDomain2(fft, highDownChirpFFT);
        if(infoLow.isReferenceSignalExist){
            lowFoundCnt++;
        }
        if(infoHigh.isReferenceSignalExist){
            highFoundCnt++;
        }

        if(isChirpFrequencyLow){
            lowIndex = infoLow.index;
            int lows[] = new int[3];
            int highs[] = new int[3];
            for(int i=0;i<lows.length;i++){
                lows[i] = lowIndex+FlagVar.chirpInterval*(i-1);
                highs[i] = lows[i]+FlagVar.lChirp;
            }
            for(int i=0;i<buffer.length;i++){
                for(int j=0;j<lows.length;j++){
                    if(i<highs[j] && i>=lows[j]){
                        buffer[i] = 0;
                    }
                }
            }
            float fft2[] = JniUtils.fft(normalization(buffer), chirpCorrLen);
            IndexMaxVarInfo infoHigh2 = getIndexMaxVarInfoFromFDomain2(fft2, highDownChirpFFT);
            highIndex = infoHigh2.index;
//            previousHighIndexes[0] = infoHigh.index;
//            previousHighIndexes[1] = infoHigh2.index;
        }

        else{
            highIndex = infoHigh.index;
            int lows[] = new int[3];
            int highs[] = new int[3];
            for(int i=0;i<lows.length;i++){
                lows[i] = highIndex+FlagVar.chirpInterval*(i-1);
                highs[i] = lows[i]+FlagVar.lChirp;
            }
            for(int i=0;i<buffer.length;i++){
                for(int j=0;j<lows.length;j++){
                    if(i<highs[j] && i>=lows[j]){
                        buffer[i] = 0;
                    }
                }
            }
            float fft2[] = JniUtils.fft(normalization(buffer), chirpCorrLen);
            IndexMaxVarInfo infoLow2 = getIndexMaxVarInfoFromFDomain2(fft2, lowUpChirpFFT);
            lowIndex = infoLow2.index;
        }




        lowChirpPosition = processBufferSize * mLoopCounter + lowIndex;
        highChirpPosition = processBufferSize * mLoopCounter + highIndex;
    }


    int distance(int i1,int i2){
        return Math.abs(Algorithm.moveIntoRange(i1-i2,0-FlagVar.chirpInterval/2,FlagVar.chirpInterval));
    }

    //speed estimation by adding sine wave and analyzing its frequency domain. Abandoned
    private void runOnMyOriginalPerTurn2(){
        if (samplesList.size() >= 2) {
            if(skip()){
                return;
            }
            constructBuffer();

            synchronized (mLoopCounter) {
                mLoopCounter++;
            }
            analysisData();

            saveAndSendLowPos(lowIndex);
            saveAndSendHighPos(highIndex);

            if(isChirpFrequencyLow){
                System.arraycopy(bufferAll,highIndex+FlagVar.lChirp,speedBuffer,0,FlagVar.lSine);
                speedInfo = estimateSpeed(normalization(speedBuffer));
                speed = speedInfo.speed;
                String msg = FlagVar.skipStr+" "+speed;
                currentP2pThread.setMessage(msg);
            }

            isAdjusted = adJustSamples();
            if(isChirpFrequencyLow && isAdjusted){
                skip = true;
                String msg = FlagVar.skipStr+" "+"true";
                currentP2pThread.setMessage(msg);
            }
            sendDebugMsg();

        }
    }



    private void analysisDataForMyNew(){
        float[] fft = JniUtils.fft(normalization(buffer), chirpCorrLen);
        infoLow = getIndexMaxVarInfoFromFDomain2(fft, lowUpChirpFFT);
        infoHigh = getIndexMaxVarInfoFromFDomain2(fft, highDownChirpFFT);
        if(infoLow.isReferenceSignalExist){
            lowFoundCnt++;
        }
        if(infoHigh.isReferenceSignalExist){
            highFoundCnt++;
        }

        if(isChirpFrequencyLow){
            lowIndex = infoLow.index;
            int lows[] = new int[3];
            int highs[] = new int[3];
            for(int i=0;i<lows.length;i++){
                lows[i] = lowIndex+FlagVar.chirpInterval*(i-1);
                highs[i] = lows[i]+FlagVar.lChirp*2;
            }
            for(int i=0;i<buffer.length;i++){
                for(int j=0;j<lows.length;j++){
                    if(i<highs[j] && i>=lows[j]){
                        buffer[i] = 0;
                    }
                }
            }
            float fft2[] = JniUtils.fft(normalization(buffer), chirpCorrLen);
            IndexMaxVarInfo infoHighDown = getIndexMaxVarInfoFromFDomain2(fft2, highDownChirpFFT);

            IndexMaxVarInfo infoHighUp = getIndexMaxVarInfoFromFDomain2(fft2, highUpChirpFFT);
            int highDiff = infoHighUp.index-infoHighDown.index;
            highDiff = highDiff-FlagVar.lChirp;
            highDiff = Algorithm.moveIntoRange(highDiff,0-FlagVar.chirpInterval/2,FlagVar.chirpInterval);
            speed = (float) FlagVar.bChirp2*highDiff*FlagVar.soundSpeed/(FlagVar.highFStart-FlagVar.bChirp2/2)/FlagVar.lChirp/2;

            highIndex = infoHighDown.index;
//            previousHighIndexes[0] = infoHigh.index;
//            previousHighIndexes[1] = infoHigh2.index;
        }

        else{
            highIndex = infoHigh.index;
            int lows[] = new int[3];
            int highs[] = new int[3];
            for(int i=0;i<lows.length;i++){
                lows[i] = highIndex+FlagVar.chirpInterval*(i-1);
                highs[i] = lows[i]+FlagVar.lChirp;
            }
            for(int i=0;i<buffer.length;i++){
                for(int j=0;j<lows.length;j++){
                    if(i<highs[j] && i>=lows[j]){
                        buffer[i] = 0;
                    }
                }
            }
            float fft2[] = JniUtils.fft(normalization(buffer), chirpCorrLen);

            IndexMaxVarInfo infoLowUp = getIndexMaxVarInfoFromFDomain2(fft2, lowUpChirpFFT);

//            IndexMaxVarInfo infoLowDown = getIndexMaxVarInfoFromFDomain2(fft2, lowDownChirpFFT);
//            int lowDiff = infoLowDown.index-infoLowUp.index;
//            lowDiff = lowDiff-FlagVar.lChirp;
//            lowDiff = Algorithm.moveIntoRange(lowDiff,0-FlagVar.chirpInterval/2,FlagVar.chirpInterval);
//            speed = (float) FlagVar.bChirp*lowDiff*FlagVar.soundSpeed/(FlagVar.lowFStart+FlagVar.bChirp/2)/FlagVar.lChirp/2;
//            String speedStr = FlagVar.speedStr+" "+speed;
//            currentP2pThread.setMessage(speedStr);

            lowIndex = infoLowUp.index;
        }
        lowChirpPosition = processBufferSize * mLoopCounter + lowIndex;
        highChirpPosition = processBufferSize * mLoopCounter + highIndex;
    }
    //speed estimation by computing dopller effects of chirp signal.
    private void runOnMyNewPerTurn(){
        if (samplesList.size() >= 2) {
            if(skip()){
                return;
            }
            constructBuffer();

            synchronized (mLoopCounter) {
                mLoopCounter++;
            }
            analysisDataForMyNew();
//            saveAndSendLowPos(lowIndex);
//            saveAndSendHighPos(highIndex);
            saveAndSendLowPos(lowChirpPosition);
            saveAndSendHighPos(highChirpPosition);


            isAdjusted = adJustSamples();
            if(isChirpFrequencyLow && isAdjusted){
                skip = true;
                String msg = FlagVar.skipStr+" "+"true";
                currentP2pThread.setMessage(msg);
            }
            if(isChirpFrequencyLow) {
                kalmanFilter.computeOnce();
                sendMainMsgForLow();

            }
            sendAndSaveMainMsg();
            sendDebugMsg();

        }
    }


    private void analysisDataForMyOriginal(){
        float[] fft = JniUtils.fft(normalization(buffer), chirpCorrLen);
        infoLow = getIndexMaxVarInfoFromFDomain2(fft, lowUpChirpFFT);
        infoHigh = getIndexMaxVarInfoFromFDomain2(fft, highDownChirpFFT);
        int highUpIndex = 0;
        if(infoLow.isReferenceSignalExist){
            lowFoundCnt++;
        }
        if(infoHigh.isReferenceSignalExist){
            highFoundCnt++;
        }

        if(isChirpFrequencyLow){
            lowIndex = infoLow.index;
            int lows[] = new int[3];
            int highs[] = new int[3];
            for(int i=0;i<lows.length;i++){
                lows[i] = lowIndex+FlagVar.chirpInterval*(i-1);
                highs[i] = lows[i]+FlagVar.lChirp*2;
            }
            for(int i=0;i<buffer.length;i++){
                for(int j=0;j<lows.length;j++){
                    if(i<highs[j] && i>=lows[j]){
                        buffer[i] = 0;
                    }
                }
            }
            float fft2[] = JniUtils.fft(normalization(buffer), chirpCorrLen);
            IndexMaxVarInfo infoHighDown = getIndexMaxVarInfoFromFDomain2(fft2, highDownChirpFFT);

            IndexMaxVarInfo infoHighUp = getIndexMaxVarInfoFromFDomain2(fft2, highUpChirpFFT);
            int highDiff = infoHighUp.index-infoHighDown.index;
//            highDiff = highDiff-FlagVar.lChirp;
            highDiff = Algorithm.moveIntoRange(highDiff,0-FlagVar.chirpInterval/2,FlagVar.chirpInterval);
            speed = (float) FlagVar.bChirp2*highDiff*FlagVar.soundSpeed/(FlagVar.highFStart-FlagVar.bChirp2/2)/FlagVar.lChirp/2;

            highIndex = infoHighDown.index;
            highUpIndex = infoHighUp.index;
//            previousHighIndexes[0] = infoHigh.index;
//            previousHighIndexes[1] = infoHigh2.index;
        }

        else{
            highIndex = infoHigh.index;
            highUpIndex = getIndexMaxVarInfoFromFDomain2(fft, highUpChirpFFT).index;

            int lows[] = new int[3];
            int highs[] = new int[3];
            for(int i=0;i<lows.length;i++){
                lows[i] = highIndex+FlagVar.chirpInterval*(i-1);
                highs[i] = lows[i]+FlagVar.lChirp;
            }
            for(int i=0;i<buffer.length;i++){
                for(int j=0;j<lows.length;j++){
                    if(i<highs[j] && i>=lows[j]){
                        buffer[i] = 0;
                    }
                }
            }
            float fft2[] = JniUtils.fft(normalization(buffer), chirpCorrLen);

            IndexMaxVarInfo infoLowUp = getIndexMaxVarInfoFromFDomain2(fft2, lowUpChirpFFT);

//            IndexMaxVarInfo infoLowDown = getIndexMaxVarInfoFromFDomain2(fft2, lowDownChirpFFT);
//            int lowDiff = infoLowDown.index-infoLowUp.index;
//            lowDiff = lowDiff-FlagVar.lChirp;
//            lowDiff = Algorithm.moveIntoRange(lowDiff,0-FlagVar.chirpInterval/2,FlagVar.chirpInterval);
//            speed = (float) FlagVar.bChirp*lowDiff*FlagVar.soundSpeed/(FlagVar.lowFStart+FlagVar.bChirp/2)/FlagVar.lChirp/2;
//            String speedStr = FlagVar.speedStr+" "+speed;
//            currentP2pThread.setMessage(speedStr);

            lowIndex = infoLowUp.index;
        }
        lowChirpPosition = processBufferSize * mLoopCounter + lowIndex;
        highChirpPosition = processBufferSize * mLoopCounter + highIndex;
        highUpChirpPosition = processBufferSize * mLoopCounter + highUpIndex;
    }

    private void runOnMyOriginalPerTurn(){
        if (samplesList.size() >= 2) {
            if(skip()){
                return;
            }
            constructBuffer();

            synchronized (mLoopCounter) {
                mLoopCounter++;
            }
            analysisDataForMyOriginal();
//            saveAndSendLowPos(lowIndex);
//            saveAndSendHighPos(highIndex);
            saveAndSendLowPos(lowChirpPosition);
            saveAndSendHighPos(highChirpPosition);
            saveAndSendHighUpPos(highUpChirpPosition);


            isAdjusted = adJustSamples();
            if(isChirpFrequencyLow && isAdjusted){
                skip = true;
                String msg = FlagVar.skipStr+" "+"true";
                currentP2pThread.setMessage(msg);
            }
            if(isChirpFrequencyLow) {
                kalmanFilter.computeOnce();
                getDistanceForBeep();
                sendMainMsgForLow();

            }
            sendAndSaveMainMsg();
            sendDebugMsg();

        }
    }

    private float distanceForBeep = 0;
    private void getDistanceForBeep(){
        int unprocessedDistanceCnt = highUpChirpPosition-lowChirpPosition-(remoteHighUpChirpPosition-remoteLowChirpPosition);
        int distanceCnt = Algorithm.moveIntoRange(unprocessedDistanceCnt,0,FlagVar.chirpInterval);
        distanceForBeep = FlagVar.cSample * distanceCnt;
    }

    private void sendMainMsgForLow(){
        currentP2pThread.setMessage(FlagVar.unhandledDistanceStr+" "+kalmanFilter.unhandledDistance);
        currentP2pThread.setMessage(FlagVar.unhandledSpeedStr+" "+kalmanFilter.unhandledSpeed);
        currentP2pThread.setMessage(FlagVar.unhandledDistanceCntStr+" "+kalmanFilter.distanceCnt);
        currentP2pThread.setMessage(FlagVar.speedStr+" "+kalmanFilter.speed);
        currentP2pThread.setMessage(FlagVar.distanceStr+" "+kalmanFilter.distance);
        currentP2pThread.setMessage(FlagVar.distanceForBeepStr+" "+distanceForBeep);
        mainData.putFloat(FlagVar.unhandledSpeedStr,kalmanFilter.unhandledSpeed);
        mainData.putFloat(FlagVar.unhandledDistanceStr,kalmanFilter.unhandledDistance);
        mainData.putInt(FlagVar.unhandledDistanceCntStr,kalmanFilter.distanceCnt);
        mainData.putFloat(FlagVar.speedStr,kalmanFilter.speed);
        mainData.putFloat(FlagVar.distanceStr,kalmanFilter.distance);
        mainData.putFloat(FlagVar.distanceForBeepStr,distanceForBeep);
    }
    private void sendAndSaveMainMsg(){
        Message msg = new Message();
        msg.what = FlagVar.MAIN_TEXT;
        msg.setData(mainData);
        mHandler.sendMessage(msg);
        String mainDataStr = mainData.getFloat(FlagVar.unhandledDistanceStr)+"\t"+
                mainData.getFloat(FlagVar.unhandledSpeedStr)+"\t"+
                mainData.getFloat(FlagVar.distanceStr)+"\t"+
                mainData.getFloat(FlagVar.speedStr)+"\t"+
                mainData.getFloat(FlagVar.distanceForBeepStr);
        savedMainData.add(mainDataStr);
    }

    private void runOnBeepbeepPerTurn(){
        if (samplesList.size() >= 2) {
            if(skip()){
                return;
            }
            constructBuffer();

            synchronized (mLoopCounter) {
                mLoopCounter++;
            }
            analysisData();
            analysisDataForBecconDetection();



//            saveAndSendLowPos(lowIndex);
//            saveAndSendHighPos(highIndex);
            saveAndSendLowPos(lowChirpPosition);
            saveAndSendHighPos(highChirpPosition);

            isAdjusted = adJustSamples();
            if(isChirpFrequencyLow && isAdjusted){
                skip = true;
                String msg = FlagVar.skipStr+" "+"true";
                currentP2pThread.setMessage(msg);
            }
            computeAndSendBeepMainMsg();


            sendDebugMsg();

        }
    }

    private void computeAndSendBeepMainMsg(){

        int unprocessedDistanceCnt = highChirpPosition-lowChirpPosition-(remoteHighChirpPosition-remoteLowChirpPosition);
        if(!isChirpFrequencyLow){
            unprocessedDistanceCnt = 0-unprocessedDistanceCnt;
        }
        int distanceCnt = Algorithm.moveIntoRange(unprocessedDistanceCnt,0,FlagVar.chirpInterval);
        float distance = FlagVar.cSample * distanceCnt;
        Common.println("distanceCnt:"+distanceCnt+"  distance:"+distance);
        Message msg = new Message();
        msg.what = FlagVar.BEEP_MAIN_TEXT;
        msg.arg1 = distanceCnt;
        mHandler.sendMessage(msg);
        String str = distance+"";
        savedMainData.add(str);
    }


    private void analysisDataForOriginalBeep(){
        float[] fft = JniUtils.fft(normalization(buffer), chirpCorrLen);
        infoLow = getIndexMaxVarInfoFromFDomain2(fft, lowUpChirpFFT);
        infoHigh = getIndexMaxVarInfoFromFDomain2(fft, highUpChirpFFT);
        if(infoLow.isReferenceSignalExist){
            lowFoundCnt++;
        }
        if(infoHigh.isReferenceSignalExist){
            highFoundCnt++;
        }

        if(isChirpFrequencyLow){
            lowIndex = infoLow.index;
            int lows[] = new int[3];
            int highs[] = new int[3];
            for(int i=0;i<lows.length;i++){
                lows[i] = lowIndex+FlagVar.chirpInterval*(i-1);
                highs[i] = lows[i]+FlagVar.lChirp;
            }
            for(int i=0;i<buffer.length;i++){
                for(int j=0;j<lows.length;j++){
                    if(i<highs[j] && i>=lows[j]){
                        buffer[i] = 0;
                    }
                }
            }
            float fft2[] = JniUtils.fft(normalization(buffer), chirpCorrLen);
            IndexMaxVarInfo infoHigh2 = getIndexMaxVarInfoFromFDomain2(fft2, highUpChirpFFT);
            highIndex = infoHigh2.index;
//            previousHighIndexes[0] = infoHigh.index;
//            previousHighIndexes[1] = infoHigh2.index;
        }

        else{
            highIndex = infoHigh.index;
            int lows[] = new int[3];
            int highs[] = new int[3];
            for(int i=0;i<lows.length;i++){
                lows[i] = highIndex+FlagVar.chirpInterval*(i-1);
                highs[i] = lows[i]+FlagVar.lChirp;
            }
            for(int i=0;i<buffer.length;i++){
                for(int j=0;j<lows.length;j++){
                    if(i<highs[j] && i>=lows[j]){
                        buffer[i] = 0;
                    }
                }
            }
            float fft2[] = JniUtils.fft(normalization(buffer), chirpCorrLen);
            IndexMaxVarInfo infoLow2 = getIndexMaxVarInfoFromFDomain2(fft2, lowUpChirpFFT);
            lowIndex = infoLow2.index;
        }




        lowChirpPosition = processBufferSize * mLoopCounter + lowIndex;
        highChirpPosition = processBufferSize * mLoopCounter + highIndex;
    }

    private void runOnOriginalBeepbeepPerTurn(){
        if (samplesList.size() >= 2) {
            if(skip()){
                return;
            }
            constructBuffer();

            synchronized (mLoopCounter) {
                mLoopCounter++;
            }
            analysisDataForOriginalBeep();
            analysisDataForBecconDetection();



//            saveAndSendLowPos(lowIndex);
//            saveAndSendHighPos(highIndex);
            saveAndSendLowPos(lowChirpPosition);
            saveAndSendHighPos(highChirpPosition);

            isAdjusted = adJustSamples();
            if(isChirpFrequencyLow && isAdjusted){
                skip = true;
                String msg = FlagVar.skipStr+" "+"true";
                currentP2pThread.setMessage(msg);
            }
            computeAndSendBeepMainMsg();


            sendDebugMsg();

        }
    }

    private int getPos(List<Integer> positions){
        int pos = 0;
        synchronized (positions){
            pos = Algorithm.getMedian(positions);
        }
        return pos;
    }

    private void clearPositions(List<Integer> positions){
        synchronized (positions){
            positions.clear();
        }
    }

    //不确定是recordBufferSize还是playBufferSize
    private boolean adJustSamples(){


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

        int previousDiffSum = Math.abs(savednormalPos[0]-lowPos)
                +Math.abs(savednormalPos[1]-highPos)
                +Math.abs(savednormalPos[2]-remoteLowPos)
                +Math.abs(savednormalPos[3]-remoteHighPos);
        isAbnormal = !(savednormalPos[0] == 0 && savednormalPos[1] == 0 && savednormalPos[2] == 0 && savednormalPos[3] == 0) && previousDiffSum > FlagVar.diffThreshold && abnormalCnt < 3;
        if(isAbnormal){
            abnormalCnt++;
        }else {
            abnormalCnt = 0;
            savednormalPos[0] = lowPos;
            savednormalPos[1] = highPos;
            savednormalPos[2] = remoteLowPos;
            savednormalPos[3] = remoteHighPos;
        }

        diff = highPos-lowPos;
        remoteDiff = remoteHighPos-remoteLowPos;
        diff = Algorithm.moveIntoRange(diff,0-FlagVar.recordBufferSize/2,FlagVar.recordBufferSize);
        remoteDiff = Algorithm.moveIntoRange(remoteDiff,0-FlagVar.recordBufferSize/2,FlagVar.recordBufferSize);
//        Common.println("savednormalPos:"+savednormalPos[0]+" "+savednormalPos[1]+" "+savednormalPos[2]+" "+savednormalPos[3]);
//        Common.println("Pos:"+lowPos+" "+highPos+" "+remoteLowPos+" "+remoteHighPos);
//        Common.println("low size:"+lowChirpPositions.size()+"   remote high size:"+remoteHighChirpPositions.size());
//        Common.println("diff:"+diff +" remoteDiff:"+remoteDiff+"  isAbnormal:"+isAbnormal+"   previousDiffSum:"+previousDiffSum);
        if(!isChirpFrequencyLow){
            return false;
        }

        int offset = 0;
        if(adjustCnt > 0){
            adjustCnt--;
        }
        if(remoteHighChirpPositions.size() >= 1 && lowChirpPositions.size() >= 1){
            //应该是减还是加？
//            int diffDiff = diff-remoteDiff;
            int diffDiff = diff+remoteDiff;
            boolean needAdjust;
            if(FlagVar.currentRangingMode == FlagVar.MY_ORIGINAL_MODE){
                //应该是减还是加？
//                diffDiff -= FlagVar.lSine;
//                needAdjust = (diff > 0-FlagVar.lChirp-FlagVar.lSine && diff <= FlagVar.lChirp) || (remoteDiff > 0-FlagVar.lChirp-FlagVar.lSine && remoteDiff <= FlagVar.lChirp);
                needAdjust = (diff > 0-FlagVar.lChirp && diff <= FlagVar.lChirp) || (remoteDiff > 0-FlagVar.lChirp && remoteDiff <= FlagVar.lChirp);
            }else if (FlagVar.currentRangingMode == FlagVar.BEEP_BEEP_MODE || FlagVar.currentRangingMode == FlagVar.ORIGINAL_BEEP_BEEP_MODE){
                needAdjust = (diff > 0-FlagVar.lChirp && diff <= FlagVar.lChirp) || (remoteDiff > 0-FlagVar.lChirp && remoteDiff <= FlagVar.lChirp);
            }else{
                //不太确定
//                diffDiff += FlagVar.lChirp;
//                needAdjust = (diff > 0-FlagVar.lChirp && diff <= 2*FlagVar.lChirp) || (remoteDiff > 0-FlagVar.lChirp && remoteDiff <= 2*FlagVar.lChirp);
                diffDiff -= FlagVar.lChirp;
                needAdjust = (diff > 0-FlagVar.lChirp*2 && diff <= FlagVar.lChirp) || (remoteDiff > 0-2*FlagVar.lChirp && remoteDiff <= FlagVar.lChirp);
            }
            needAdjust = (needAdjust && !isAbnormal && adjustCnt == 0) || firstAdjust;
            if(firstAdjust){
                firstAdjust = false;
            }
            //应该是减还是加？
            offset = (FlagVar.recordBufferSize+diffDiff)/2;
            Common.println("diff:"+diff +" remoteDiff:"+remoteDiff+" offset:"+offset+"  needAdjust:"+needAdjust+"  isAbnormal:"+isAbnormal);

            if(needAdjust){
//                if(!isAbnormal && adjustCnt == 0) {
                clearPositions(lowChirpPositions);
                clearPositions(highChirpPositions);
                clearPositions(remoteHighChirpPositions);
                clearPositions(remoteLowChirpPositions);
                savednormalPos[0] = 0;
                savednormalPos[1] = 0;
                savednormalPos[2] = 0;
                savednormalPos[3] = 0;
                playThread.adjustSample(offset);
                adjustCnt = FlagVar.adjustThreshold;
                Common.println("adjust.");
                return true;
//                }
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
                else if(strs[0].equals(FlagVar.highUpPosStr)){
                    remoteHighUpChirpPosition = Integer.parseInt(strs[1]);
                }
                else if(strs[0].equals(FlagVar.skipStr)){
                    synchronized (skip){
                        skip = Boolean.parseBoolean(strs[1]);
                    }
                }
                else if(strs[0].equals(FlagVar.speedStr)){
                    mainData.putFloat(FlagVar.speedStr,Float.parseFloat(strs[1]));
                }
                else if(strs[0].equals(FlagVar.distanceStr)){
                    mainData.putFloat(FlagVar.distanceStr,Float.parseFloat(strs[1]));
                }
                else if(strs[0].equals(FlagVar.unhandledDistanceCntStr)){
                    mainData.putInt(FlagVar.unhandledDistanceCntStr,Integer.parseInt(strs[1]));
                }
                else if(strs[0].equals(FlagVar.unhandledDistanceStr)){
                    mainData.putFloat(FlagVar.unhandledDistanceStr,Float.parseFloat(strs[1]));
                }
                else if(strs[0].equals(FlagVar.unhandledSpeedStr)){
                    mainData.putFloat(FlagVar.unhandledSpeedStr,Float.parseFloat(strs[1]));
                }
                else if(strs[0].equals(FlagVar.distanceForBeepStr)){
                    mainData.putFloat(FlagVar.distanceForBeepStr,Float.parseFloat(strs[1]));
                }
            }
        }
    };


}
