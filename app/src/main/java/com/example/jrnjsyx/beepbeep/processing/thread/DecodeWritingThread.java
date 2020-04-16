package com.example.jrnjsyx.beepbeep.processing.thread;

import com.example.jrnjsyx.beepbeep.physical.thread.RecordThreadTemplate;
import com.example.jrnjsyx.beepbeep.utils.Common;

public class DecodeWritingThread extends DecodeThreadTemplate {

    private RecordThreadTemplate recordThread;
    private int bufferCnt;
    short[] buffer;
    int perBufferSize;
    int cnt = 0;
    String fileName = "savedData_record";
    public DecodeWritingThread(int bufferCnt, int perBufferSize){
        this.bufferCnt = bufferCnt;
        this.recordThread = recordThread;
        this.perBufferSize = perBufferSize;
        buffer = new short[perBufferSize*bufferCnt];
        Common.createEmptyFile(fileName);

    }
    public void run() {
        try {
            while (isRunning) {
                if (samplesList.size() >= bufferCnt) {
                    writeOnce(bufferCnt);
                }

            }
            writeOnce(samplesList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeOnce(int n){
        synchronized (samplesList) {
            for (int i = 0; i < n; i++) {
                System.arraycopy(samplesList.get(i), 0, buffer, i * perBufferSize, perBufferSize);
            }
            for (int i = 0;i < n;i++) {
                samplesList.remove(0);
            }
        }
        this.cnt++;
        Common.println("writeOnce cnt:"+this.cnt);
        Common.saveShorts(buffer,fileName,true);
    }

    protected void onThreadStop(){
        Common.println("DecodeWritingThread onThreadStop().");

    }


}
