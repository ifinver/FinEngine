package com.ifinver.finenginesample.singleswitch;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifinver.finenginesample.FrameMeter;
import com.ifinver.finenginesample.R;
import com.ifinver.finrender.CameraHolder;
import com.ifinver.finrender.FinRender;
import com.ifinver.finrender.TextureRenderer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iFinVer on 2016/11/28.
 * ilzq@foxmail.com
 */

public class SingleActivity extends AppCompatActivity implements FilterAdapter.OnItemClickListener, CameraHolder.CameraListener {

    private static final String TAG = "SingleActivity";

    private TextView tvFps;
    private Timer mFpsTimer;
    private Handler mHandler;
    private TextureRenderer mRenderer;
    private FrameMeter mFrameMeter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        tvFps = (TextView) findViewById(R.id.tv_fps);
        mHandler = new Handler();
        mFpsTimer = new Timer(true);
        mFrameMeter = new FrameMeter();
        mFpsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvFps.setText(getString(R.string.tip_fps,mFrameMeter.getFPSString()));
                    }
                });
            }
        }, 1000, 300);
        TextureView tvRender = (TextureView) findViewById(R.id.tex);
        mRenderer = new TextureRenderer(FinRender.FORMAT_NV21);
        tvRender.setSurfaceTextureListener(mRenderer);

        RecyclerView rvFilter = (RecyclerView) findViewById(R.id.rv_filter);
        rvFilter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFilter.addItemDecoration(new SpaceItemDecoration(10));
        rvFilter.setAdapter(new FilterAdapter(this,this));
    }

    @Override
    public void onFilterItemClick(int filter) {
        Log.d(TAG, "onFilterItemClick,filter=" + filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_camera:
                if(CameraHolder.getInstance().toggleCamera()) {
                    Toast.makeText(this, "switching", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "can't switch", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        CameraHolder.getInstance().start(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraHolder.getInstance().stopCamera();
    }

    @Override
    public void onToggleCameraComplete(boolean success) {
        Log.d(TAG, "onToggleCameraComplete,success= " + success);
    }

    @Override
    public void onCameraStart(boolean success, int frameWidth, int frameHeight) {

    }

    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight) {
        mRenderer.onVideoBuffer(data, frameWidth, frameHeight);

        //计算帧率
        mFrameMeter.meter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
