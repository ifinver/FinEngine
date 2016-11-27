package com.ifinver.myopengles.sdk;

import android.view.Surface;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class FinRender {
    static {
        System.loadLibrary("fin-render-lib");
    }

    /**
     * @return 0 means failed
     */
    public static native long createGLContext(Surface surface, int imageFormat, int mFilterType);

    public static native void releaseGLContext(long nativeContext);

    public static native void renderOnContext(long nativeGlContext, byte[] data,int frameDegree, int frameWidth, int frameHeight);
}
