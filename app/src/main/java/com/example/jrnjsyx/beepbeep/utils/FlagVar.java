package com.example.jrnjsyx.beepbeep.utils;

import java.math.BigDecimal;

public class FlagVar {

    public static int Fs = 48000;

    public static int bufferSize = 20480;
    public static float tChrip = 0.04f;
    public static int bChirp = 2000;
//    public static int lowFStart = 15500;
//    public static int highFStart = 17500;
    public static int lowFStart = 11000;
    public static int highFStart = 13000;

    public static String connectionStartStr = "start";
    public static String connectionThreadEndStr = "end";
    public static String aModeStr = "aMode";
    public static String bModeStr = "bMode";

    public static int lChirp = (int)(new BigDecimal(tChrip *Fs).setScale(0, BigDecimal.ROUND_HALF_UP).floatValue());

    public static int startBeforeMaxCorr = 200;
    public static int endBeforeMaxCorr = 0;
    public static int cSound = 34000;
    public static float cSample = (float)cSound/Fs/2;

    public static int B_BUTTON_ENABLE = 1;
    public static int A_BUTTON_ENABLE = 2;

    public static int speedOffset = 0;

    public static float maxAvgRatioThreshold = 6f;
    public static float ratioThreshold = 9;
    public static float ratioAvailableThreshold = 0.4f;

    public static int PORT = 34567;
}
