package com.ifinver.myopengles;

import android.view.Surface;

/**
 * Created by iFinVer on 2016/11/12.
 * ilzq@foxmail.com
 */

public class GLNative {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("gl-native-lib");
    }

    public static native long createGLContext(Surface surface);

    public static native void releaseGLContext(long nativeContext);

    public static native void renderOnContext(long nativeGlContext, byte[] data, int frameWidth, int frameHeight, int imageFormat);
}
