package com.ifinver.finenginesample.singleswitch;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.ifinver.finenginesample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iFinVer on 2016/11/25.
 * ilzq@foxmail.com
 */

class ModeAdapter extends RecyclerView.Adapter<FilterViewHolder> {

    private final Context mCtx;
    private List<String> mDataList;
    private OnItemClickListener mOnItemClickListener;

    public ModeAdapter(Context ctx, OnItemClickListener listener) {
        mOnItemClickListener = listener;
        mCtx = ctx;
        mDataList = new ArrayList<>();

        mDataList.add("无特效");
        mDataList.add("人脸交换");
        mDataList.add("蒙娜丽莎");
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View filterView = View.inflate(mCtx, R.layout.list_item_filter, null);
        return new FilterViewHolder(filterView);
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder holder, int position) {
        String name = mDataList.get(position);
        Resources resources = mCtx.getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources, bitmap);
        drawable.setCircular(true);
        holder.ivFilter.setImageDrawable(drawable);
        holder.tvFilter.setText(name);
        final int pos = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onModeClicked(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    interface OnItemClickListener {
        void onModeClicked(int position);
    }
}
