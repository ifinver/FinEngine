package com.ifinver.myopengles.sdk;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

/**
 * Created by iFinVer on 2016/11/21.
 * ilzq@foxmail.com
 */

public class TextureRenderer implements TextureView.SurfaceTextureListener {
    private static final String TAG = "TextureRenderer";

    public static final int FILTER_TYPE_NORMAL = 0;
    public static final int FILTER_TYPE_CYAN = 1;
    public static final int FILTER_TYPE_FISH_EYE = 2;
    public static final int FILTER_TYPE_GREY_SCALE = 3;
    public static final int FILTER_TYPE_NEGATIVE_COLOR = 4;

    private RenderThread mRenderThread;
    private int mImageFormat;
    private Surface mSurface;
    private int mFilterType;

    public TextureRenderer(int imageFormat){
        this(imageFormat,FILTER_TYPE_NORMAL);
    }

    public TextureRenderer(int imageFormat,int filterType) {
        this.mSurface = null;
        this.mRenderThread = null;
        this.mFilterType = filterType;
        this.mImageFormat = imageFormat;
    }

    private void release() {
        if (mRenderThread != null) {
            mRenderThread.quit();
            mRenderThread = null;
        }
    }

    public void onVideoBuffer(byte[] data,int frameDegree, int frameWidth, int frameHeight) {
        if (mRenderThread != null) {
            mRenderThread.notifyWithBuffer(data,frameDegree, frameWidth, frameHeight);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        mSurface = new Surface(surface);

        if ( mRenderThread == null ) {
            mRenderThread = new RenderThread(mSurface, mImageFormat,mFilterType);
            mRenderThread.start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        //do nothing.
        Log.d(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        mSurface = null;
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //do nothing.
    }

    private static class RenderThread extends Thread {
        private static final String TAG = "RenderThread";
        private final int mImageFormat;
        boolean quit = false;
        Surface mSurface;
        private long glContext;
        private int mFrameWidth;
        private int mFrameHeight;
        private byte[] mData;
        private int mFilterType;
        private int mFrameDegree;

        RenderThread(Surface surface,int mImageFormat, int mFilterType) {
            this.mSurface = surface;
            this.mImageFormat = mImageFormat;
            this.mFilterType = mFilterType;
        }

        public void notifyWithBuffer(byte[] data,int frameDegree, int frameWidth, int frameHeight) {
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
            this.mFrameDegree = frameDegree;
            this.mData = data;
            //wakeup
            synchronized (this) {
                notify();
            }
        }

        private void onDrawFrame() {
            if (glContext != 0 && mData != null) {
                // TODO: 2016/11/25 在这里进行frameAvailableSoon
//                long spend = SystemClock.elapsedRealtime();
                GLNative.renderOnContext(glContext, mData,mFrameDegree, mFrameWidth, mFrameHeight);
//                spend = SystemClock.elapsedRealtime() - spend;
//                Log.d(TAG, "渲染一帧:" + spend);
            }
        }


    public void quit() {
        Log.d(TAG, "开始退出渲染线程");
        quit = true;
        interrupt();
    }

    private void initGL() {
        glContext = GLNative.createGLContext(mSurface, mImageFormat,mFilterType);
        if (glContext == 0) {
            Log.e(TAG, "渲染上下文创建失败！");
        } else {
            Log.d(TAG, "渲染初始化成功");
        }
    }

    private void destroyGL() {
        if (glContext != 0) {
            GLNative.releaseGLContext(glContext);
        }
        Log.d(TAG, "渲染线程已退出");
    }

    @Override
    public void run() {
        initGL();

        while (!quit) {
            synchronized (this) {
                onDrawFrame();

                try {
                    wait();
                } catch (InterruptedException e) {
                    quit = true;
                }
            }
        }

        destroyGL();
    }
}
}
