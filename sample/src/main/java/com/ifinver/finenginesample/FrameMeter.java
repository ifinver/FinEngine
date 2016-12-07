package com.ifinver.finenginesample;

import android.os.SystemClock;

import java.text.DecimalFormat;

/**
 * Created by iFinVer on 2016/11/30.
 * ilzq@foxmail.com
 */

public class FrameMeter {
    private long mFrameCount = 0;
    private long mStartTime = 0;
    private double fps = 0;
    private DecimalFormat df;

    public FrameMeter(){
        df = new DecimalFormat("#0.00");
    }

    public void meter(){
        mFrameCount++;
        if (mStartTime == 0) {
            mStartTime = SystemClock.elapsedRealtime();
        } else {
            long spend = SystemClock.elapsedRealtime() - mStartTime;
            fps = mFrameCount * 1000.0 / spend;
        }
    }

    public String getFPSString(){
        return df.format(fps);
    }
}
