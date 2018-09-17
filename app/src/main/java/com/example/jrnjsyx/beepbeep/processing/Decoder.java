package com.example.jrnjsyx.beepbeep.processing;

import com.example.jrnjsyx.beepbeep.physical.SignalGenerator;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;
import com.example.jrnjsyx.beepbeep.utils.JniUtils;

import java.util.Arrays;

public class Decoder implements FlagVar {

    public static short[] upChirp = SignalGenerator.upChirpGenerator(FlagVar.Fs, FlagVar.tChrip, FlagVar.bChirp, FlagVar.Fmin);
    public static short[] downChirp = SignalGenerator.downChirpGenerator(FlagVar.Fs, FlagVar.tChrip, FlagVar.bChirp, FlagVar.Fmax);


    // create variables to store the samples in case frequent new and return

    public int processBufferSize = 4096;

    public int chirpCorrLen = getFFTLen(processBufferSize+ lChirp, lChirp);

    //we calculate the fft before decoding in order to reduce computation time.
    public float[] upChirpFFT;
    public float[] downChirpFFT;
    public float[][] upSymbolFFTs;
    public float[][] downSymbolFFTs;
    protected float maRatio;
    protected float ratio;




    public void initialize(int processBufferSize){
        this.processBufferSize = processBufferSize;
        chirpCorrLen = getFFTLen(processBufferSize+ lChirp +startBeforeMaxCorr, lChirp);

        //compute the fft of the preambles and symbols to reduce the computation cost;
        upChirpFFT = new float[chirpCorrLen];
        downChirpFFT = new float[chirpCorrLen];

        upChirpFFT = JniUtils.fft(normalization(upChirp), chirpCorrLen);
        downChirpFFT = JniUtils.fft(normalization(downChirp), chirpCorrLen);



    }





    /**
     * normalize the short data to float array
     * @param s : data stream of short samples
     * @return normalized data in float format
     */
    public float[] normalization(short s[]){
        float[] normalized = new float[s.length];
        for (int i = 0; i < s.length; i++) {
            normalized[i] = (float) (s[i]) / 32768;
        }
        return normalized;
    }

    public float[] normalization(short s[], int low, int high){
        float[] normalized = new float[high-low+1];
        for(int i=low;i<=high;i++){
            normalized[i-low] = (float)(s[i])/32768;
        }
        return normalized;
    }


    /**
     * compute the best fit position of the chirp signal (data2) from data1.
     * @auther ruinan jin
     * @param data1: audio samples
     * @param data2: reference signal
     * @param isFdomain: whether the data is frequency domain data.
     * @return: return the fit value and its index
     */

    public IndexMaxVarInfo getIndexMaxVarInfoFromFloats(float[] data1,float[] data2, boolean isFdomain){

        if(!isFdomain){
            int len = getFFTLen(data1.length,data2.length);
            data1 = JniUtils.fft(data1,len);
            data2 = JniUtils.fft(data2,len);
        }
        //compute the corr
        float[] corr = JniUtils.xcorr(data1,data2);

        if(chirpDetectionType >= DETECT_TYPE1 && chirpDetectionType <= DETECT_TYPE3) {
            IndexMaxVarInfo indexMaxVarInfo = new IndexMaxVarInfo();
            //compute the fit vals
            float[] fitVals = getFitValsFromCorr(corr);
            //get the fit index
            int index = getFitPos(fitVals, corr);
            indexMaxVarInfo.index = index;
            indexMaxVarInfo.fitVal = fitVals[index];
            if(index == 0 ){
                System.out.println();
                System.out.println("corr:"+corr.length+" "+Arrays.toString(corr));
                System.out.println("fitVals:"+fitVals.length+" "+Arrays.toString(fitVals));
            }

            //detect whether the chirp signal exists.
            IndexMaxVarInfo resultInfo = preambleDetection(corr, indexMaxVarInfo);
            return resultInfo;
        }else {
            IndexMaxVarInfo info = Algorithm.getMaxInfo(corr,0,corr.length-1);
            float mean = Algorithm.meanValue(corr,info.index-startBeforeMaxCorr,info.index-endBeforeMaxCorr-1);
            maRatio = info.fitVal/mean;
            System.out.println("index:"+info.index+"   maRatio:"+maRatio+"   maxCorr:"+corr[info.index]);
            if(isIndexAvailable(info)){

                if((maRatio>maxAvgRatioThreshold)) {
                    info.isReferenceSignalExist = true;
                }
            }
            return info;
        }
    }





    /** compute the position of the max value of the fitVals, while the corr of this position should be larger than a threshold.
     * @auther Ruinan Jin
     * @param fitVals fit value arrays.
     * @param corr correlation arrays.
     * @return
     */
    public int getFitPos(float [] fitVals, float[] corr){
        float max = 0;
        float maxCorr = 0;
        int index = 0;
        for(int i=0;i<fitVals.length;i++){
            maxCorr = maxCorr<corr[i]?corr[i]:maxCorr;
        }
        float threshold = ratioAvailableThreshold*maxCorr;
        for(int i=0;i<fitVals.length;i++){
            if(fitVals[i]>max && corr[i]>threshold){
                max = fitVals[i];
                index = i;
            }
        }

        return index;
    }

    /**
     * get the fitVals from corr. fitVals is the value by which the corr is devided the average of the previous 200(startBeforeMaxCorr) corrs.
     * The first 200 fitVals is zero, while this is why the buffer length should be added by startBeforeMaxCorr.
     * @auther ruinan jin
     * @param corr
     * @return fitVals
     */
    public float[] getFitValsFromCorr(float [] corr){
        float[] fitVals = new float[processBufferSize+ lChirp +startBeforeMaxCorr];
        float val = 0;
        for(int i=0;i<startBeforeMaxCorr-endBeforeMaxCorr;i++){
            val += corr[i];
        }
        for(int i=startBeforeMaxCorr;i<fitVals.length;i++){
            fitVals[i] = val;
            val -= corr[i-startBeforeMaxCorr];
            val += corr[i-endBeforeMaxCorr];

        }
        for(int i=startBeforeMaxCorr;i<fitVals.length;i++){
            fitVals[i] = corr[i]*(startBeforeMaxCorr-endBeforeMaxCorr)/fitVals[i];
            if(chirpDetectionType == FlagVar.DETECT_TYPE2){
                fitVals[i] = fitVals[i]*corr[i];
            }else if(chirpDetectionType == FlagVar.DETECT_TYPE3){
                fitVals[i] = fitVals[i]*corr[i]*corr[i];
            }
        }
        return fitVals;
    }

    /**
     * getIndexMaxVarInfoFromFloats method for only frequency domain.
     * @auther ruinan jin
     * @param sf
     * @param rf
     * @return
     */
    public IndexMaxVarInfo getIndexMaxVarInfoFromFDomain(float[] sf, float[] rf){
        IndexMaxVarInfo indexMaxVarInfo = getIndexMaxVarInfoFromFloats(sf,rf,true);
        return indexMaxVarInfo;
    }

    /**
     * compute the min value larger than the sum of len1 and len2 which is the power of 2.
     * @auther ruinan jin
     * @param len1
     * @param len2
     * @return
     */
    public int getFFTLen(int len1, int len2){
        int len = 1;
        while(len < len1+len2){
            len = len*2;
        }
        return len;
    }





    /**
     * detect whether the preamble exist
     * @auther ruinan jin
     * @param corr - correlation array
     * @param indexMaxVarInfo - the info of the max corr index
     * @return true indicate the presence of the corresponding preamble and vise versa
     */
    public IndexMaxVarInfo preambleDetection(float[] corr, IndexMaxVarInfo indexMaxVarInfo){
        indexMaxVarInfo.isReferenceSignalExist = false;
//        float ratio = indexMaxVarInfo.fitVal*corr[indexMaxVarInfo.index];
//        float ratio = indexMaxVarInfo.fitVal;
        /*
            we mainly detect the preamble by thersholding the value of the fitVals*log(corr+1) in the fit position. it's tested to be the
        best method considering the fitVals and the corr.
         */
        maRatio = indexMaxVarInfo.fitVal;
        if(chirpDetectionType == FlagVar.DETECT_TYPE2){
            maRatio = maRatio/corr[indexMaxVarInfo.index];
        }else if(chirpDetectionType == FlagVar.DETECT_TYPE3){
            maRatio = maRatio/corr[indexMaxVarInfo.index]/corr[indexMaxVarInfo.index];
        }
        ratio = (float) (maRatio*Math.log(corr[indexMaxVarInfo.index]+1));
        if(maRatio > maxAvgRatioThreshold && ratio > ratioThreshold && isIndexAvailable(indexMaxVarInfo)) {
            indexMaxVarInfo.isReferenceSignalExist = true;
        }
        System.out.println("index:"+indexMaxVarInfo.index+"   ratio:"+ratio+"   maRatio:"+maRatio+"  marThreshold:"+maxAvgRatioThreshold+"  rThreshold:"+ratioThreshold);
        return indexMaxVarInfo;
    }




    public boolean isIndexAvailable(IndexMaxVarInfo indexMaxVarInfo){
        int index = indexMaxVarInfo.index;
        if(index >= processBufferSize+startBeforeMaxCorr || index < startBeforeMaxCorr){
            return false;
        }else{
            return true;
        }
    }




}
