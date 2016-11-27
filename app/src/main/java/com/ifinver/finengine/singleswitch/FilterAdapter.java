package com.ifinver.finengine.singleswitch;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.ifinver.finengine.MyApp;
import com.ifinver.finengine.R;
import com.ifinver.finengine.sdk.TextureRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iFinVer on 2016/11/25.
 * ilzq@foxmail.com
 */

class FilterAdapter extends RecyclerView.Adapter<FilterViewHolder>{

    private List<FilterDataModel> mDataList;
    private OnItemClickListener mOnItemClickListener;

    public FilterAdapter(OnItemClickListener listener){
        mOnItemClickListener = listener;
        mDataList = new ArrayList<>();
        FilterDataModel model;

        model = new FilterDataModel();
        model.filterName = "Normal";
        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = TextureRenderer.FILTER_TYPE_NORMAL;
        mDataList.add(model);

        model = new FilterDataModel();
        model.filterName = "Cyan";
        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = TextureRenderer.FILTER_TYPE_CYAN;
        mDataList.add(model);

        model = new FilterDataModel();
        model.filterName = "Fish eye";
        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = TextureRenderer.FILTER_TYPE_FISH_EYE;
        mDataList.add(model);

        model = new FilterDataModel();
        model.filterName = "Grey scale";
        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = TextureRenderer.FILTER_TYPE_GREY_SCALE;
        mDataList.add(model);

        model = new FilterDataModel();
        model.filterName = "Negative123456789color";
        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = TextureRenderer.FILTER_TYPE_NEGATIVE_COLOR;
        mDataList.add(model);

        for(int i = 0;i < 25;i ++){
            model = new FilterDataModel();
            model.filterName = "Coming soon";
            model.filterImageResId = R.mipmap.coming_soon;
            model.filterType = TextureRenderer.FILTER_TYPE_NORMAL;
            mDataList.add(model);
        }
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View filterView = View.inflate(MyApp.getContext(), R.layout.list_item_filter, null);
        return new FilterViewHolder(filterView);
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder holder, int position) {
        final FilterDataModel filterDataModel = mDataList.get(position);
        Resources resources = MyApp.getContext().getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, filterDataModel.filterImageResId);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources, bitmap);
        drawable.setCircular(true);
        holder.ivFilter.setImageDrawable(drawable);
        holder.tvFilter.setText(filterDataModel.filterName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onFilterItemClick(filterDataModel.filterType);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    interface OnItemClickListener{
        void onFilterItemClick(int filter);
    }
}
