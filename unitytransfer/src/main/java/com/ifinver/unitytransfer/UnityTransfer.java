package com.ifinver.unitytransfer;

/**
 * Created by iFinVer on 2016/12/13.
 * ilzq@foxmail.com
 */

public class UnityTransfer {
    static {
        System.loadLibrary("unity-transfer-lib");
    }
    public static native void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean mirror, long facePtr);

    public static native void onMonalisaData(long msgPtr);
}
