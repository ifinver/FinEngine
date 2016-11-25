package com.ifinver.myopengles.singleswitch;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by iFinVer on 2016/11/25.
 * ilzq@foxmail.com
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration{

    private int space;

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.top = 0;
        outRect.bottom = 0;
        outRect.right = space;
    }
}