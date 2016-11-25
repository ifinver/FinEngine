package com.ifinver.myopengles;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iFinVer on 2016/11/15.
 * ilzq@foxmail.com
 */

@SuppressWarnings("deprecation")
public class CameraHolder implements Camera.PreviewCallback {

    private static final String TAG = "CameraHolder";
    private static final int MAGIC_TEXTURE_ID = 28;
    private static final int MAX_UNSPECIFIED = -1;
    private static int IMAGE_FORMAT = ImageFormat.NV21;
    private static CameraHolder mInstance;


    private Camera mCamera;
    public int mCameraIndex;
    public int mFrameWidth;
    public int mFrameHeight;
    public int mMaxHeight;
    public int mMaxWidth;

    private CameraCallback mCameraCallback;
    private BufferProcessThread mBufferProcessThread;
    private SurfaceTexture mSurfaceTexture;
    private boolean mCanNotifyFrame;//是否可以向监听者传输buffer了，用以控制onComplete之后才进行传输
    private int mWindowDegree = 0;
    private boolean mInitialized = false;
    private int mCameraOrientation = 0;
    private ByteBuffer mFrameByteBuffer;
//    private final CameraOrientationTracker mOrientationTracker;
//    private int mLastOrientation = 0;

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

    private CameraHolder() {
        mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
        mBufferProcessThread = new BufferProcessThread();
        mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
        mBufferProcessThread.start();
//        mOrientationTracker = new CameraOrientationTracker(MyApp.getContext());
    }

    public void setCameraDegreeByWindowRotation(int windowRotation) {
        Log.d(TAG, "调整摄像机角度,windowRotation=" + windowRotation);
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
        if (mInitialized) {
            setCameraDispOri();
        }

    }

    public int getCameraOrientation() {
        return mCameraOrientation;
    }

    private void setCameraDispOri() {
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
            Log.d(TAG, "setCameraDispOri = " + mCameraOrientation);
        }
    }

    public SurfaceTexture getCameraSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void toggleCamera() {
        mBufferProcessThread.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                if (mInitialized) {
                    success = toggleCameraInternal();
                }else{
                    Log.e(TAG,"调整摄像头时，未初始化！");
                }
                if (mCameraCallback != null) {
                    final boolean finalSuccess = success;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCameraCallback != null) {
                                mCameraCallback.onToggleCameraComplete(finalSuccess, mCameraIndex);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * @param width    期望的宽
     * @param height   期望的高
     * @param callback 初始化结果的回调，将会执行在主线程
     */
    public void start(final int width, final int height, CameraCallback callback) {
        mMaxHeight = MAX_UNSPECIFIED;
        mMaxWidth = MAX_UNSPECIFIED;
        mCanNotifyFrame = false;
        mCameraCallback = callback;

        mBufferProcessThread.execute(new Runnable() {
            @Override
            public void run() {
                final boolean success = startInternal(width, height);
//                mOrientationTracker.enable();
                if (mCameraCallback != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCameraCallback != null) {
                                mCameraCallback.onCameraStarted(success, mFrameWidth, mFrameHeight, IMAGE_FORMAT);
                            }
                            mCanNotifyFrame = true;
                        }
                    });
                } else {
                    mCanNotifyFrame = true;
                }
            }
        });
    }

    private boolean startInternal(int width, int height) {
        boolean result = true;
        synchronized (this) {
            mCamera = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                int localCameraIndex = mCameraIndex;
                if (mCameraIndex == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Log.d(TAG, "尝试打开后置摄像头");
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
                        Camera.getCameraInfo(camIdx, cameraInfo);
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            localCameraIndex = camIdx;
                            break;
                        }
                    }
                } else if (mCameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Log.d(TAG, "尝试打开后置摄像头");
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
                        Camera.getCameraInfo(camIdx, cameraInfo);
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            localCameraIndex = camIdx;
                            break;
                        }
                    }
                }
                try {
                    mCamera = Camera.open(localCameraIndex);
                    if (localCameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        Log.d(TAG, "已打开前置摄像头");
                    } else {
                        Log.d(TAG, "已打开后置摄像头");
                    }
                } catch (RuntimeException e) {
                    Log.e(TAG, "摄像头 #" + localCameraIndex + "打开失败！: " + e.getLocalizedMessage());
                }
            }

            if (mCamera == null)
                return false;

            try {
                Camera.Parameters params = mCamera.getParameters();
                List<Camera.Size> sizes = params.getSupportedPreviewSizes();
                if (sizes != null) {
                    params.setPreviewFormat(IMAGE_FORMAT);
                    CameraSize frameSize = calculateCameraFrameSize(sizes, width, height);
                    Log.d(TAG, "预览设置为 " + frameSize.width + "x" + frameSize.height);
                    params.setPreviewSize(frameSize.width, frameSize.height);

                    //三星的机型也有问题，未知的问题机型较多，所以不使用
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !android.os.Build.MODEL.equals("GT-I9100"))
//                        params.setRecordingHint(true);

                    List<String> FocusModes = params.getSupportedFocusModes();
                    if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }
                    mCamera.setParameters(params);
                    params = mCamera.getParameters();

                    mFrameWidth = params.getPreviewSize().width;
                    mFrameHeight = params.getPreviewSize().height;
                    int size = mFrameWidth * mFrameHeight;
                    size = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                    mFrameByteBuffer = ByteBuffer.allocateDirect(size);

                    mCamera.addCallbackBuffer(mFrameByteBuffer.array());
                    mCamera.setPreviewCallbackWithBuffer(this);
                    mCamera.setPreviewTexture(mSurfaceTexture);

                    Log.d(TAG, "开始预览");
                    mCamera.startPreview();
                    setCameraDispOri();
                } else
                    result = false;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
        }
        mInitialized = result;
        return result;
    }

    private boolean toggleCameraInternal() {
        if (mCamera != null) {
            try {
                stopInternal();
            } catch (Throwable ignored) {
                Log.e(TAG,"调整摄像头时，关闭当前摄像头失败！",ignored);
                return false;
            }
            if (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraIndex) {
                mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
            } else {
                mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
            return startInternal(mFrameWidth, mFrameHeight);
        }
        return false;
    }

    public void stop() {
//        mOrientationTracker.disable();
        final CameraCallback finalCallback = mCameraCallback;
        mCameraCallback = null;
        mBufferProcessThread.execute(new Runnable() {
            @Override
            public void run() {
                stopInternal();
                if (finalCallback != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            finalCallback.onCameraStopped();
                        }
                    });
                }
            }
        });
    }

    private void stopInternal() {
        synchronized (this) {
            mInitialized = false;
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);

                mCamera.release();
            }
            mCamera = null;
            Log.d(TAG, "摄像机已释放");
//            if (mFrameChain != null) {
//                mFrameChain[0].release();
//                mFrameChain[1].release();
//            }
        }
    }

    /**
     * This helper method can be called by subclasses to select camera preview size.
     * It goes over the list of the supported preview sizes and selects the maximum one which
     * fits both values set via setMaxFrameSize() and surface frame allocated for this view
     *
     * @param supportedSizes
     * @param surfaceWidth
     * @param surfaceHeight
     * @return optimal frame size
     */
    private CameraSize calculateCameraFrameSize(List<Camera.Size> supportedSizes, int surfaceWidth, int surfaceHeight) {
        int calcWidth = 0;
        int calcHeight = 0;
        int maxAllowedWidth = (mMaxWidth != MAX_UNSPECIFIED && mMaxWidth < surfaceWidth) ? mMaxWidth : surfaceWidth;
        int maxAllowedHeight = (mMaxHeight != MAX_UNSPECIFIED && mMaxHeight < surfaceHeight) ? mMaxHeight : surfaceHeight;

        for (Camera.Size size : supportedSizes) {
            int width = size.width;
            int height = size.height;

            if (width <= maxAllowedWidth && height <= maxAllowedHeight) {
                if (width >= calcWidth && height >= calcHeight) {
                    calcWidth = width;
                    calcHeight = height;
                }
            }
        }

        return new CameraSize(calcWidth, calcHeight);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCameraCallback != null && mCanNotifyFrame) {
//            long spend = SystemClock.elapsedRealtime();
            mCameraCallback.onVideoBuffer(mFrameByteBuffer.array(), mCameraOrientation, mFrameWidth, mFrameHeight);
//            spend = SystemClock.elapsedRealtime() - spend;
//            Log.d(TAG, "分派一帧数据耗时:" + spend);
        }
//        synchronized (this) {
        if (mCamera != null) {
            mCamera.addCallbackBuffer(mFrameByteBuffer.array());
        }
//        }
    }

    public int getImageFormat() {
        return IMAGE_FORMAT;
    }

    public interface CameraCallback {
        /**
         * 将会执行在主线程
         *
         * @param success      true = 初始化成功
         * @param mFrameWidth  视频帧的宽度
         * @param mFrameHeight 视频帧的高度
         * @param imageFormat  目前只支持ImageFormat.NV21
         */
        void onCameraStarted(boolean success, int mFrameWidth, int mFrameHeight, int imageFormat);

        /**
         * 将回调在子线程
         *
         * @param frameBytes NV21类型
         */
        void onVideoBuffer(byte[] frameBytes, int frameDegree, int frameWidth, int frameHeight);

        /**
         * @param current one of Camera.CameraInfo.CAMERA_FACING_BACK 、Camera.CameraInfo.CAMERA_FACING_FRONT
         */
        void onToggleCameraComplete(boolean success, int current);

        void onCameraStopped();
    }

    private class BufferProcessThread extends HandlerThread {

        private Handler mHandler;
        private List<Runnable> taskToExecute;

        private BufferProcessThread() {
            super("BufferProcess", Process.THREAD_PRIORITY_URGENT_DISPLAY);
        }

        @Override
        protected void onLooperPrepared() {
            mHandler = new Handler(getLooper());
            if (taskToExecute != null) {
                synchronized (this) {
                    for (Runnable task : taskToExecute) {
                        mHandler.post(task);
                    }
                    taskToExecute = null;
                }
            }
        }

        void execute(Runnable task) {
            if (mHandler != null) {
                mHandler.post(task);
            } else {
                if (taskToExecute == null) {
                    synchronized (this) {
                        if (taskToExecute == null) {
                            taskToExecute = new ArrayList<>();
                        }
                    }
                }
                synchronized (this) {
                    taskToExecute.add(task);
                }
            }
        }

        public void close() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                quitSafely();
            } else {
                quit();
            }
        }
    }

//    private class CameraOrientationTracker extends OrientationEventListener {
//
//        public CameraOrientationTracker(Context context) {
//            super(context, SensorManager.SENSOR_DELAY_UI);
//        }
//
//        @Override
//        public void onOrientationChanged(int orientation) {
//            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
//                mLastOrientation = orientation;
//                return;
//            }
//
//            if (mLastOrientation < 0) {
//                mLastOrientation = 0;
//            }
//
//            if (((orientation - mLastOrientation) < 20)
//                    && ((orientation - mLastOrientation) > -20)) {
//                return;
//            }
//            mLastOrientation = orientation;
////            if(orientation % 90 == 0){
//            Log.d(TAG, "onOrientationChanged = " + orientation);
////            }
//        }
//    }

    private class CameraSize {
        int width;
        int height;

        public CameraSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
