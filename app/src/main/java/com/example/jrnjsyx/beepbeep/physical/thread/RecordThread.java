package com.example.jrnjsyx.beepbeep.physical.thread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.jrnjsyx.beepbeep.processing.thread.DecodeThread;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

public class RecordThread extends Thread {
    private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
//    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_STEREO;
    private static int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;

    private static int minBufferSize;
    private static int recordBufferSize;

    private static short[] buffer;

    static{
        if(RECORDER_CHANNELS_IN == AudioFormat.CHANNEL_IN_STEREO) {
            recordBufferSize = FlagVar.recordBufferSize*2;
        }
        else if(RECORDER_CHANNELS_IN == AudioFormat.CHANNEL_IN_MONO){
            recordBufferSize = FlagVar.recordBufferSize;
        }
        buffer = new short[recordBufferSize];
        minBufferSize = AudioRecord.getMinBufferSize(FlagVar.Fs, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING);
        if(minBufferSize > recordBufferSize){
            throw new RuntimeException("recordBufferSize should be larger than minBufferSize.");
        }
    }

    private AudioRecord audioRecord;
    private Boolean isRunning = true;
    private DecodeThread decodeThread;

    public RecordThread(DecodeThread decodeThread) {
        if(decodeThread == null){
            throw new RuntimeException("decodeThread should not be null.");
        }
        else{
            this.decodeThread = decodeThread;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FlagVar.Fs, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING, minBufferSize);
    }



    private void readFully(short[] data){
        int offset = 0;
        while (offset < data.length){
            int read;
            if(offset+minBufferSize > data.length){
                read = audioRecord.read(data, offset, data.length-offset);
            }else {
                read = audioRecord.read(data, offset, minBufferSize);
            }
            offset += read;
//            Common.println("audiorecord offset:"+offset+"  write:"+read+" length:"+data.length+" minBufferSize:"+minBufferSize);
        }
    }
//    private void readFully(AudioRecord recorder, short[] data, int off, int length) {
//        int read;
//        while (length > 0) {
//            read = recorder.read(data, off, length);
//            length -= read;
//            off += read;
//        }
//    }

    public void run(){

        try {
            audioRecord.startRecording();

            while(isRunning) {
//                readFully(audioRecord,recordBuffer,0,bufferSize);
//                Common.println("bufferSize:"+bufferSize+" recordBuffer.length:"+recordBuffer.length+" val:"+recordBuffer[4097]+" time:"+(new Date().getTime()));
                readFully(buffer);
                onDataReady(buffer);
            }
        } finally {
            audioRecord.release();
        }
    }


    public void stopRunning(){
        synchronized (isRunning) {
            isRunning = false;
        }
    }

    public void onDataReady(short[] data) {

//        Common.println("onDataReady");
        if(RECORDER_CHANNELS_IN == AudioFormat.CHANNEL_IN_STEREO) {
            int len = data.length/2;
            short[] data1 = new short[len];
            short[] data2 = new short[len];
            for (int i = 0; i < len; i++) {
                data1[i] = data[2 * i];
                data2[i] = data[2 * i + 1];
            }
            decodeThread.fillSamples(data2);
        }
        else if(RECORDER_CHANNELS_IN == AudioFormat.CHANNEL_IN_MONO){
            decodeThread.fillSamples(data);
        }
    }
}
