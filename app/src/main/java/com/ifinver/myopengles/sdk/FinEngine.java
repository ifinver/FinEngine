package com.ifinver.myopengles.sdk;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class FinEngine {
    static {
        System.loadLibrary("fin-engine-lib");
    }

    public static native long startEngine();

    public static native void stopEngine(long engine);
}
