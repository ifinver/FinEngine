package com.ifinver.finenginesample;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.ifinver.finrecorder.FinRecorder;
import com.ifinver.finrender.FinRender;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class Renderer implements TextureView.SurfaceTextureListener, FinRecorder.FinRenderListener {

    private static final  String TAG="FinRender";

    private SurfaceTexture mInputSurface;
    private RenderThread mRenderThread;
    private int mFrameWidth;
    private int mFrameHeight = 0;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        FinRecorder.getInstance().prepare(new Surface(surface),this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mRenderThread.quit();
        mRenderThread = null;
        FinRecorder.getInstance().release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    @Override
    public void onPrepared(int texName) {
        mInputSurface = new SurfaceTexture(texName);
        FinRecorder.getInstance().recording(mInputSurface);
        if(mFrameHeight != 0) {
            mInputSurface.setDefaultBufferSize(mFrameWidth, mFrameHeight);
        }
        mRenderThread = new RenderThread(new Surface(mInputSurface),FinRender.FORMAT_NV21);
        mRenderThread.start();
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight,int degree,boolean isFrontCamera) {
        if (mRenderThread != null) {
            mRenderThread.notifyWithBuffer(data, frameWidth, frameHeight,degree,isFrontCamera);
        }
    }

    public void onCameraStart(int frameWidth, int frameHeight) {
        this.mFrameWidth = frameWidth;
        this.mFrameHeight = frameHeight;
        if(mInputSurface != null) {
            mInputSurface.setDefaultBufferSize(frameWidth,frameHeight);
        }
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
            if (mEngine != 0 && mData != null) {
//                long spend = SystemClock.elapsedRealtime();
                FinRender.renderOnContext(mEngine, mData, mFrameWidth, mFrameHeight, mDegree, isFrontCamera);
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
