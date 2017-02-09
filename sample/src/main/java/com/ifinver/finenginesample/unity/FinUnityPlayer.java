package com.ifinver.finenginesample.unity;

import android.content.ContextWrapper;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

/**
 * Created by iFinVer on 2016/12/13.
 * ilzq@foxmail.com
 */

public class FinUnityPlayer extends UnityPlayer {
    public FinUnityPlayer(ContextWrapper contextWrapper) {
        super(contextWrapper);
    }

    @Override
    protected void kill() {
        //super.kill() will kill process
        super.kill();
    }

    public void MonaLisaMode(String off){
        if("on".equals(off)){
            Log.e("FinEngine","MonaLisaMode on");
        }else if("off".equals(off)){
            Log.e("FinEngine","MonaLisaMode off");
        }
    }

}
