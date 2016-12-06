package com.ifinver.finengine;

import android.view.Surface;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class FinEngine {

    static {
        System.loadLibrary("fin-engine-lib");
    }

    public static final int FILTER_TYPE_NORMAL = 0;
    public static final int FILTER_TYPE_CYAN = 1;
    public static final int FILTER_TYPE_FISH_EYE = 2;
    public static final int FILTER_TYPE_GREY_SCALE = 3;
    public static final int FILTER_TYPE_NEGATIVE_COLOR = 4;

    /**
     * @return 0 means failed
     */
    public static native boolean init(Surface surface);

    public static native void release();

    public static native void render(byte[] data, int frameWidth, int frameHeight, int degree, boolean mirror);
}
