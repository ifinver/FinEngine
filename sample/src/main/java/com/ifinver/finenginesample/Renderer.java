package com.ifinver.finenginesample;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.ifinver.finengine.FinEngine;
import com.ifinver.finrender.FinRender;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class Renderer implements TextureView.SurfaceTextureListener, FinRender.FinRenderListener {

    private static final String TAG = "FinEngine";

    private SurfaceTexture mInputSurface;
    private RenderThread mRenderThread;
    private int mFrameWidth;
    private int mFrameHeight = 0;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        FinRender.getInstance().prepare(new Surface(surface), this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mRenderThread.quit();
        mRenderThread = null;
        FinRender.getInstance().release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    @Override
    public void onPrepared(boolean isPrepared, int texName, long eglContext) {
        mInputSurface = new SurfaceTexture(texName);
        FinRender.getInstance().startWithInput(mInputSurface);
        if (mFrameHeight != 0) {
            mInputSurface.setDefaultBufferSize(mFrameWidth, mFrameHeight);
        }
        mRenderThread = new RenderThread(new Surface(mInputSurface));
        mRenderThread.start();
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera) {
        if (mRenderThread != null) {
            mRenderThread.notifyWithBuffer(data, frameWidth, frameHeight, degree, isFrontCamera);
        }
    }

    public void onCameraStart(int frameWidth, int frameHeight) {
        this.mFrameWidth = frameWidth;
        this.mFrameHeight = frameHeight;
        if (mInputSurface != null) {
            mInputSurface.setDefaultBufferSize(frameWidth, frameHeight);
        }
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
                FinEngine.render(mData, mFrameWidth, mFrameHeight, mDegree, isFrontCamera);
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
            inited = FinEngine.init(mSurface);
            if (!inited) {
                Log.e(TAG, "渲染引擎启动失败！");
            } else {
                Log.d(TAG, "渲染引擎初始化成功");
            }
        }

        private void destroyGL() {
            FinEngine.release();
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
