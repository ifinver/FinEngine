package com.ifinver.finengine.offscreen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

import com.ifinver.finengine.R;

/**
 * Created by iFinVer on 2016/11/28.
 * ilzq@foxmail.com
 */

public class OffScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offscreen);
        TextureView tvRender = (TextureView) findViewById(R.id.tex);

    }
}
