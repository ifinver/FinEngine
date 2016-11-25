package com.ifinver.myopengles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;


/**
 * Created by iFinVer on 2016/11/15.
 * ilzq@foxmail.com
 */

public class CameraActivity extends AppCompatActivity implements CameraHolder.CameraCallback{
    private static final String TAG = "CameraActivity";

    private CameraHolder mCameraHolder;
    private TextureRenderer mRenderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        TextureView tex = (TextureView) findViewById(R.id.tex);
        mCameraHolder = CameraHolder.getInstance();
        mCameraHolder.setCameraDegreeByWindowRotation(getWindowManager().getDefaultDisplay().getRotation());
        mRenderer = new TextureRenderer(mCameraHolder.getImageFormat());
        tex.setSurfaceTextureListener(mRenderer);
    }

    @Override
    protected void onResume() {
        super.onResume();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mCameraHolder.start(displayMetrics.widthPixels, displayMetrics.heightPixels, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraHolder.stop();
    }

    @Override
    public void onCameraStarted(boolean success, int mFrameWidth, int mFrameHeight, int imageFormat) {

    }

    @Override
    public void onVideoBuffer(byte[] frameBytes,int frameDegree, int frameWidth, int frameHeight) {
        Log.d(TAG, "收到视频数据,len=" + frameBytes.length);

    }

    @Override
    public void onToggleCameraComplete(boolean success, int current) {

    }

    @Override
    public void onCameraStopped() {

    }
}
