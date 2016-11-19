package com.ifinver.myopengles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

/**
 * Created by iFinVer on 2016/11/16.
 * ilzq@foxmail.com
 */

public class OpenGLActivity extends AppCompatActivity {

    private FrameLayout[] mFlContainer = new FrameLayout[4];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl);

        mFlContainer[0] = (FrameLayout) findViewById(R.id.fl_content_0);
        mFlContainer[1] = (FrameLayout) findViewById(R.id.fl_content_1);
        mFlContainer[2] = (FrameLayout) findViewById(R.id.fl_content_2);
        mFlContainer[3] = (FrameLayout) findViewById(R.id.fl_content_3);
    }


}
