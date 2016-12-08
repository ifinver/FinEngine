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
    public static final int FILTER_TYPE_CYAN = 0x101;
    public static final int FILTER_TYPE_FISH_EYE = 0x102;
    public static final int FILTER_TYPE_GREY_SCALE = 0x103;
    public static final int FILTER_TYPE_NEGATIVE_COLOR = 0x104;

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
            Log.d(TAG, "开始切换滤镜");
            String vertex;
            String fragment;
            synchronized (FinEngineThread.class) {
                switch (mFilterType) {
                    default:
                    case FILTER_TYPE_NORMAL:
                        vertex = "";
                        fragment = "";
                        break;
                    case FILTER_TYPE_CYAN:
                        vertex = "vertex.glsl";
                        fragment = "fragment_cyan.glsl";
                        break;
                    case FILTER_TYPE_FISH_EYE:
                        vertex = "vertex.glsl";
                        fragment = "fragment_fish_eye.glsl";
                        break;
                    case FILTER_TYPE_GREY_SCALE:
                        vertex = "vertex.glsl";
                        fragment = "fragment_grey.glsl";
                        break;
                    case FILTER_TYPE_NEGATIVE_COLOR:
                        vertex = "vertex.glsl";
                        fragment = "fragment_negative_color.glsl";
                        break;
                }
                nativeSwitchFilter(mAssetManager, mFilterType, vertex, fragment);
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
