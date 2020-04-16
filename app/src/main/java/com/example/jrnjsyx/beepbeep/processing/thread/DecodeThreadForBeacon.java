package com.example.jrnjsyx.beepbeep.processing.thread;

import com.example.jrnjsyx.beepbeep.physical.thread.PlayThreadTemplate;
import com.example.jrnjsyx.beepbeep.physical.thread.RecordThread;
import com.example.jrnjsyx.beepbeep.physical.thread.RecordThreadTemplate;
import com.example.jrnjsyx.beepbeep.processing.IndexMaxVarInfo;
import com.example.jrnjsyx.beepbeep.utils.Common;
import com.example.jrnjsyx.beepbeep.utils.FlagVar;
import com.example.jrnjsyx.beepbeep.utils.FlagVar2;
import com.example.jrnjsyx.beepbeep.utils.JniUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DecodeThreadForBeacon extends DecodeThreadTemplate{

    private boolean isLow;
    private short[] beacon = new short[FlagVar2.lChirp];


    public DecodeThreadStop decodeThreadStop;
    public DecodeThreadForBeacon(boolean isLow, DecodeThreadStop decodeThreadStop){
        this.isLow = isLow;
        this.decodeThreadStop = decodeThreadStop;
        initialize(FlagVar2.recordBufferSize);


    }


    public void run() {
        try {
            while (isRunning) {
                if (samplesList.size() >= 2) {
                    constructBuffer();
                    float[] fft = JniUtils.fft(normalization(buffer), chirpCorrLen);
                    if(isLow) {

                        IndexMaxVarInfo infoLow = getIndexMaxVarInfoFromFDomain2(fft, lowUpChirpFFT);
                        if(infoLow.isReferenceSignalExist){
                            System.arraycopy(buffer,infoLow.index,beacon,0,beacon.length);
                            stopRunning();
                        }
                    }
                    else{
                        IndexMaxVarInfo infoHigh = getIndexMaxVarInfoFromFDomain2(fft, highDownChirpFFT);
                        if(infoHigh.isReferenceSignalExist){
                            System.arraycopy(buffer,infoHigh.index,beacon,0,beacon.length);
                            stopRunning();
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void constructBuffer() {
        synchronized (samplesList) {

            System.arraycopy(samplesList.get(0), processBufferSize - FlagVar.lChirp - FlagVar.startBeforeMaxCorr, buffer,
                    0, FlagVar.lChirp + FlagVar.startBeforeMaxCorr);
            System.arraycopy(samplesList.get(1), 0, buffer, FlagVar.lChirp + FlagVar.startBeforeMaxCorr, processBufferSize);
            samplesList.remove(0);

        }
    }
    protected void onThreadStop() {
        Common.println("DecodeThreadForBeacon onThreadStop().");
        decodeThreadStop.stop();
        if(isLow){
            Common.saveShorts(beacon, "savedData_lowBeacon",false);
        }else{
            Common.saveShorts(beacon, "savedData_highBeacon",false);
        }


    }

    public short[] getBeacon(){
        return beacon;
    }

    public boolean getIsLow(){
        return isLow;
    }

    public interface DecodeThreadStop{
        void stop();
    }


}
