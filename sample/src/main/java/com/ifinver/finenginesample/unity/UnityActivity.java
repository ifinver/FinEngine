package com.ifinver.finenginesample.unity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifinver.finengine.CameraHolder;
import com.ifinver.finenginesample.FrameMeter;
import com.ifinver.finenginesample.R;
import com.ifinver.unitytransfer.UnityTransfer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iFinVer on 2016/11/30.
 * ilzq@foxmail.com
 */

public class UnityActivity extends UnityBaseActivity implements SurfaceHolder.Callback, CameraHolder.CameraListener {

    private static final String TAG = "UnityActivity";

    //帧率
    private FrameMeter mFrameMeter;
    private Handler mHandler;
    private Timer mFpsTimer;
    private TextView tvFPS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);

        //find views
        tvFPS = (TextView) findViewById(R.id.tv_fps);
        SurfaceView unitySurface = (SurfaceView) findViewById(R.id.surface_unity);

        //unity
        initUnitySurface(unitySurface);

        //帧率
        initFPS();
    }

    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean frontCurrent) {
        UnityTransfer.onVideoBuffer(data,frameWidth,frameHeight,degree,frontCurrent);

        mFrameMeter.meter();
    }

    @Override
    public void onCameraStart(boolean success) {

    }

    @Override
    public void onToggleCameraComplete(boolean success) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        CameraHolder.getInstance().start(1280, 720, this, this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        CameraHolder.getInstance().stopCamera();
        super.onPause();
    }

    private void initFPS() {
        mHandler = new Handler();
        mFpsTimer = new Timer(true);
        mFrameMeter = new FrameMeter();
        mFpsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvFPS.setText(getString(R.string.tip_fps, mFrameMeter.getFPSString()));
                    }
                });
            }
        }, 1000, 300);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_camera:
                if (CameraHolder.getInstance().toggleCamera()) {
                    Toast.makeText(this, "switching", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "can't switch", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFpsTimer.cancel();
    }
}
