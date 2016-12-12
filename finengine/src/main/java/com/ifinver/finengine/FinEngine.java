package com.ifinver.finengine;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class FinEngine {

    static {
        System.loadLibrary("fin-engine-lib");
    }

    private static final String TAG = "FinEngine";

    public static final int FILTER_TYPE_NORMAL = 0; //must be zero here.
    public static final int FILTER_TYPE_GREY_SCALE = 1;
    public static final int FILTER_TYPE_SEPIA_STONE = 2;
    public static final int FILTER_TYPE_CYAN = 3;
    public static final int FILTER_TYPE_RADIAL_BLUR = 4;
    public static final int FILTER_TYPE_NEGATIVE_COLOR = 5;
    public static final int FILTER_TYPE_V_MIRROR = 6;
    public static final int FILTER_TYPE_H_MIRROR = 7;
    public static final int FILTER_TYPE_FISH_EYE = 8;

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

    public void prepare(Surface output, int width, int height) {
        mEngineThread.prepare(output, width, height);
    }

    public void process(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera) {
        mEngineThread.process(data, frameWidth, frameHeight, degree, isFrontCamera);
    }

    public void release() {
        mEngineThread.release();
    }

    public void resizeInput(int surfaceWidth, int surfaceHeight) {
        mEngineThread.resizeInput(surfaceWidth, surfaceHeight);
    }

    public void switchFilter(Context ctx, int filterType) {
        mEngineThread.switchFilter(ctx, filterType);
    }

    public int getCurrentFilter(){
        return mEngineThread.mFilterType;
    }

    private class FinEngineThread extends HandlerThread implements Handler.Callback {
        private final int MSG_INIT = 0x101;
        private final int MSG_RELEASE = 0x104;
        private final int MSG_PROCESS = 0x105;
        private final int MSG_SWITCH_FILTER = 0x106;

        private Handler mSelfHandler;
        private boolean delayStart = false;
        private Surface mOutputSurface;
        private boolean isPrepared;
        private int mFrameWidth;
        private int mFrameHeight;
        private byte[] mData;
        private int mDegree;
        private boolean isFrontCamera;
        private int mOutWidth;
        private int mOutHeight;
        private AssetManager mAssetManager;
        private int mFilterType;

        FinEngineThread() {
            super("FinEngineThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
            mFilterType = FILTER_TYPE_NORMAL;
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

        public void prepare(Surface output, int width, int height) {
            this.mOutputSurface = output;
            this.mOutWidth = width;
            this.mOutHeight = height;
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

        public void switchFilter(Context ctx, int filterType) {
            synchronized (FinEngineThread.class) {
                this.mAssetManager = ctx.getApplicationContext().getAssets();
                this.mFilterType = filterType;
            }
            mSelfHandler.sendEmptyMessage(MSG_SWITCH_FILTER);
        }

        public void process(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera) {
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
            this.mData = data;
            this.mDegree = degree;
            this.isFrontCamera = isFrontCamera;
            if(mSelfHandler != null) {
                mSelfHandler.sendEmptyMessage(MSG_PROCESS);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    init();
                    return true;
                case MSG_RELEASE:
                    Log.w(TAG, "FinEngine已释放");
                    nativeRelease();
                    isPrepared = false;
                    return true;
                case MSG_SWITCH_FILTER:
                    switchFilterInternal();
                    return true;
                case MSG_PROCESS:
                    if (isPrepared && mData != null) {
                        nativeRender(mData, mFrameWidth, mFrameHeight, mDegree, isFrontCamera, mOutWidth, mOutHeight);
                    }
                    return true;
            }
            return false;
        }

        private void switchFilterInternal() {
            if(isPrepared) {
                Log.w(TAG, "开始切换滤镜");
                synchronized (FinEngineThread.class) {
                    FinFiltersManager.Shader shader = FinFiltersManager.findShader(mFilterType);
                    nativeSwitchFilter(mAssetManager, mFilterType, shader.vertex, shader.fragment);
                }
            }
        }

        private void init() {
            isPrepared = nativeInit(mOutputSurface);
        }

        public void release() {
            mSelfHandler.sendEmptyMessage(MSG_RELEASE);
        }

        public void resizeInput(int surfaceWidth, int surfaceHeight) {
            this.mOutWidth = surfaceWidth;
            this.mOutHeight = surfaceHeight;
        }
    }


    /**
     * @return 0 means failed
     */
    private native boolean nativeInit(Surface output);

    private native void nativeRelease();

    private native void nativeSwitchFilter(AssetManager mAssetManager, int mFilterType, String mVertexName, String mFragmentName);

    private native void nativeRender(byte[] data, int frameWidth, int frameHeight, int degree, boolean mirror, int mOutWidth, int mOutHeight);
}
