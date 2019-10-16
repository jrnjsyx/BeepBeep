package com.example.jrnjsyx.beepbeep.physical.thread;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.util.Arrays;

public class PlayThread extends Thread {

    /*
    This thread is used to play audio samples in PCM format
     */

    private boolean isRunning = true;
    private static int minBufferSize = 0;
    private short[] buffer = new short[FlagVar.playBufferSize];
    private AudioTrack audiotrack;
    private Boolean needAdjust = false;
    private short[] adjustSamples;

    private final String TAG = "PlayThread";
    static {
        minBufferSize = AudioTrack.getMinBufferSize(
                FlagVar.Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if(minBufferSize > FlagVar.playBufferSize){
            throw new RuntimeException("playBufferSize should be larger than minBufferSize.");
        }
    }

    public PlayThread(short[] buffer){

        // get the minimum buffer size

        if(buffer.length > FlagVar.playBufferSize){
            throw new RuntimeException("playBufferSize should be larger than buffer length.");
        }
        for(int i=0;i<this.buffer.length;i++){
            this.buffer[i] = 0;
        }
        System.arraycopy(buffer,0,this.buffer,0,buffer.length);
    }

    public void adjustSample(int cnt){
        synchronized (needAdjust) {
            needAdjust = true;
            adjustSamples = new short[cnt];
            for(int i=0;i<adjustSamples.length;i++){
                adjustSamples[i] = 0;
            }
        }
    }




    /**
     * run process, always stay alive until the end of the program
     */
    public void run(){


        // get the minimum buffer size
        // initialize the audiotrack
        audiotrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                FlagVar.Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);

        audiotrack.play();
        while (isRunning){
            synchronized (needAdjust){
                if(needAdjust){
                    needAdjust = false;
                    writeFully(adjustSamples);
                }
            }
            writeFully(buffer);

        }

        try{
            audiotrack.stop();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeFully(short[] data){
        int offset = 0;
        while (offset < data.length){
            int write;
            if(offset+minBufferSize > data.length){
                write = audiotrack.write(data, offset, data.length-offset);
            }else {
                write = audiotrack.write(data, offset, minBufferSize);
            }
            offset += write;
//            Common.println("audiotrack offset:"+offset+"  write:"+write+" length:"+data.length+" minBufferSize:"+minBufferSize);
        }
    }

    /*
    shut down the thread
     */
    public void stopRunning(){
        isRunning = false;
    }
}
