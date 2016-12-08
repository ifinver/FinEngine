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

import com.ifinver.finengine.FilterDataModel;
import com.ifinver.finengine.FinEngine;
import com.ifinver.finengine.FinFiltersManager;
import com.ifinver.finenginesample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iFinVer on 2016/11/25.
 * ilzq@foxmail.com
 */

class FilterAdapter extends RecyclerView.Adapter<FilterViewHolder> {

    private final Context mCtx;
    private List<FilterDataModel> mDataList;
    private OnItemClickListener mOnItemClickListener;

    public FilterAdapter(Context ctx, OnItemClickListener listener) {
        mOnItemClickListener = listener;
        mCtx = ctx;
        mDataList = new ArrayList<>(FinFiltersManager.getSupportFilters());

        FilterDataModel model = new FilterDataModel();
        model.filterName = "Coming soon";
        model.filterImageResId = R.mipmap.coming_soon;
        model.filterType = FinEngine.FILTER_TYPE_NORMAL;
        mDataList.add(model);
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View filterView = View.inflate(mCtx, R.layout.list_item_filter, null);
        return new FilterViewHolder(filterView);
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder holder, int position) {
        final FilterDataModel filterDataModel = mDataList.get(position);
        Resources resources = mCtx.getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, filterDataModel.filterImageResId == 0?R.mipmap.ic_launcher : filterDataModel.filterImageResId);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources, bitmap);
        drawable.setCircular(true);
        holder.ivFilter.setImageDrawable(drawable);
        holder.tvFilter.setText(filterDataModel.filterName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onFilterItemClick(filterDataModel.filterType);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    interface OnItemClickListener {
        void onFilterItemClick(int filter);
    }
}
