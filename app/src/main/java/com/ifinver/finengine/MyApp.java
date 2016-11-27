package com.ifinver.finengine;

import android.app.Application;

/**
 * Created by iFinVer on 2016/11/24.
 * ilzq@foxmail.com
 */

public class MyApp extends Application {
    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApp getContext(){return instance;}
}
