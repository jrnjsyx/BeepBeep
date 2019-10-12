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
    private int validBufferLenght = 0;
    private int minBufferSize = 0;
    private short[] buffer;
    private short[] buffer0;
    private AudioTrack audiotrack;
    private boolean sendBufferSig;

    private final String TAG = "PlayThread";

    public PlayThread(short[] buffer){

        // get the minimum buffer size
        minBufferSize = AudioTrack.getMinBufferSize(
                FlagVar.Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int len = buffer.length>minBufferSize?buffer.length:minBufferSize;
        validBufferLenght = FlagVar.minPlayBufferSize;
        while(validBufferLenght < len){
            validBufferLenght = validBufferLenght * 2;
        }
        this.buffer = new short[validBufferLenght];
        buffer0 = new short[validBufferLenght];
        for(int i=0;i<buffer0.length;i++){
            buffer0[i] = 0;
        }
        System.arraycopy(buffer,0,this.buffer,0,buffer.length);
    }

    public int getBufferSize(){
        return validBufferLenght;
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
            if(sendBufferSig){
                sendBufferSig = false;
                writeFully(buffer);
            }else{
                sendBufferSig = true;
                writeFully(buffer0);
            }

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
            offset += audiotrack.write(data,offset,data.length);
        }
    }

    /*
    shut down the thread
     */
    public void stopRunning(){
        isRunning = false;
    }
}
