package com.ifinver.finengine;

/**
 * Created by iFinVer on 2016/12/16.
 * ilzq@foxmail.com
 */

public class FinCv {
    static {
        System.loadLibrary("fin-engine-lib");
    }

    public static native int[] BGRA2Grey(int[] data,int width,int height);
}
