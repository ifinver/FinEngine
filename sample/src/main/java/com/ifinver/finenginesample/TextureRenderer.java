package com.ifinver.finenginesample;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.ifinver.finengine.FinEngine;

/**
 * Created by iFinVer on 2016/11/21.
 * ilzq@foxmail.com
 */

public class TextureRenderer implements TextureView.SurfaceTextureListener {
    private static final String TAG = "FinEngine";


    private RenderThread mRenderThread;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;


    public TextureRenderer() {
        this.mSurface = null;
        this.mRenderThread = null;
    }

    private void release() {
        if (mRenderThread != null) {
            mRenderThread.quit();
            mRenderThread = null;
        }
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera) {
        if (mRenderThread != null) {
            mRenderThread.notifyWithBuffer(data, frameWidth, frameHeight, degree, isFrontCamera);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.mSurfaceTexture = surface;
        mSurface = new Surface(surface);

        if (mRenderThread == null) {
            mRenderThread = new RenderThread(mSurface);
            mRenderThread.start();
        }
    }

    public SurfaceTexture getSurfaceTexture() {
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
        private int mFrameWidth;
        private int mFrameHeight;
        private byte[] mData;
        private int mDegree;
        private boolean isFrontCamera;
        private boolean inited;

        RenderThread(Surface surface) {
            this.mSurface = surface;
        }

        public void notifyWithBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera) {
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
            if (inited && mData != null) {
//                long spend = SystemClock.elapsedRealtime();
                FinEngine.nativeRender(mData, mFrameWidth, mFrameHeight, mDegree, isFrontCamera);
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
            inited = FinEngine.nativeInit(mSurface);
            if (!inited) {
                Log.e(TAG, "渲染引擎启动失败！");
            } else {
                Log.d(TAG, "渲染引擎初始化成功");
            }
        }

        private void destroyGL() {
            FinEngine.nativeRelease();
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
