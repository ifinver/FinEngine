package com.ifinver.finenginesample.singleswitch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ifinver.finenginesample.R;


/**
 * Created by iFinVer on 2016/11/25.
 * ilzq@foxmail.com
 */

class FilterViewHolder extends RecyclerView.ViewHolder{

    ImageView ivFilter;
    TextView tvFilter;

    public FilterViewHolder(View itemView) {
        super(itemView);
        ivFilter = (ImageView) itemView.findViewById(R.id.iv_filter);
        tvFilter = (TextView) itemView.findViewById(R.id.tv_filter);
    }
}
