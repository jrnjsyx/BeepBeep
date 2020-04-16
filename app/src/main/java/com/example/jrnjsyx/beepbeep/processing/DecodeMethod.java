package com.example.jrnjsyx.beepbeep.processing;

public interface DecodeMethod {
    void stopRunning();
    void fillSamples(short[] data);
}
