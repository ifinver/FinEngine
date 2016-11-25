package com.ifinver.myopengles.singleswitch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ifinver.myopengles.R;
import com.ifinver.myopengles.sdk.CameraHolder;
import com.ifinver.myopengles.sdk.TextureRenderer;


/**
 * Created by iFinVer on 2016/11/15.
 * ilzq@foxmail.com
 */

public class CameraActivity extends AppCompatActivity implements CameraHolder.CameraCallback, FilterAdapter.OnItemClickListener {
    private static final String TAG = "CameraActivity";

    private CameraHolder mCameraHolder;
    private TextureRenderer mRenderer;
    private LinearLayout llFilters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        TextureView tex = (TextureView) findViewById(R.id.tex);
        RecyclerView rvFilter = (RecyclerView) findViewById(R.id.rv_filter);
        llFilters = (LinearLayout) findViewById(R.id.ll_filters);

        mCameraHolder = CameraHolder.getInstance();
        mCameraHolder.setCameraDegreeByWindowRotation(getWindowManager().getDefaultDisplay().getRotation());
        mRenderer = new TextureRenderer(mCameraHolder.getImageFormat(),TextureRenderer.FILTER_TYPE_CYAN);
        tex.setSurfaceTextureListener(mRenderer);

        rvFilter.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        rvFilter.addItemDecoration(new SpaceItemDecoration(10));
        rvFilter.setAdapter(new FilterAdapter(this));


    }

    @Override
    public void onFilterItemClick(int filter) {
        Log.d(TAG,"onFilterItemClick,filter="+filter);
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
        llFilters.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideoBuffer(byte[] frameBytes,int frameDegree, int frameWidth, int frameHeight) {
        mRenderer.onVideoBuffer(frameBytes,frameDegree,frameWidth,frameHeight);

    }

    @Override
    public void onToggleCameraComplete(boolean success, int current) {

    }

    @Override
    public void onCameraStopped() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_camera:
                if (mCameraHolder != null) {
                    mCameraHolder.toggleCamera();
                    Toast.makeText(this, "switching", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
