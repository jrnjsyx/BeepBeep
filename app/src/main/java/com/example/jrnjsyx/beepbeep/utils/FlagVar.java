package com.example.jrnjsyx.beepbeep.utils;

import java.math.BigDecimal;

public interface FlagVar {

    public int Fs = 48000;

    public int bufferSize = 20480;
    public float TPreamble = 0.04f;
    public int BPreamble = 4000;
    public int Fmin = 17500;
    public int Fmax = 21500;



    int LPreamble = (int)(new BigDecimal(TPreamble*Fs).setScale(0, BigDecimal.ROUND_HALF_UP).floatValue());

    int startBeforeMaxCorr = 200;
    int endBeforeMaxCorr = 0;
    int cSound = 34000;
    float cSample = (float)cSound/Fs/2;

    int B_BUTTON_ENABLE = 1;
    int A_BUTTON_ENABLE = 2;

    int MIC_UP = 1;
    int MIC_DOWN = 2;
    int DETECT_TYPE1 = 1;
    int DETECT_TYPE2 = 2;
    int DETECT_TYPE3 = 3;
    int DETECT_TYPE4 = 4;
    int speedOffset = 0;
    int micUsed = MIC_UP;
    int preambleDetectionType = DETECT_TYPE1;

    float maxAvgRatioThreshold = 8f;
    float ratioThreshold = 9;
    float ratioAvailableThreshold = 0.4f;
}
