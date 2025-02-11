package com.nnxfb.teeter;

import android.os.SystemClock;

/* loaded from: classes3.dex */
class CS {
    private static boolean sIsPaused = false;
    private static long sLevelStartTime = 0;
    private static long sLevelTime = 0;
    private static long sTotalTime = 0;
    private static int sLevelAttempt = 0;
    private static int sTotalAttempt = 1;

    CS() {
    }

    public static void sfnBeginLevel() {
        sLevelStartTime = SystemClock.uptimeMillis();
        if (!sIsPaused) {
            sLevelAttempt = 0;
            sLevelTime = 0L;
        } else {
            sIsPaused = false;
        }
    }

    public static void sfnFallInHole() {
        sLevelAttempt++;
    }

    public static void sfnEndLevel() {
        sLevelTime += SystemClock.uptimeMillis() - sLevelStartTime;
        sTotalTime += sLevelTime;
        sLevelAttempt++;
        sTotalAttempt += sLevelAttempt - 1;
        sIsPaused = false;
    }

    public static void sfnPauseRecord() {
        sIsPaused = true;
        sLevelTime += SystemClock.uptimeMillis() - sLevelStartTime;
        sLevelStartTime = SystemClock.uptimeMillis();
    }

    public static void sfnReset() {
        sIsPaused = false;
        sLevelStartTime = 0L;
        sLevelTime = 0L;
        sTotalTime = 0L;
        sLevelAttempt = 0;
        sTotalAttempt = 1;
    }

    public static boolean sfnGetPausedState() {
        return sIsPaused;
    }

    public static long sfnGetLevelTime() {
        return sLevelTime;
    }

    public static int sfnGetLevelAttempt() {
        return sLevelAttempt;
    }

    public static long sfnGetTotalTime() {
        return sTotalTime;
    }

    public static int sfnGetTotalAttempt() {
        return sTotalAttempt;
    }

    public static void sfnSetPausedState(boolean b) {
        sIsPaused = b;
    }

    public static void sfnSetLevelTime(long levelTime) {
        sLevelTime = levelTime;
    }

    public static void sfnSetLevelAttempt(int levelAttempt) {
        sLevelAttempt = levelAttempt;
    }

    public static void sfnSetTotalTime(long totalTime) {
        sTotalTime = totalTime;
    }

    public static void sfnSetTotalAttempt(int totalAttempt) {
        sTotalAttempt = totalAttempt;
    }
}
