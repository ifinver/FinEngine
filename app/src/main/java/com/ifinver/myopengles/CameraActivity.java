package com.ifinver.myopengles;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


/**
 * Created by iFinVer on 2016/11/15.
 * ilzq@foxmail.com
 */

public class CameraActivity extends AppCompatActivity implements CameraHolder.BufferCallback{
    private static final String TAG = "CameraActivity";

    private CameraHolder mCameraHolder;
    private Bitmap mCacheBitmap;
    private ImageView ivCanvas;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ivCanvas = (ImageView) findViewById(R.id.iv_canvas);
        mCameraHolder = new CameraHolder(getWindowManager().getDefaultDisplay().getRotation());
        mCameraHolder.setBufferCallback(this);
        ivCanvas.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mCameraHolder.init(ivCanvas.getWidth(), ivCanvas.getHeight(), new CameraHolder.InitCallback() {
                    @Override
                    public void onInitComplete(boolean success,int frameDegree, int mFrameWidth, int mFrameHeight, int imageFormat) {
                        Log.d(TAG, "摄像头初始化成功");
                        mCacheBitmap = Bitmap.createBitmap(mFrameWidth,mFrameHeight,Bitmap.Config.ARGB_8888);
                    }
                });
                ivCanvas.removeOnLayoutChangeListener(this);
            }
        });
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight) {
        Log.d(TAG, "收到视频数据,len=" + data.length);


        //摄像机数据
//        long spend = SystemClock.elapsedRealtime();
//        long nativeRgbMat = GLNative.processFrameMat(data.length, data);
//        Mat rgb = new Mat(nativeRgbMat);
//        try {
//            Utils.matToBitmap(rgb, mCacheBitmap);
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    ivCanvas.setImageBitmap(mCacheBitmap);
//                }
//            });
//        }catch (Throwable t){
//            t.printStackTrace();
//        }
//        spend = SystemClock.elapsedRealtime() - spend;
//        Log.d(TAG,"转换耗时:"+spend);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraHolder.deInit(new CameraHolder.ReleaseCallback() {
            @Override
            public void onReleaseComplete() {

            }
        });
    }
}
