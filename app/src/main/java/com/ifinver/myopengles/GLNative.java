package com.ifinver.myopengles;

/**
 * Created by iFinVer on 2016/11/12.
 * ilzq@foxmail.com
 */

public class GLNative {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("gl-native-lib");
        System.loadLibrary("opencv_java3");
    }

    public static native void init(int width,int height);
    public static native void onDraw();

    public static native int[] getGrayImage(int[] pixels, int w, int h);

    public static native void processFrame(int length, byte[] frameBuffer);
    public static native long processFrameMat(int length, byte[] frameBuffer);

    public static native void initProcesser(int mFrameWidth, int mFrameHeight, int imageFormat);

    public static native void releaseProcesser();
}
