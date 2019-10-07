package com.example.jrnjsyx.beepbeep.physical.thread;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.util.Arrays;

public class PlayThread extends Thread {

    /*
    This thread is used to play audio samples in PCM format
     */

    private boolean isRunning = true;
    private int validBufferLenght = 0;
    private int minBufferSize = 0;
    private short[] buffer;
    private AudioTrack audiotrack;

    private final String TAG = "PlayThread";

    public PlayThread(short[] buffer){

        // get the minimum buffer size
        minBufferSize = AudioTrack.getMinBufferSize(
                FlagVar.Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int len = buffer.length>minBufferSize?buffer.length:minBufferSize;
        validBufferLenght = 4096;
        while(validBufferLenght < len){
            validBufferLenght = validBufferLenght * 2;
        }
        this.buffer = new short[validBufferLenght];
        System.arraycopy(buffer,0,this.buffer,0,buffer.length);
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
            audiotrack.write(buffer,0,buffer.length);
        }

        try{
            audiotrack.stop();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    shut down the thread
     */
    public void stopRunning(){
        isRunning = false;
    }
}
