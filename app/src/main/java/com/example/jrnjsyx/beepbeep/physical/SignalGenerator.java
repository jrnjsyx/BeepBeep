package com.example.jrnjsyx.beepbeep.physical;


import com.example.jrnjsyx.beepbeep.processing.Algorithm;
import com.example.jrnjsyx.beepbeep.processing.IndexMaxVarInfo;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.util.LinkedList;
import java.util.List;

public class SignalGenerator  {

    /**
     * generate up chirp signal
     * @param fs - sampling rate
     * @param t - douration of the chirp signal
     * @param b - bandwidth
     * @param f - fmin
     * @return audio samples in short format
     */
    public static short[] upChirpGenerator(int fs, float t, int b, int f){
        float[] x = chirpGenerator(fs, t, b, f, 0);
        waveformReshaping(x);
        short[] samples = new short[x.length];
        for(int i = 0; i < x.length; i++){
            samples[i] = (short)(32767 * x[i]);
        }
        return samples;
    }

    public static short[] sigAverage(short[]sig1,short[]sig2){
        if(sig1.length != sig2.length){
            throw new RuntimeException("两信号长度不匹配。");
        }
        short[] ret = new short[sig1.length];
        for(int i=0;i<sig1.length;i++){
            ret[i] = (short)(sig1[i]/2+sig2[i]/2);
        }
        return ret;
    }

    public static float[] sigAverage(List<float[]> sigs){
        if(sigs == null || sigs.size() == 0 ){
            throw new RuntimeException("sigs不能为空。");
        }
        for(int i=0;i< sigs.size()-1;i++){
            if(sigs.get(i).length != sigs.get(i+1).length){
                throw new RuntimeException("信号长度不匹配。");
            }
        }
        float[] medRet = new float[sigs.get(0).length];
        for(float[] sig:sigs){
            for(int i=0;i<sig.length;i++){
                medRet[i] += sig[i];
            }
        }
        float[] ret = new float[sigs.get(0).length];
        for(int i=0;i<ret.length;i++){
            ret[i] = (float)(medRet[i]/sigs.size());
        }
        return ret;
    }

    public static short[] multipleSineWaveGenerator(int[] frequencies,int fs,int length){
        List<float[]> sigs = new LinkedList<float[]>();
        for(int f:frequencies){
            float[] sig = new float[length];
            for(int i=0;i<sig.length;i++){
                sig[i] = (float)Math.cos(2 * Math.PI * f * i / fs);
            }
            sigs.add(sig);
        }
        float[] sig = sigAverage(sigs);
        waveformReshaping(sig);
        short[] ret = new short[length];
        for(int i=0;i<sig.length;i++){
            ret[i] = (short)(32767*sig[i]);
        }
        return ret;

    }

    public static short[] addSig(short[] sig1,short[] sig2){
        short[] sig = new short[sig1.length+sig2.length];
        System.arraycopy(sig1,0,sig,0,sig1.length);
        System.arraycopy(sig2,0,sig,sig1.length,sig2.length);
        return sig;

    }



    /**
     * generate down chirp signal
     * @param fs - sampliong rate
     * @param t - duration
     * @param b - bandwidth
     * @param f - fmax
     * @return audio samples in short format
     */
    public static short[] downChirpGenerator(int fs, float t, int b, int f){
        float[] x = chirpGenerator(fs, t, b, f, 1);
        waveformReshaping(x);
        short[] samples = new short[x.length];
        for(int i = 0; i < x.length; i++){
            samples[i] = (short)(32767 * x[i]);
        }
        return samples;
    }

    /**
     *  generate the chirp signal in short format
     * @param fs - sampling rate
     * @param t - duration of the chirp signal
     * @param b - bandwidth of the chirp signal
     * @param f - fmin for up chirp signal and fmax for the down chirp signal
     * @param type - 0 for up chirp signal and 1 for down chirp signal
     * @return chirp samples in float format
     */
    public static float[] chirpGenerator(int fs, float t, int b, int f, int type){
        int n = (int)(fs * t);
        float[] samples = new float[n];
        if( type == 0 ) {
            for (int i = 0; i < n; i++) {
                samples[i] = (float) Math.cos(2 * Math.PI * f * i / fs + Math.PI * b * i * i / t / fs / fs);
            }
        }else{
            for (int i = 0; i < n; i++) {
                samples[i] = (float) Math.cos(2 * Math.PI * f * i / fs - Math.PI * b * i * i / t / fs / fs);
            }
        }
        return samples;
    }

    /**
     * waveform reshaping to mitigate the audible noise
     * slowly ramping up the amplitude of the 100 samples and reversely perform it on the last 100 samples
     * @param samples
     */
    public static void waveformReshaping(float samples[]){
        int k = 100;
        float coefficients = 1.0f / k;

        for(int i = 0; i < k; i++){
            samples[i] = samples[i] * (i + 1) / k;
            samples[samples.length - 1 - i] = samples[samples.length - 1 - i] * (i + 1) / k;
        }
    }
}
