package com.ifinver.finrender;

import android.view.Surface;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class FinRender {

    public static final int FILTER_TYPE_NORMAL = 0;
    public static final int FILTER_TYPE_CYAN = 1;
    public static final int FILTER_TYPE_FISH_EYE = 2;
    public static final int FILTER_TYPE_GREY_SCALE = 3;
    public static final int FILTER_TYPE_NEGATIVE_COLOR = 4;

//    public static final int FORMAT_RGBA = 0x201;
    public static final int FORMAT_NV21 = 0x202;
    static {
        System.loadLibrary("fin-render-lib");
    }

    /**
     * @return 0 means failed
     */
    public static long createGLContext(Surface surface,int frameFormat){
        return createGLContext(surface,true,frameFormat);
    }

    public static native long createGLContext(Surface surface,boolean isSurfaceThreadExclusive, int frameFormat);

    public static native void releaseGLContext(long nativeContext);

    public static native void renderOnContext(long nativeGlContext, byte[] data,int frameWidth, int frameHeight,int degree,boolean mirror);
}
