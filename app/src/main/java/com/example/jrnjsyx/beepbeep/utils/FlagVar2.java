package com.example.jrnjsyx.beepbeep.utils;

import java.math.BigDecimal;

public class FlagVar2 {

    public static int Fs = 48000;

    public static float tChrip = 0.04f;
    public static int bChirp = 2000;
    public static int lowFStart = 15500;
    public static int lowFEnd = lowFStart+bChirp;
    public static int highFEnd = 21000;
    public static int bChirp2 = highFEnd *bChirp/(bChirp+lowFStart);
    public static int highFStart = highFEnd-bChirp2;
    public static int spaceF = 100;
    public static int chirpInterval = 3840;
    public static int chirpIntervalTime = chirpInterval/Fs;
    public static int playBufferSize = chirpInterval;
    public static int recordBufferSize = chirpInterval;
    public static int startBeforeMaxCorr = 200;
    public static int lChirp = (int)(new BigDecimal(tChrip *Fs).setScale(0, BigDecimal.ROUND_HALF_UP).floatValue());
    public static int cSound = 34000;
    public static float cSample = (float)cSound/Fs/2;
}
