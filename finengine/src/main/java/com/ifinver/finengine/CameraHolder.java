package com.ifinver.finengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class CameraHolder {

    private static final String TAG = "FinEngine";

    private Context mAppCtx;

    private FinEngineThread mEngineThread;

    private CameraHolder() {
        mEngineThread = new FinEngineThread();
        mEngineThread.start();
    }

    @SuppressLint("StaticFieldLeak")
    private static CameraHolder mInstance;

    public static CameraHolder getInstance() {
        if (mInstance == null) {
            synchronized (CameraHolder.class) {
                if (mInstance == null) {
                    mInstance = new CameraHolder();
                }
            }
        }
        return mInstance;
    }

    public void start(int expectedWidth,int expectedHeight,Context ctx, CameraListener listener) {
        this.mAppCtx = ctx.getApplicationContext();
        mEngineThread.start(expectedWidth, expectedHeight, listener);
    }

    public boolean toggleCamera() {
        return mEngineThread.toggleCamera();
    }

    public void stopCamera() {
        mAppCtx = null;
        mEngineThread.exit();
    }

    @SuppressWarnings({"WeakerAccess", "deprecation"})
    private class FinEngineThread extends HandlerThread implements  Handler.Callback, Camera.PreviewCallback {

        private final int MSG_INIT = 0x101;
        private final int MSG_STOP = 0x103;
        private final int MSG_TOGGLE = 0x104;
        private final int TEXTURE_ID = 20;

        private int mExpectedWidth;
        private int mExpectedHeight;
        private ByteBuffer mVideoBuffer[];
        private int mVideoBufferIdx = 0;
        private CameraListener mListener;
        private SurfaceTexture mSurfaceTexture;
        private int mFrameWidth;
        private int mFrameHeight;
        private Handler mSelfHandler;
        private Handler mMainHandler;
        private boolean delayStart = false;
        private boolean exited = false;

        private Camera mCamera;
        public int mCameraIndex;
        private int mCameraOrientation = 0;

        FinEngineThread() {
            super("FinEngineThread", Process.THREAD_PRIORITY_DISPLAY);
            mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
            mMainHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if(!exited) {
                //prepare another frame
                mVideoBufferIdx = 1 - mVideoBufferIdx;
                if(mCamera != null) {
                    mCamera.addCallbackBuffer(mVideoBuffer[mVideoBufferIdx].array());
                }

                if(mListener != null) {
                    mListener.onVideoBuffer(data, mFrameWidth, mFrameHeight, mCameraOrientation, isFrontCurrent());
                }
            }
        }

        private boolean isFrontCurrent() {
            return mCameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT;
        }


        public void start(int expectedWidth, int expectedHeight, CameraListener listener) {
            this.mExpectedWidth = expectedWidth;
            this.mExpectedHeight = expectedHeight;
            this.mListener = listener;
            if (mSelfHandler != null) {
                mSelfHandler.sendEmptyMessage(MSG_INIT);
            } else {
                synchronized (FinEngineThread.class) {
                    if (mSelfHandler == null) {
                        delayStart = true;
                    } else {
                        mSelfHandler.sendEmptyMessage(MSG_INIT);
                    }
                }
            }
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


        public boolean toggleCamera() {
            if (mSelfHandler != null) {
                try {
                    mSelfHandler.sendEmptyMessage(MSG_TOGGLE);
                    return true;
                } catch (Throwable ignored) {
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
                    initCamera();
                    return true;
                case MSG_STOP:
                    stopCamera();
                    return true;
                case MSG_TOGGLE:
                    toggleCameraInternal();
                    return true;
            }
            return false;
        }

        private void toggleCameraInternal() {
            Log.d(TAG, "toggleCamera");
            stopCamera();
            if(mCameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT){
                mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
            }else{
                mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
            final boolean finalSuccess = initCamera();
            if (mListener != null) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onToggleCameraComplete(finalSuccess);
                    }
                });
            }
        }

        private void stopCamera() {
            synchronized (this) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);

                    mCamera.release();
                }
                mCamera = null;
                Log.d(TAG, "摄像机已释放");
            }
        }

        public boolean initCamera() {
            if(mCamera != null){
                Log.e(TAG,"初始化摄像头时,摄像头已经初始化过了！");
                return false;
            }
            Log.d(TAG, "摄像头开始初始化");
            boolean init = false;
            synchronized (this) {
                mCamera = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    try {
                        mCamera = Camera.open(mCameraIndex);
                        if (mCameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            Log.d(TAG, "已打开前置摄像头");
                        } else {
                            Log.d(TAG, "已打开后置摄像头");
                        }
                    } catch (RuntimeException e) {
                        Log.e(TAG, "摄像头 #" + mCameraIndex + "打开失败！: " + e.getLocalizedMessage());
                    }
                }

                if (mCamera == null) {
                    return false;
                }

                try {
                    Camera.Parameters params = mCamera.getParameters();
                    List<Camera.Size> sizes = params.getSupportedPreviewSizes();
                    if (sizes != null) {
                        //格式
                        params.setPreviewFormat(ImageFormat.NV21);
                        //大小
                        CameraSize frameSize = calculateCameraFrameSize(sizes, mExpectedWidth, mExpectedHeight);
                        Log.d(TAG, "预览设置为 " + frameSize.width + "x" + frameSize.height);
                        params.setPreviewSize(frameSize.width, frameSize.height);
                        //帧率
                        List<int[]> support = params.getSupportedPreviewFpsRange();
                        if (support.size() > 0) {
                            int[] ints = support.get(0);
                            int max = Math.max(ints[0],ints[1]);
                            for (int[] size : support) {
                                max = Math.max(max,size[1]);
                            }
                            params.setPreviewFpsRange(max, max);
                            Log.d(TAG, "帧率设置为:[" + max + "," + max + "]");
                        } else {
                            Log.e(TAG, "WTF,不能设置帧率");
                        }
                        //优化
                        //三星的机型也有问题，未知的问题机型较多，所以不使用
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !android.os.Build.MODEL.equals("GT-I9100")){
//                        params.setRecordingHint(true);
//                    }
                        //聚焦
                        List<String> FocusModes = params.getSupportedFocusModes();
                        if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                        }
                        mCamera.setParameters(params);
                        //记录宽高
                        params = mCamera.getParameters();
                        mFrameWidth = params.getPreviewSize().width;
                        mFrameHeight = params.getPreviewSize().height;
                        int size = mFrameWidth * mFrameHeight;
                        size = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                        mVideoBuffer = new ByteBuffer[2];
                        mVideoBuffer[0] = ByteBuffer.allocate(size);
                        mVideoBuffer[1] = ByteBuffer.allocate(size);
                        mSurfaceTexture = new SurfaceTexture(TEXTURE_ID);

                        mCamera.addCallbackBuffer(mVideoBuffer[mVideoBufferIdx].array());
                        mCamera.setPreviewCallbackWithBuffer(this);
                        mCamera.setPreviewTexture(mSurfaceTexture);
                        mCamera.startPreview();
                        updateCameraDegree();
                        Log.d(TAG, "开始Camera预览");
                        init = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(init) {
                Log.d(TAG, "摄像头初始化成功！");
            }else{
                if(mVideoBuffer != null){
                    mVideoBuffer[0] = null;
                    mVideoBuffer[1] = null;
                    mVideoBuffer = null;
                }
                if(mSurfaceTexture != null){
                    mSurfaceTexture.release();
                    mSurfaceTexture = null;
                }
                mCamera = null;
                Log.d(TAG, "摄像头初始化失败！");
            }
            if (mListener != null) {
                final boolean finalInit = init;
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onCameraStart(finalInit);
                        if(finalInit) {
                            exited = false;
                        }
                    }
                });
            }else if(init){
                exited = false;
            }
            return init;
        }

        private void updateCameraDegree() {
            WindowManager wm = (WindowManager) mAppCtx.getSystemService(Context.WINDOW_SERVICE);
            setCameraDegreeByWindowRotation(wm.getDefaultDisplay().getRotation());
        }

        public void setCameraDegreeByWindowRotation(int windowRotation) {
            int mWindowDegree = 0;
            switch (windowRotation) {
                case Surface.ROTATION_0:
                    mWindowDegree = 0;
                    break;
                case Surface.ROTATION_90:
                    mWindowDegree = 90;
                    break;
                case Surface.ROTATION_180:
                    mWindowDegree = 180;
                    break;
                case Surface.ROTATION_270:
                    mWindowDegree = 270;
                    break;
            }
            if (mCamera != null) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(mCameraIndex, info);

                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCameraOrientation = (info.orientation + mWindowDegree) % 360;
                } else { // back-facing
                    mCameraOrientation = (info.orientation - mWindowDegree + 360) % 360;
                }
                mCameraOrientation = (360 - mCameraOrientation) % 360; // compensate the mirror
                mCamera.setDisplayOrientation(mCameraOrientation);
                Log.d(TAG, "摄像机角度 = " + mCameraOrientation);
            }

        }

        /**
         * 根据期望选择合适宽高
         */
        private CameraSize calculateCameraFrameSize(List<Camera.Size> supportedSizes, int expectWidth, int expectHeight) {
            int calcWidth = 0;
            int calcHeight = 0;

            for (Camera.Size size : supportedSizes) {
                int width = size.width;
                int height = size.height;
                Log.d(TAG,"摄像头支持Size:"+width+"x"+height+", height/width="+(float)height/width);

                if (width <= expectWidth && height <= expectHeight) {
                    if (width >= calcWidth && height >= calcHeight) {
                        calcWidth = width;
                        calcHeight = height;
                    }
                }
            }
            Log.d(TAG,"期望的Size:"+expectWidth+"x"+expectHeight+",width/height="+(float)expectWidth/expectHeight);

            return new CameraSize(calcWidth, calcHeight);
        }
    }

    public interface CameraListener {

        void onCameraStart(boolean success);
        /**
         * notice that this method may be invoked before onCameraStart.
         */
        void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean frontCurrent);

        void onToggleCameraComplete(boolean success);
    }

    private class CameraSize {
        int width;
        int height;

        CameraSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
