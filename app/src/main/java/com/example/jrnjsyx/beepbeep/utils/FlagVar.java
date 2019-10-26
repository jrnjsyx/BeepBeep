package com.example.jrnjsyx.beepbeep.utils;

import java.math.BigDecimal;

public class FlagVar {

    public static int Fs = 48000;

    public static int bufferSize = 20480;
    public static float tChrip = 0.04f;
    public static int bChirp = 2000;
    public static int bChirp2 = 1500;
    public static int lowFStart = 16000;
    public static int highFStart = 20000;
    public static int chirpInterval = 4000*3;
    public static int playBufferSize = chirpInterval;
    public static int recordBufferSize = chirpInterval;
    public static int[] frequencies = {14500,14600,14700,14800,14900,15000,15100,15200,15300,15400};
    //该设置效果较好
//    public static int lowFStart = 11000;
//    public static int highFStart = 13000;
//    public static int playBufferSize = 4096*2;
//    public static int recordBufferSize = 4096*2;



    public static String connectionStartStr = "connectionStart";
    public static String connectionThreadEndStr = "connectionEnd";
    public static String aModeStr = "aMode";
    public static String bModeStr = "bMode";
    public static String rangingStartStr = "rangingStartStr";
    public static String rangingEndStr = "rangingEnd";
    public static String lowPosStr = "lowPosStr";
    public static String skipStr = "skipStr";
    public static String highPosStr = "highPosStr";
    public static String recordStr = "recordStr";
    public static String basePosStr = "basePosStr";

    public static int previousIndexThreshold = 300;
    public static int twiceIndexThreshold = 200;

    public static int NETWORK_TEXT = 0;
    public static int DISTANCE_TEXT = 1;
    public static int DEBUG_TEXT = 2;
    public static int minPosDataCapacity = 9;
    public static int adjustThreshold = 4;
    public static int diffThreshold = 1000;
    public static int BEEP_BEEP_MODE = 0;
    public static int MY_ORIGINAL_MODE = 1;
    //被证明效果不太好
    public static int MY_NEW_MODE = 2;
    public static int currentRangingMode = MY_ORIGINAL_MODE;

    public static int playThreadDelay = 10;

    public static int lChirp = (int)(new BigDecimal(tChrip *Fs).setScale(0, BigDecimal.ROUND_HALF_UP).floatValue());
    public static int lSine = 1000;

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
