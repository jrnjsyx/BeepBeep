package com.example.jrnjsyx.beepbeep.utils;

import org.ejml.data.DMatrixRMaj;

import java.math.BigDecimal;

public class FlagVar {

    public static int Fs = 48000;

    public static int bufferSize = 20480;
    public static float tChrip = 0.04f;
    public static int bChirp = 2000;
    public static int lowFStart = 16000;
    public static int highFStart = 20500;
    public static int bChirp2 = highFStart*bChirp/(bChirp+lowFStart);
    public static int chirpInterval = 4000*3;
    public static int chirpIntervalTime = chirpInterval/Fs;
    public static int playBufferSize = chirpInterval;
    public static int recordBufferSize = chirpInterval;
    public static int[] frequencies = {14500,14600,14700,14800,14900,15000,15100,15200,15300,15400};
//    public static int[] frequencies = {12500,12600,12700,12800,12900,13000,13100,13200,13300,13400};
//    public static int[] frequencies = {12500,12700,12900,13100,13300};
//    public static int[] frequencies = {8500,8700,8900,9100,9300};
//    public static int[] frequencies = {8500,8600,8700,8800,8900,9000,9100,9200,9300,9400};
    //该设置效果较好
//    public static int lowFStart = 11000;
//    public static int highFStart = 13000;
//    public static int playBufferSize = 4096*2;
//    public static int recordBufferSize = 4096*2;


    public static String unhandledSpeedStr = "unhandledSpeed";
    public static String speedStr = "speed";
    public static String unhandledDistanceStr = "unhandledDistance";
    public static String distanceStr = "distance";
    public static String unhandledDistanceCntStr = "unhandledDistanceCnt";
    public static String connectionStartStr = "connectionStart";
    public static String connectionThreadEndStr = "connectionEnd";
    public static String aModeStr = "aMode";
    public static String bModeStr = "bMode";
    public static String rangingStartStr = "rangingStart";
    public static String rangingEndStr = "rangingEnd";
    public static String lowPosStr = "lowPos";
    public static String skipStr = "skip";
    public static String highPosStr = "highPos";
    public static String recordStr = "record";

    public static int NETWORK_TEXT = 0;
    public static int MAIN_TEXT = 1;
    public static int DEBUG_TEXT = 2;

    public static int BEEP_BEEP_MODE = 0;
    //abandoned
    public static int MY_ORIGINAL_MODE = 1;
    public static int MY_NEW_MODE = 2;
    public static int currentRangingMode = MY_NEW_MODE;

    public static int minPosDataCapacity = 9;
    public static int adjustThreshold = 4;
    public static int diffThreshold = 1000;
    public static int speedEstimateBufferLength = 65536;
    public static int speedEstimateRangeF = 40;
    public static int soundSpeed = 34000;
    public static int speedThreshold = 200;
    public static int distanceThreshold = 100;
    public static int playThreadDelay = 10;
    public static int lChirp = (int)(new BigDecimal(tChrip *Fs).setScale(0, BigDecimal.ROUND_HALF_UP).floatValue());
    public static int lSine = 1000;
    public static int startBeforeMaxCorr = 200;
    public static int endBeforeMaxCorr = 0;
    public static int cSound = 34000;
    public static float cSample = (float)cSound/Fs/2;
    public static float maxAvgRatioThreshold = 6f;
    public static float ratioThreshold = 9;
    public static float ratioAvailableThreshold = 0.4f;
    public static int PORT = 34567;



    public static DMatrixRMaj Q = new DMatrixRMaj(2,2);
    public static DMatrixRMaj R = new DMatrixRMaj(2,2);
    static {
        Q.set(0,0,10);
        Q.set(1,1,10);
        R.set(0,0,20);
        R.set(0,1,-10);
        R.set(1,0,-10);
        R.set(1,1,40);
    }

    final static public int SHOW_SETTING = 0;
    final static public int GRAPH_MODE = 1;
    final static public int STRING_MODE = 2;
    final static public int METRIC_SETTING = 3;
    final static public int METRIC_MODE = 4;
    final static public int SERIES_SETTING = 5;
    final static public int UNHANDLED_SERIES_MODE = 6;
    final static public int HANDLED_SERIES_MODE = 7;
    final static public int ALL_SERIES_MODE = 8;
    final static public int SELF_ADAPTION_SETTING = 9;
    final static public int SELF_ADAPTION_MODE = 10;
    final static public int FREE_MODE = 11;

    static public int currentSeriesMode = ALL_SERIES_MODE;
    static public int currentSelfAdaptionMode = FREE_MODE;

    static public int currentShowMode = STRING_MODE;
}
