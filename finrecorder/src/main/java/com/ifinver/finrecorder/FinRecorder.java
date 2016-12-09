package com.ifinver.finrecorder;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;


/**
 * Created by iFinVer on 2016/12/7.
 * ilzq@foxmail.com
 */

public class FinRecorder {
    static {
        System.loadLibrary("fin-recorder-lib");
    }

    private static final String TAG = "FinRecorder";
    private RecorderThread mRecorderThread;

    public static FinRecorder prepare(Surface out, int inputTex, long sharedCtx,Object locker) {
        return new FinRecorder(out, inputTex, sharedCtx,locker);
    }

    private FinRecorder(Surface out, int inputTex, long sharedCtx, Object locker) {
        mRecorderThread = new RecorderThread(out, inputTex, sharedCtx,locker);
        mRecorderThread.start();
    }

    public void record(){
        mRecorderThread.process();
    }

    public void release() {
        mRecorderThread.release();
    }

    //if no ctx can get, 0 will be returned
    public long fetchGLCtxOfThread(){
        return nativeFetchGLCtx();
    }

    private class RecorderThread extends HandlerThread implements Handler.Callback {
        private final int MSG_INIT = 0x101;
        private final int MSG_RELEASE = 0x104;
        private final int MSG_PROCESS = 0x105;

        private Surface mOutputSurface;
        private int mInputTex;
        private long mSharedCtx;
        private Handler mSelfHandler;
        private long mRecorderEngine;
        private final Object mLocker;
        private long mRecorder;

        private RecorderThread(Surface out, int inputTex, long mSharedCtx, Object locker) {
            super("RecorderThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
            this.mOutputSurface = out;
            this.mInputTex = inputTex;
            this.mSharedCtx = mSharedCtx;
            this.mLocker = locker;
        }

        @Override
        protected void onLooperPrepared() {
            mSelfHandler = new Handler(getLooper(), this);
            mSelfHandler.sendEmptyMessage(MSG_INIT);
        }

        private void release() {
            mSelfHandler.sendEmptyMessage(MSG_RELEASE);
        }

        private void process(){
            if(mSelfHandler != null) {
                mSelfHandler.sendEmptyMessage(MSG_PROCESS);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    initRecorder();
                    return true;
                case MSG_RELEASE:
                    releaseInternal();
                    Log.d(TAG, "录制引擎已释放");
                    return true;
                case MSG_PROCESS:
                    processInternal();
                    return true;
            }
            return false;
        }

        private void initRecorder() {
            Log.d(TAG, "录制引擎开始初始化");
            mRecorder = nativeCreate(mSharedCtx, mOutputSurface);
            if(mRecorder != 0){
                Log.d(TAG, "录制引擎初始化成功");
            }else{
                Log.d(TAG, "录制引擎初始化失败!");
                mSelfHandler.sendEmptyMessage(MSG_RELEASE);
            }
        }

        private void processInternal() {
            if(mRecorder != 0) {
                synchronized (mLocker) {
                    nativeProcess(mRecorder, mInputTex);
                }
            }
        }

        private void releaseInternal() {
            if(mOutputSurface != null){
                mOutputSurface.release();
                mOutputSurface = null;
            }
            if(mRecorder != 0) {
                nativeRelease(mRecorder);
            }
            quitSafely();
        }
    }

    private native long nativeCreate(long sharedCtx,Surface output);

    private native void nativeProcess(long recorder,int inputTex);

    private native void nativeRelease(long recorder);

    private native long nativeFetchGLCtx();
}
