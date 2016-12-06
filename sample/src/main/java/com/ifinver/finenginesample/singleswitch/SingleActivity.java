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
import com.ifinver.finenginesample.Renderer;
import com.ifinver.finengine.CameraHolder;

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
    private Renderer mRenderer;
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
        final TextureView tvRender = (TextureView) findViewById(R.id.tex);
        mRenderer = new Renderer();
        tvRender.setSurfaceTextureListener(mRenderer);

//        tvRender.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        FinRender.getInstance().recording(mRenderer.getSurfaceTexture());
//                        break;
//                    case MotionEvent.ACTION_POINTER_UP:
//                        FinRender.getInstance().stopRecording();
//                        break;
//                }
//                return true;
//            }
//        });

        RecyclerView rvFilter = (RecyclerView) findViewById(R.id.rv_filter);
        rvFilter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFilter.addItemDecoration(new SpaceItemDecoration(10));
        rvFilter.setAdapter(new FilterAdapter(this,this));

//        TextureView tvLittle = (TextureView) findViewById(R.id.tex_l);
//        tvLittle.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                FinRender.getInstance().prepare(new Surface(surface));
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                FinRender.getInstance().release();
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            }
//        });
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
        mRenderer.onCameraStart(frameWidth,frameHeight);
    }

    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean frontCurrent) {
        mRenderer.onVideoBuffer(data, frameWidth, frameHeight,degree,frontCurrent);

        //计算帧率
        mFrameMeter.meter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
