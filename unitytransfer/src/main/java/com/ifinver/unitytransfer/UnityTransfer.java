package com.ifinver.unitytransfer;

/**
 * Created by iFinVer on 2016/12/13.
 * ilzq@foxmail.com
 */

public class UnityTransfer {
    public static final int RECARD_ACTION_NONE = -1;
    public static final int RECARD_ACTION_START = 0;
    public static final int RECARD_ACTION_STOP = 1;
    public static final int RECARD_ACTION_CANCEL = 2;

    static {
        System.loadLibrary("unity-transfer-lib");
    }
    public static native void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean mirror, long facePtr);

    public static native void onMonalisaData(long msgPtr);

    public static native void initAssetsLoader(String json);

    public static native void loadAsset(int assetId);

    public static native void setRecordAction(int recordAction);

    public static native void enableBlur(boolean isEnable);

    public static native void pauseAssetAudio(boolean isPause);

    public static native void cleanUpAssetsCache();
}
