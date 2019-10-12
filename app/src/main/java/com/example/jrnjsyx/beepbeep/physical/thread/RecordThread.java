package com.example.jrnjsyx.beepbeep.physical.thread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;

public class RecordThread extends Thread {
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_STEREO;

    private AudioRecord audioRecord;
    RecordingCallback recordingCallback;
    private Boolean isRunning = true;
    private int bufferSize;

    public RecordThread(RecordingCallback recordingCallback) {
        this.recordingCallback = recordingCallback;
        bufferSize = getBufferSize();
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FlagVar.Fs, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING, bufferSize);
    }

    public interface RecordingCallback {
        void onDataReady(short[] data, int bytelen);
    }

    private void readFully(AudioRecord recorder, short[] data, int off, int length) {
        int read;
        while (length > 0) {
            read = recorder.read(data, off, length);
            length -= read;
            off += read;
        }
    }

    public void run(){
        int bufferSize = getBufferSize();

        try {
            audioRecord.startRecording();

            short recordBuffer[] = new short[bufferSize];
            while(isRunning) {
                readFully(audioRecord,recordBuffer,0,bufferSize);
//                Common.println("bufferSize:"+bufferSize+" recordBuffer.length:"+recordBuffer.length+" val:"+recordBuffer[4097]+" time:"+(new Date().getTime()));

                recordingCallback.onDataReady(recordBuffer,bufferSize/2);
            }
        } finally {
            audioRecord.release();
        }
    }

    public static int getBufferSize(){
        int bufferSize = AudioRecord.getMinBufferSize(FlagVar.Fs, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING);

        int size = FlagVar.minRecordBufferSize*2;
        while(size < bufferSize){
            size = size * 2;
        }
        bufferSize = size;

        return bufferSize;
    }

    public void stopRunning(){
        synchronized (isRunning) {
            isRunning = false;
        }
    }
}
