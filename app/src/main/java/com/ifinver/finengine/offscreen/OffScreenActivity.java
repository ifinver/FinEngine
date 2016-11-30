package com.ifinver.finengine.offscreen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ifinver.finengine.R;
import com.ifinver.finengine.sdk.FinEngine;

/**
 * Created by iFinVer on 2016/11/28.
 * ilzq@foxmail.com
 */

public class OffScreenActivity extends AppCompatActivity implements FinEngine.OnVideoBufferListener {

    private static final String TAG = "OffScreenActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offscreen);
//        TextureView tvRender = (TextureView) findViewById(R.id.tex);
//        tvRender.setSurfaceTextureListener(new OffScreenRenderer(ImageFormat.NV21,OffScreenRenderer.FILTER_TYPE_CYAN));
    }


    @Override
    protected void onResume() {
        super.onResume();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        FinEngine.getInstance().startEngine(dm.widthPixels,dm.heightPixels,FinEngine.FILTER_TYPE_NORMAL,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FinEngine.getInstance().stopEngine();
    }

    @Override
    public void onVideoBuffer(byte[] data) {
        Log.d(TAG,"onVideoBuffer,data[1025]="+data[1025]);
    }
}
