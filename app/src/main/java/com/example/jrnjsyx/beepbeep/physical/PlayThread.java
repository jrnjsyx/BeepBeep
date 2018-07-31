package com.example.jrnjsyx.beepbeep.physical;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.example.jrnjsyx.beepbeep.processing.Decoder;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

import java.util.Arrays;

public class PlayThread extends Thread implements FlagVar {

    /*
    This thread is used to play audio samples in PCM format
     */

    private boolean isRunning = true;
    private boolean isBufferReady = false;
    private int validBufferLenght = 0;
    private int minBufferSize = 0;
    private short[] buffer;

    private final String TAG = "PlayThread";

    public PlayThread(){
    // init the data
        // create a large buffer to store the waveform samples
        buffer = new short[FlagVar.bufferSize];
    }

    /**
     * fill the buffer to the audiotrack and play
     * @param data
     */
    public void fillBufferAndPlay(short[] data){
        // firt clear the buffer
        Arrays.fill(buffer, (short)(0));
        // copy short samples to the buffer
        System.arraycopy(data, 0, buffer, 0, data.length);

        // determine the write() function length
        if(data.length > minBufferSize){
            validBufferLenght = data.length;
        }else {
            validBufferLenght = minBufferSize;
        }
        synchronized (this){
            isBufferReady = true;
        }
    }

    /**
     * run process, always stay alive until the end of the program
     */
    public void run(){

        AudioTrack audiotrack;
        // get the minimum buffer size
        minBufferSize = AudioTrack.getMinBufferSize(
                FlagVar.Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        // initialize the audiotrack
        audiotrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                FlagVar.Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);

        while (isRunning){
            if(isBufferReady){
                isBufferReady = false;
                audiotrack.play();
                audiotrack.write(buffer,0,validBufferLenght);
            }
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
    public void close(){
        isRunning = false;
    }

    public void omitChirpSignal() {
        fillBufferAndPlay(Decoder.upPreamble);
    }
}
