package com.ifinver.finenginesample.multirender;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifinver.finengine.CameraHolder;
import com.ifinver.finenginesample.FrameMeter;
import com.ifinver.finenginesample.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iFinVer on 2016/11/30.
 * ilzq@foxmail.com
 */

public class MultiActivity extends AppCompatActivity  {

    private static final String TAG = "MultiActivity";

//    private TextureRenderer[] mRenderer;
    //帧率
    private FrameMeter mFrameMeter;
    private TextView tvFPS;
    private Handler mHandler;
    private Timer mFpsTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);

        TextureView[] tvContent = new TextureView[4];
        tvContent[0] = (TextureView) findViewById(R.id.av_0);
        tvContent[1] = (TextureView) findViewById(R.id.av_1);
        tvContent[2] = (TextureView) findViewById(R.id.av_2);
        tvContent[3] = (TextureView) findViewById(R.id.av_3);

//        mRenderer = new TextureRenderer[4];
//        for(int i = 0;i < 4;i ++){
//            mRenderer[i] = new TextureRenderer();
//            tvContent[i].setSurfaceTextureListener(mRenderer[i]);
//        }

        //帧率
        mHandler = new Handler();
        mFpsTimer = new Timer(true);
        mFrameMeter = new FrameMeter();
        tvFPS = (TextView) findViewById(R.id.tv_fps);
        mFpsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvFPS.setText(getString(R.string.tip_fps,mFrameMeter.getFPSString()));
                    }
                });
            }
        }, 1000, 300);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
