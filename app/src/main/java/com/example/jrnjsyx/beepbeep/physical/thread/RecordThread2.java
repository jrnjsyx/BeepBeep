package com.example.jrnjsyx.beepbeep.physical.thread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.jrnjsyx.beepbeep.processing.thread.DecodeThread;
import com.example.jrnjsyx.beepbeep.processing.thread.DecodeThreadTemplate;
import com.example.jrnjsyx.beepbeep.utils.FlagVar2;

public class RecordThread2 extends RecordThreadTemplate {
    private int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
//    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_STEREO;
    private int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;

    private int minBufferSize;
    private int recordBufferSize;
    private boolean isMinSizeLarger;
    private short[] buffer;

    {
        if(RECORDER_CHANNELS_IN == AudioFormat.CHANNEL_IN_STEREO) {
            recordBufferSize = FlagVar2.recordBufferSize*2;
        }
        else if(RECORDER_CHANNELS_IN == AudioFormat.CHANNEL_IN_MONO){
            recordBufferSize = FlagVar2.recordBufferSize;
        }
        buffer = new short[recordBufferSize];
        minBufferSize = AudioRecord.getMinBufferSize(FlagVar2.Fs, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING);
        isMinSizeLarger = minBufferSize>recordBufferSize;

    }

    private AudioRecord audioRecord;
    private DecodeThreadTemplate decodeThread;

    public RecordThread2(DecodeThreadTemplate decodeThread) {
        if(decodeThread == null){
            throw new RuntimeException("decodeThread should not be null.");
        }
        else{
            this.decodeThread = decodeThread;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FlagVar2.Fs, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING, minBufferSize);
    }



    private void readFully(){
        int offset = 0;
        while (offset < buffer.length){
            int read;
            if(offset+minBufferSize > buffer.length){
                read = audioRecord.read(buffer, offset, buffer.length-offset);
            }else {
                read = audioRecord.read(buffer, offset, minBufferSize);
            }
            offset += read;
        }
    }

    private void readFull2(){
        int read = audioRecord.read(buffer, 0, buffer.length);
    }

    public void run(){

        try {
            audioRecord.startRecording();

            while(isRunning) {
//                Common.println("bufferSize:"+bufferSize+" recordBuffer.length:"+recordBuffer.length+" val:"+recordBuffer[4097]+" time:"+(new Date().getTime()));
                if (isMinSizeLarger == false) {
                    readFully();
                }else{
                    readFull2();
                }
                onDataReady(buffer);
            }
        } finally {
            audioRecord.release();
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
//            System.out.println("samples[0]:"+data[0]);
        }
    }

    public int getBufferSize(){
        return FlagVar2.recordBufferSize;
    }
}
