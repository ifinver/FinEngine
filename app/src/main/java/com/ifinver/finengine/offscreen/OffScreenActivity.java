package com.ifinver.finengine.offscreen;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.TextureView;

import com.ifinver.finengine.R;
import com.ifinver.finengine.sdk.CameraHolder;
import com.ifinver.finengine.sdk.FinEngine;

/**
 * Created by iFinVer on 2016/11/28.
 * ilzq@foxmail.com
 */

public class OffScreenActivity extends AppCompatActivity implements CameraHolder.CameraCallback {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offscreen);
        TextureView tvRender = (TextureView) findViewById(R.id.tex);
//        tvRender.setSurfaceTextureListener(new OffScreenRenderer(ImageFormat.NV21,OffScreenRenderer.FILTER_TYPE_CYAN));
    }

    @Override
    protected void onResume() {
        super.onResume();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        CameraHolder.getInstance().start(dm.widthPixels,dm.heightPixels,false,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FinEngine.getInstance().stopEngine();
        CameraHolder.getInstance().stop();
    }

    @Override
    public void onCameraStarted(boolean success, int mFrameWidth, int mFrameHeight, int imageFormat) {
        FinEngine.getInstance().startEngine(imageFormat,mFrameWidth,mFrameHeight,FinEngine.FILTER_TYPE_NORMAL);
    }

    @Override
    public void onVideoBuffer(byte[] frameBytes, int frameDegree, int frameWidth, int frameHeight) {}

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture, int frameDegree) {
        FinEngine.getInstance().notifyProcess(surfaceTexture,frameDegree);
    }

    @Override
    public void onToggleCameraComplete(boolean success, int current) {

    }

    @Override
    public void onCameraStopped() {

    }
}
