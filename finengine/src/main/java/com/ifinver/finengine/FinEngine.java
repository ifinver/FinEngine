package com.ifinver.finengine;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;

import static android.content.ContentValues.TAG;

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

    private static FinEngine instance;
    private final FinEngineThread mEngineThread;

    public static FinEngine getInstance() {
        if (instance == null) {
            synchronized (FinEngine.class) {
                if (instance == null) {
                    instance = new FinEngine();
                }
            }
        }
        return instance;
    }

    private FinEngine() {
        mEngineThread = new FinEngineThread();
        mEngineThread.start();
    }

    public void prepare(Surface output){
        mEngineThread.prepare(output);
    }

    public void process(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera){
        mEngineThread.process(data,frameWidth,frameHeight,degree,isFrontCamera);
    }

    public void release(){
        mEngineThread.release();
    }

    private class FinEngineThread extends HandlerThread implements Handler.Callback {
        private final int MSG_INIT = 0x101;
        private final int MSG_RELEASE = 0x104;
        private final int MSG_PROCESS = 0x105;

        private Handler mSelfHandler;
        private boolean delayStart = false;
        private Surface mOutputSurface;
        private boolean isPrepared;
        private int mFrameWidth;
        private int mFrameHeight;
        private byte[] mData;
        private int mDegree;
        private boolean isFrontCamera;

        FinEngineThread() {
            super("FinEngineThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
        }

        @Override
        protected void onLooperPrepared() {
            synchronized (FinEngineThread.class) {
                mSelfHandler = new Handler(getLooper(), this);
                if (delayStart) {
                    delayStart = false;
                    mSelfHandler.sendEmptyMessage(MSG_INIT);
                }
            }
        }

        public void prepare(Surface output) {
            this.mOutputSurface = output;
            if (!isPrepared) {
                if (mSelfHandler != null) {
                    mSelfHandler.sendEmptyMessage(MSG_INIT);
                } else {
                    synchronized (FinEngineThread.class) {
                        if (mSelfHandler != null) {
                            mSelfHandler.sendEmptyMessage(MSG_INIT);
                        } else {
                            delayStart = true;
                        }
                    }
                }
            }
        }

        public void process(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera) {
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
            this.mData = data;
            this.mDegree = degree;
            this.isFrontCamera = isFrontCamera;

            mSelfHandler.sendEmptyMessage(MSG_PROCESS);
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    init();
                    return true;
                case MSG_RELEASE:
                    Log.d(TAG, "已释放");
                    nativeRelease();
                    isPrepared = false;
                    return true;
                case MSG_PROCESS:
                    if(isPrepared && mData != null){
                        nativeRender(mData, mFrameWidth, mFrameHeight, mDegree, isFrontCamera);
                    }
                    return true;
            }
            return false;
        }

        private void init() {
            isPrepared = nativeInit(mOutputSurface);
        }

        public void release() {
            mSelfHandler.sendEmptyMessage(MSG_RELEASE);
        }
    }

    /**
     * @return 0 means failed
     */
    private native boolean nativeInit(Surface output);

    private native void nativeRelease();

    private native void nativeRender(byte[] data, int frameWidth, int frameHeight, int degree, boolean mirror);
}
