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
    public static final int CAMERA_ID_ANY = -1;
    public static final int CAMERA_ID_BACK = 99;
    public static final int CAMERA_ID_FRONT = 98;
    private static final int MAX_UNSPECIFIED = -1;
    private static int IMAGE_FORMAT = ImageFormat.NV21;

    private Camera mCamera;
    public int mCameraIndex;
    public int mFrameWidth;
    public int mFrameHeight;
    public int mMaxHeight;
    public int mMaxWidth;

    private byte mBuffer[];
    private BufferCallback mBufferCallback;
    private BufferProcessThread mBufferProcessThread;
    private InitCallback mInitCallback;
    private SurfaceTexture mSurfaceTexture;
    private boolean mCanNotifyFrame;//是否可以向监听者传输buffer了，用以控制onComplete之后才进行传输

    public CameraHolder() {}

    public void setBufferCallback(BufferCallback callback) {
        this.mBufferCallback = callback;
    }

    public SurfaceTexture getCameraSurfaceTexture(){return mSurfaceTexture;}

    /**
     * @param width    期望的宽
     * @param height   期望的高
     * @param callback 初始化结果的回调，将会执行在主线程
     */
    public void init(final int width, final int height, InitCallback callback) {
        mMaxHeight = MAX_UNSPECIFIED;
        mMaxWidth = MAX_UNSPECIFIED;
        mCameraIndex = CAMERA_ID_FRONT;
        mCanNotifyFrame = false;

        mBufferProcessThread = new BufferProcessThread();
        mBufferProcessThread.start();

        mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);

        this.mInitCallback = callback;
        mBufferProcessThread.execute(new Runnable() {
            @Override
            public void run() {
                final boolean success = initInternal(width, height);
                if (mInitCallback != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mInitCallback != null) {
                                mInitCallback.onInitComplete(success, mFrameWidth, mFrameHeight, IMAGE_FORMAT);
                                mCanNotifyFrame = true;
                            }
                        }
                    });
                } else {
                    mCanNotifyFrame = true;
                }
            }
        });
    }

    private boolean initInternal(int width, int height) {
        Log.d(TAG, "Initialize java camera");
        boolean result = true;
        synchronized (this) {
            mCamera = null;

            if (mCameraIndex == CAMERA_ID_ANY) {
                Log.d(TAG, "Trying to open camera with old open()");
                try {
                    mCamera = Camera.open();
                } catch (Exception e) {
                    Log.e(TAG, "Camera is not available (in use or does not exist): " + e.getLocalizedMessage());
                }

                if (mCamera == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    boolean connected = false;
                    for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
                        Log.d(TAG, "Trying to open camera with new open(" + Integer.valueOf(camIdx) + ")");
                        try {
                            mCamera = Camera.open(camIdx);
                            connected = true;
                        } catch (RuntimeException e) {
                            Log.e(TAG, "Camera #" + camIdx + "failed to open: " + e.getLocalizedMessage());
                        }
                        if (connected) break;
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    int localCameraIndex = mCameraIndex;
                    if (mCameraIndex == CAMERA_ID_BACK) {
                        Log.i(TAG, "Trying to open back camera");
                        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                        for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
                            Camera.getCameraInfo(camIdx, cameraInfo);
                            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                localCameraIndex = camIdx;
                                break;
                            }
                        }
                    } else if (mCameraIndex == CAMERA_ID_FRONT) {
                        Log.i(TAG, "Trying to open front camera");
                        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                        for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
                            Camera.getCameraInfo(camIdx, cameraInfo);
                            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                                localCameraIndex = camIdx;
                                break;
                            }
                        }
                    }
                    if (localCameraIndex == CAMERA_ID_BACK) {
                        Log.e(TAG, "Back camera not found!");
                    } else if (localCameraIndex == CAMERA_ID_FRONT) {
                        Log.e(TAG, "Front camera not found!");
                    } else {
                        Log.d(TAG, "Trying to open camera with new open(" + localCameraIndex + ")");
                        try {
                            mCamera = Camera.open(localCameraIndex);
                        } catch (RuntimeException e) {
                            Log.e(TAG, "Camera #" + localCameraIndex + "failed to open: " + e.getLocalizedMessage());
                        }
                    }
                }
            }

            if (mCamera == null)
                return false;

            /* Now set camera parameters */
            try {
                Camera.Parameters params = mCamera.getParameters();
                Log.d(TAG, "getSupportedPreviewSizes()");
                List<Camera.Size> sizes = params.getSupportedPreviewSizes();

                if (sizes != null) {
                    /* Select the size that fits surface considering maximum size allowed */
                    CameraSize frameSize = calculateCameraFrameSize(sizes, width, height);

                    params.setPreviewFormat(IMAGE_FORMAT);
                    Log.d(TAG, "Set preview size to " + frameSize.width + "x" + frameSize.height);
                    params.setPreviewSize(frameSize.width, frameSize.height);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !android.os.Build.MODEL.equals("GT-I9100"))
                        params.setRecordingHint(true);

                    List<String> FocusModes = params.getSupportedFocusModes();
                    if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }
                    mCamera.setParameters(params);
                    params = mCamera.getParameters();

                    mFrameWidth = params.getPreviewSize().width;
                    mFrameHeight = params.getPreviewSize().height;

                    //考虑着视频裁剪的事应该放在U3d里面
//                    if ((getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) && (getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT))
//                        mScale = Math.min(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
//                    else
//                        mScale = 0;

                    int size = mFrameWidth * mFrameHeight;
                    size = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                    mBuffer = new byte[size];

                    mCamera.addCallbackBuffer(mBuffer);
                    mCamera.setPreviewCallbackWithBuffer(this);

//                    mFrameChain = new Mat[2];
//                    mFrameChain[0] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);
//                    mFrameChain[1] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);

                    mCamera.setPreviewTexture(mSurfaceTexture);


//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                        mCamera.setPreviewTexture(new SurfaceTexture(MAGIC_TEXTURE_ID));
//                    } else
//                        mCamera.setPreviewDisplay(null);

                    /* Finally we are ready to start the preview */
                    Log.d(TAG, "startPreview");
                    mCamera.startPreview();
                } else
                    result = false;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
        }

        return result;
    }

    public void deInit(final ReleaseCallback callback) {
        mBufferProcessThread.execute(new Runnable() {
            @Override
            public void run() {
                deInitInternal();
                mBufferProcessThread.close();
                mBufferProcessThread = null;
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onReleaseComplete();
                        }
                    });
                }
            }
        });

    }

    private void deInitInternal() {
        synchronized (this) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);

                mCamera.release();
            }
            mCamera = null;
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
        if (mBufferCallback != null && mCanNotifyFrame) {
            mBufferCallback.onVideoBuffer(data,mFrameWidth,mFrameHeight,IMAGE_FORMAT);
        }
        if (mCamera != null)
            mCamera.addCallbackBuffer(mBuffer);
    }

    public interface InitCallback {
        /**
         * 将会执行在主线程
         *
         * @param success      true = 初始化成功
         * @param mFrameWidth  视频帧的宽度
         * @param mFrameHeight 视频帧的高度
         * @param imageFormat  目前只支持ImageFormat.NV21
         */
        void onInitComplete(boolean success, int mFrameWidth, int mFrameHeight, int imageFormat);
    }

    public interface ReleaseCallback {
        void onReleaseComplete();
    }

    public interface BufferCallback {
        /**
         * 将回调在子线程
         *
         * @param data NV21类型
         */
        void onVideoBuffer(byte[] data,int frameWidth,int frameHeight,int imageFormat);
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

        public void execute(Runnable task) {
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

    private class CameraSize {
        int width;
        int height;

        public CameraSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
