package com.nnxfb.teeter;

/* loaded from: classes3.dex */
final class CTime {
    private final int H2S = 3600;
    private final int M2S = 60;
    private int mHours;
    private int mMinutes;
    private int mSeconds;

    CTime(long miniseconds) {
        long sec = miniseconds / 1000;
        this.mHours = (int) (sec / 3600);
        this.mMinutes = (int) ((sec - (this.mHours * 3600)) / 60);
        this.mSeconds = (int) ((sec - (this.mHours * 3600)) - (this.mMinutes * 60));
    }

    public int fnGetHours() {
        return this.mHours;
    }

    public int fnGetMinutes() {
        return this.mMinutes;
    }

    public int fnGetSeconds() {
        return this.mSeconds;
    }
}
