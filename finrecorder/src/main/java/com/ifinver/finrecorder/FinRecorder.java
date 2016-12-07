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
    private static FinRecorder instance;
    private RecorderThread mRecorderThread;
    public static FinRecorder getInstance(){
        if(instance == null){
            synchronized (FinRecorder.class){
                if(instance == null){
                    instance = new FinRecorder();
                }
            }
        }
        return instance;
    }
    private FinRecorder(){
        mRecorderThread = new RecorderThread();
        mRecorderThread.start();
    }

    public void prepare(Surface out,int inputTex,long sharedCtx){
        mRecorderThread.prepare(out,inputTex,sharedCtx);
    }



    private class RecorderThread extends HandlerThread implements Handler.Callback {
        private final int MSG_INIT = 0x101;
        private final int MSG_START = 0x102;
        private final int MSG_RELEASE = 0x104;
        private final int MSG_PROCESS = 0x105;

        private Surface mOutputSurface;
        private int mInputTex;
        private long sharedCtx;
        private Handler mSelfHandler;
        private boolean delayStart = false;
        private long mRecorderEngine;

        public RecorderThread(){
            super("RecorderThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
        }

        @Override
        protected void onLooperPrepared() {
            synchronized (RecorderThread.class) {
                mSelfHandler = new Handler(getLooper(), this);
                if (delayStart) {
                    delayStart = false;
                    mSelfHandler.sendEmptyMessage(MSG_INIT);
                }
            }
        }
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    Log.d(TAG,"开始初始化");
                    initRecorder();
                    return true;
                case MSG_START:
                    return true;
                case MSG_RELEASE:
                    return true;
                case MSG_PROCESS:
                    return true;
            }
            return false;
        }

        private void initRecorder() {

        }

        public void prepare(Surface out, int inputTex, long sharedCtx) {
            this.mOutputSurface = out;
            this.mInputTex = inputTex;
            this.sharedCtx = sharedCtx;

            if (mRecorderEngine != 0) {
                if (mSelfHandler != null) {
                    mSelfHandler.sendEmptyMessage(MSG_INIT);
                } else {
                    synchronized (RecorderThread.class) {
                        if (mSelfHandler != null) {
                            mSelfHandler.sendEmptyMessage(MSG_INIT);
                        } else {
                            delayStart = true;
                        }
                    }
                }
            }
        }


    }

}
