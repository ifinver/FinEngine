package com.ifinver.finenginesample;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.ifinver.finrender.FinRender;

/**
 * Created by iFinVer on 2016/11/21.
 * ilzq@foxmail.com
 */

public class TextureRenderer implements TextureView.SurfaceTextureListener {
    private static final String TAG = "FinRender";


    private RenderThread mRenderThread;
    private Surface mSurface;
    private int mFrameFormat;
    private SurfaceTexture mSurfaceTexture;


    public TextureRenderer(int frameFormat) {
        this.mSurface = null;
        this.mRenderThread = null;
        this.mFrameFormat = frameFormat;
    }

    private void release() {
        if (mRenderThread != null) {
            mRenderThread.quit();
            mRenderThread = null;
        }
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight,int degree,boolean isFrontCamera) {
        if (mRenderThread != null) {
            mRenderThread.notifyWithBuffer(data, frameWidth, frameHeight,degree,isFrontCamera);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.mSurfaceTexture = surface;
        mSurface = new Surface(surface);

        if ( mRenderThread == null ) {
            mRenderThread = new RenderThread(mSurface, mFrameFormat);
            mRenderThread.start();
        }
    }

    public SurfaceTexture getSurfaceTexture(){
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        //do nothing.
        Log.d(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface = null;
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //do nothing.
    }

    private static class RenderThread extends Thread {
        boolean quit = false;
        Surface mSurface;
        private long mEngine;
        private int mFrameWidth;
        private int mFrameHeight;
        private byte[] mData;
        private int mFrameFormat;
        private int mDegree;
        private boolean isFrontCamera;

        RenderThread(Surface surface, int frameFormat) {
            this.mSurface = surface;
            this.mFrameFormat = frameFormat;
        }

        public void notifyWithBuffer(byte[] data, int frameWidth, int frameHeight,int degree,boolean isFrontCamera) {
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
            this.mData = data;
            this.mDegree = degree;
            this.isFrontCamera = isFrontCamera;
            //wakeup
            synchronized (this) {
                notify();
            }
        }

        private void onDrawFrame() {
            if (mEngine != 0 && mData != null) {
//                long spend = SystemClock.elapsedRealtime();
                FinRender.renderOnContext(mEngine,mData,mFrameWidth,mFrameHeight,mDegree,isFrontCamera);
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
        mEngine = FinRender.createGLContext(mSurface, mFrameFormat);
        if (mEngine == 0) {
            Log.e(TAG, "渲染引擎启动失败！");
        } else {
            Log.d(TAG, "渲染引擎初始化成功");
        }
    }

    private void destroyGL() {
        if (mEngine != 0) {
            FinRender.releaseGLContext(mEngine);
        }
        Log.d(TAG, "渲染引擎已退出");
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
