package com.ifinver.finrender;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class FinRender {
    static {
        System.loadLibrary("fin-render-lib");
    }

    private static final String TAG = "FinRender";

    private static FinRender instance;
    private RenderThread mRenderThread;
    private boolean isPrepared = false;
    private FinRenderListener mListener;

    public static FinRender getInstance() {
        if (instance == null) {
            synchronized (FinRender.class) {
                if (instance == null) {
                    instance = new FinRender();
                }
            }
        }
        return instance;
    }

    private FinRender() {
        mRenderThread = new RenderThread();
        mRenderThread.start();
    }

    public void prepare(Surface output,FinRenderListener listener) {
        this.mListener = listener;
        mRenderThread.prepare(output);
    }

    public void startWithInput(SurfaceTexture inputSurface) {
        mRenderThread.startOutput(inputSurface);
    }

    public void release() {
        this.mListener = null;
        mRenderThread.release();
    }

    private class RenderThread extends HandlerThread implements Handler.Callback, SurfaceTexture.OnFrameAvailableListener {
        private final int MSG_INIT = 0x101;
        private final int MSG_START = 0x102;
        private final int MSG_RELEASE = 0x104;
        private final int MSG_PROCESS = 0x105;

        private Handler mSelfHandler;
//        private Handler mMainHandler;
        private boolean delayStart = false;
        private Surface mOutputSurface;
        private SurfaceTexture mInputSurface;
        private long mRenderEngine;

        public RenderThread() {
            super("FinRecorderThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
//            mMainHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        protected void onLooperPrepared() {
            synchronized (RenderThread.class) {
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
                    mRenderEngine = nativeCreate(mOutputSurface);
                    isPrepared = mRenderEngine != 0;
                    if(isPrepared){
                        Log.d(TAG,"初始化完成");
                    }else{
                        Log.e(TAG,"初始化出错");
                    }
                    //在当前线程回调
                    if(mListener != null){
                        mListener.onPrepared(isPrepared,getInputTex(mRenderEngine),getEglContext(mRenderEngine));
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
                case MSG_RELEASE:
                    stopOutputInternal();
                    nativeRelease(mRenderEngine);
                    mRenderEngine = 0;
                    Log.d(TAG,"已释放");
                    return true;
                case MSG_PROCESS:
                    process();
                    return true;
            }
            return false;
        }

        private void process() {
            if(isPrepared){
                Log.d(TAG,"process recorder");
                nativeRenderOut(mRenderEngine,mInputSurface);
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
                mSelfHandler.sendEmptyMessage(MSG_PROCESS);
            }
        }

        public void prepare(Surface surface) {
            this.mOutputSurface = surface;
            if (!isPrepared) {
                if (mSelfHandler != null) {
                    mSelfHandler.sendEmptyMessage(MSG_INIT);
                } else {
                    synchronized (RenderThread.class) {
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
    }

    public interface FinRenderListener{
        void onPrepared(boolean isPrepared, int texName, long eglContext);
    }

    private native long nativeCreate(Surface output);

    private native void nativeRenderOut(long engine,SurfaceTexture input);

    private native void nativeRelease(long engine);

    private native int getInputTex(long engine);

    private native long getEglContext(long engine);
}
