package com.ifinver.finrecorder;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class FinRecorder {
    static {
        System.loadLibrary("fin-recorder-lib");
    }

    private static final String TAG = "FinRecorder";

    private static FinRecorder instance;
    private RecorderThread mRecorderThread;
    private boolean isPrepared = false;
    private FinRenderListener mListener;

    public static FinRecorder getInstance() {
        if (instance == null) {
            synchronized (FinRecorder.class) {
                if (instance == null) {
                    instance = new FinRecorder();
                }
            }
        }
        return instance;
    }

    private FinRecorder() {
        mRecorderThread = new RecorderThread();
        mRecorderThread.start();
    }

    public void prepare(Surface output,FinRenderListener listener) {
        this.mListener = listener;
        mRecorderThread.prepare(output);
    }

    public void recording(SurfaceTexture inputSurface) {
        mRecorderThread.startOutput(inputSurface);
    }

    public void stopRecording() {
        mRecorderThread.stopOutput();
    }

    public void release() {
        this.mListener = null;
        mRecorderThread.release();
    }

    private class RecorderThread extends HandlerThread implements Handler.Callback, SurfaceTexture.OnFrameAvailableListener {
        private final int MSG_INIT = 0x101;
        private final int MSG_START = 0x102;
        private final int MSG_STOP = 0x103;
        private final int MSG_RELEASE = 0x104;
        private final int MSG_PROCESS = 0x105;

        private Handler mSelfHandler;
        private Handler mMainHandler;
        private boolean delayStart = false;
        private Surface mOutputSurface;
        private SurfaceTexture mInputSurface;
        private int inputTex;

        public RecorderThread() {
            super("FinRecorderThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
            mMainHandler = new Handler(Looper.getMainLooper());
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
                    inputTex = nativePrepare(mOutputSurface);
                    isPrepared = inputTex != 0;
                    if(isPrepared){
                        Log.d(TAG,"初始化完成");
                    }else{
                        Log.e(TAG,"初始化出错");
                    }
                    //在当前线程回调
                    if(mListener != null){
                        mListener.onPrepared(inputTex);
                    }
//                    mMainHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(mListener != null){
//                                mListener.onPrepared(inputTex);
//                            }
//                        }
//                    });
                    return true;
                case MSG_START:
                    startOutputInternal();
                    return true;
                case MSG_STOP:
                    stopOutputInternal();
                    return true;
                case MSG_RELEASE:
                    nativeRelease();
                    Log.d(TAG,"已释放");
                    return true;
                case MSG_PROCESS:
                    process();
                    Log.d(TAG,"已释放");
                    return true;
            }
            return false;
        }

        private void process() {
            if(isPrepared){
                Log.d(TAG,"process recorder");
                nativeRenderOutput(mInputSurface);
            }
        }

        private void stopOutputInternal() {
            Log.d(TAG,"stop recording");
            mInputSurface.setOnFrameAvailableListener(null);
            mInputSurface = null;
        }

        private void startOutputInternal() {
            if(mInputSurface != null) {
                Log.d(TAG,"recording");
                mInputSurface.setOnFrameAvailableListener(this);
            }
        }
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if(isPrepared){
                Log.d(TAG,"onFrameAvailableonFrameAvailable");
                mSelfHandler.sendEmptyMessage(MSG_PROCESS);
            }
        }

        public void prepare(Surface surface) {
            this.mOutputSurface = surface;
            if (!isPrepared) {
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

        public void release() {
            isPrepared = false;
            mSelfHandler.sendEmptyMessage(MSG_RELEASE);
        }

        public void startOutput(SurfaceTexture inputSurface) {
            this.mInputSurface = inputSurface;
            mSelfHandler.sendEmptyMessage(MSG_START);
        }

        public void stopOutput() {
            isPrepared = false;
            mSelfHandler.sendEmptyMessage(MSG_STOP);
        }

    }

    public interface FinRenderListener{
        void onPrepared(int texName);
    }

    private native int nativePrepare(Surface output);

    private native void nativeRenderOutput(SurfaceTexture input);

    private native void nativeRelease();

}
