package com.ifinver.finengine;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class FinEngine {
    static {
        System.loadLibrary("fin-engine-lib");
    }

    private static final String TAG = "Fin Engine";

    public static final int FILTER_TYPE_NORMAL = 0;
    public static final int FILTER_TYPE_CYAN = 1;
    public static final int FILTER_TYPE_FISH_EYE = 2;
    public static final int FILTER_TYPE_GREY_SCALE = 3;
    public static final int FILTER_TYPE_NEGATIVE_COLOR = 4;
    private Context mAppCtx;

    private FinEngine() {}

    private static FinEngine mInstance;

    public static FinEngine getInstance() {
        if (mInstance == null) {
            synchronized (FinEngine.class) {
                if (mInstance == null) {
                    mInstance = new FinEngine();
                }
            }
        }
        return mInstance;
    }

    private FinEngineThread mEngineThread;

    public void startEngine(Context ctx, EngineListener listener) {
        this.mAppCtx = ctx.getApplicationContext();
        DisplayMetrics dm = mAppCtx.getResources().getDisplayMetrics();
        mEngineThread = new FinEngineThread(dm.widthPixels, dm.heightPixels, listener);
        mEngineThread.start();
    }

    public boolean toggleCamera() {
        if(mEngineThread != null) {
            return mEngineThread.toggleCamera();
        }
        return false;
    }

    public void stopEngine() {
        mEngineThread.exit();
        mEngineThread = null;
    }

    private class FinEngineThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener, Handler.Callback {

        private final int MSG_INIT = 0x101;
        private final int MSG_PROCESS = 0x102;
        private final int MSG_STOP = 0x103;
        private final int MSG_TOGGLE = 0x104;

        private final int mExpectedWidth;
        private final int mExpectedHeight;
        private ByteBuffer mVideoBuffer;
        private final EngineListener mListener;
        private int mInputTex;
        private SurfaceTexture mSurfaceTexture;
        private CameraUtils mCameraUtils;
        private int mFrameWidth;
        private int mFrameHeight;
        private Handler mSelfHandler;
        private Handler mMainHandler;
        private boolean exited = false;


        FinEngineThread(int expectedWidth, int expectedHeight, EngineListener listener) {
            super("FinEngineThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
            this.mExpectedWidth = expectedWidth;
            this.mExpectedHeight = expectedHeight;
            this.mListener = listener;
            this.mCameraUtils = new CameraUtils();
            try {
                mMainHandler = new Handler();
            }catch (RuntimeException ignored){
                throw new IllegalStateException("必须在有looper的线程中初始化引擎，主线程或者HandlerThread");
            }
        }

        @Override
        protected void onLooperPrepared() {
            mSelfHandler = new Handler(getLooper(), this);
            mSelfHandler.sendEmptyMessage(MSG_INIT);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (mSelfHandler != null) {
                mSelfHandler.sendEmptyMessage(MSG_PROCESS);
            }
        }

        public boolean toggleCamera() {
            if (mSelfHandler != null) {
                try {
                    mSelfHandler.sendEmptyMessage(MSG_TOGGLE);
                    return true;
                }catch (Throwable ignored){
                    return false;
                }
            }
            return false;
        }

        public void exit() {
            exited = true;
            if (mSelfHandler != null) {
                mSelfHandler.sendEmptyMessage(MSG_STOP);
            }
        }


        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    initCameraAndEngine();
                    return true;
                case MSG_PROCESS:
                    processPerFrame();
                    return true;
                case MSG_STOP:
                    stopCameraAndEngine();
                    return true;
                case MSG_TOGGLE:
                    toggleCameraInternal();
                    return true;
            }
            return false;
        }

        private void toggleCameraInternal() {
            boolean success = mCameraUtils.toggleCameraWithoutStart();
            if (success) {
                success = mCameraUtils.startPreview(mAppCtx, mSurfaceTexture);
            }
            final boolean finalSuccess = success;
            if (mListener != null) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onToggleCameraComplete(finalSuccess);
                    }
                });
            }
        }

        private void stopCameraAndEngine() {
            mCameraUtils.stop();
            _stopEngine();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                quitSafely();
            } else {
                quit();
            }
        }

        private void processPerFrame() {
            if (mSurfaceTexture != null && !exited) {
                process(mSurfaceTexture, mCameraUtils.getFrameDegree(), mVideoBuffer.array());
                if (mListener != null) {
                    mListener.onVideoBuffer(mVideoBuffer.array(), mFrameWidth, mFrameHeight);
                }
            }
        }

        private void initCameraAndEngine() {
            boolean init = mCameraUtils.init(mExpectedWidth, mExpectedHeight);
            if (!init) {
                Log.e(TAG, "引擎初始化失败！摄像头不能初始化");
                return;
            }
            mFrameWidth = mCameraUtils.getFrameWidth();
            mFrameHeight = mCameraUtils.getFrameHeight();
            mInputTex = _startEngine(mFrameWidth, mFrameHeight, mAppCtx.getAssets());
            if (mInputTex == -1) {
                Log.e(TAG, "引擎初始化失败！");
                init = false;
            }else {
                try {
                    mVideoBuffer = ByteBuffer.allocateDirect(mFrameHeight * mFrameWidth * 4);
                    mVideoBuffer.position(0);
                    mSurfaceTexture = new SurfaceTexture(mInputTex);
                    mSurfaceTexture.setOnFrameAvailableListener(this);
                    mCameraUtils.startPreview(mAppCtx, mSurfaceTexture);
                }catch (Throwable err){
                    err.printStackTrace();
                    init = false;
                }
            }
            if(mListener != null){
                final boolean finalInit = init;
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onEngineStart(finalInit,mFrameWidth,mFrameHeight);
                    }
                });
            }
        }

    }

    private native int _startEngine(int frameWidth, int frameHeight, AssetManager assets);

    private native void _stopEngine();

    private native void process(SurfaceTexture surfaceTexture, int mFrameDegree, byte[] array);

    public interface EngineListener {

        void onEngineStart(boolean success,int frameWidth,int frameHeight);

        /**
         * notice that this method may be invoked before onEngineStart.
         */
        void onVideoBuffer(byte[] data, int frameWidth, int frameHeight);

        void onToggleCameraComplete(boolean success);
    }
}
