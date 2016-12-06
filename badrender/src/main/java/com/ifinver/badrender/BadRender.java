package com.ifinver.badrender;

import android.view.Surface;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class BadRender {
    static {
        System.loadLibrary("bad-render-lib");
    }

    public static native void createGLContext(Surface surface);

    public static native void release();

    public static native void render();
}
