package com.ifinver.finenginesample.singleswitch;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ifinver.finengine.FinEngine;
import com.ifinver.finengine.FinRender;
import com.ifinver.finengine.TextureRenderer;
import com.ifinver.finenginesample.R;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iFinVer on 2016/11/28.
 * ilzq@foxmail.com
 */

public class SingleActivity extends AppCompatActivity implements FinEngine.EngineListener, FilterAdapter.OnItemClickListener {

    private static final String TAG = "SingleActivity";

    private TextView tvFps;
    private long mFrameCount = 0;
    private long mStartTime = 0;
    private double fps = 0;
    private Timer mFpsTimer;
    private DecimalFormat df;
    private Handler mHandler;
    private TextureRenderer mRenderer;
    private LinearLayout llFilters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offscreen);
        tvFps = (TextView) findViewById(R.id.tv_fps);
        df = new DecimalFormat("#0");
        mHandler = new Handler();
        mFpsTimer = new Timer(true);
        mFpsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvFps.setText(getString(R.string.tip_fps, df.format(fps)));
                    }
                });
            }
        }, 1000, 300);
        TextureView tvRender = (TextureView) findViewById(R.id.tex);
        mRenderer = new TextureRenderer(FinRender.FORMAT_RGBA);
        tvRender.setSurfaceTextureListener(mRenderer);

        RecyclerView rvFilter = (RecyclerView) findViewById(R.id.rv_filter);
        llFilters = (LinearLayout) findViewById(R.id.ll_filters);
        rvFilter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFilter.addItemDecoration(new SpaceItemDecoration(10));
        rvFilter.setAdapter(new FilterAdapter(this));
    }

    @Override
    public void onFilterItemClick(int filter) {
        Log.d(TAG, "onFilterItemClick,filter=" + filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_camera:
                if(FinEngine.getInstance().toggleCamera()) {
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
        FinEngine.getInstance().startEngine(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FinEngine.getInstance().stopEngine();
    }

    @Override
    public void onToggleCameraComplete(boolean success) {
        Log.d(TAG, "onToggleCameraComplete,success= " + success);
    }

    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight) {
        Log.d(TAG, "onVideoBuffer,data[1025]=" + data[1025]);
        mRenderer.onVideoBuffer(data, frameWidth, frameHeight);

        //计算帧率
        mFrameCount++;
        if (mStartTime == 0) {
            mStartTime = SystemClock.elapsedRealtime();
        } else {
            long spend = SystemClock.elapsedRealtime() - mStartTime;
            fps = mFrameCount * 1000.0 / spend;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
