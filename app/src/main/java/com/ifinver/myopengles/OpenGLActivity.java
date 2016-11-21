package com.ifinver.myopengles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by iFinVer on 2016/11/16.
 * ilzq@foxmail.com
 */

public class OpenGLActivity extends AppCompatActivity implements CameraHolder.InitCallback, CameraHolder.ReleaseCallback, CameraHolder.BufferCallback {

    private CameraHolder mCameraHolder;
    private DisplayMetrics displayMetrics;
    private TextureRenderer[] mRenderer = new TextureRenderer[4];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl);

        FrameLayout fm[] = new FrameLayout[4];
        fm[0] = (FrameLayout) findViewById(R.id.av_0);
        fm[1] = (FrameLayout) findViewById(R.id.av_1);
        fm[2] = (FrameLayout) findViewById(R.id.av_2);
        fm[3] = (FrameLayout) findViewById(R.id.av_3);

        TextureView[] textureViews = new TextureView[4];
        for(int i = 0;i < fm.length;i++){
            textureViews[i] = new TextureView(this);
            fm[i].addView(textureViews[i],new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        mCameraHolder = new CameraHolder();
        mCameraHolder.setBufferCallback(this);

        mRenderer[0] = new TextureRenderer(mCameraHolder);
        mRenderer[1] = new TextureRenderer(mCameraHolder);
        mRenderer[2] = new TextureRenderer(mCameraHolder);
        mRenderer[3] = new TextureRenderer(mCameraHolder);

        for(int i = 0;i < 1;i ++){
            textureViews[i].setSurfaceTextureListener(mRenderer[i]);
        }

        displayMetrics = getResources().getDisplayMetrics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraHolder.init(displayMetrics.widthPixels,displayMetrics.heightPixels,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraHolder.deInit(this);
    }

    @Override
    public void onInitComplete(boolean success, int mFrameWidth, int mFrameHeight, int imageFormat) {

    }

    @Override
    public void onReleaseComplete() {

    }

    @Override
    public void onVideoBuffer(byte[] data,int frameWidth,int frameHeight,int imageFormat) {
        for (TextureRenderer aMRenderer : mRenderer) {
            aMRenderer.onVideoBuffer(data, frameWidth, frameHeight, imageFormat);
        }
    }
}
