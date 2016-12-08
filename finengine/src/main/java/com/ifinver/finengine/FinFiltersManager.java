package com.ifinver.finengine;

import java.util.ArrayList;
import java.util.List;

import static com.ifinver.finengine.FinEngine.FILTER_TYPE_CYAN;
import static com.ifinver.finengine.FinEngine.FILTER_TYPE_FISH_EYE;
import static com.ifinver.finengine.FinEngine.FILTER_TYPE_GREY_SCALE;
import static com.ifinver.finengine.FinEngine.FILTER_TYPE_H_MIRROR;
import static com.ifinver.finengine.FinEngine.FILTER_TYPE_NEGATIVE_COLOR;
import static com.ifinver.finengine.FinEngine.FILTER_TYPE_NORMAL;
import static com.ifinver.finengine.FinEngine.FILTER_TYPE_RADIAL_BLUR;
import static com.ifinver.finengine.FinEngine.FILTER_TYPE_SEPIA_STONE;
import static com.ifinver.finengine.FinEngine.FILTER_TYPE_V_MIRROR;

/**
 * Created by iFinVer on 2016/12/8.
 * ilzq@foxmail.com
 */
@SuppressWarnings("WeakerAccess")
public class FinFiltersManager {

    private static List<FilterDataModel> mSupportFilters;

    static {
        mSupportFilters = new ArrayList<>();
        FilterDataModel model;

        model = new FilterDataModel();
        model.filterName = "Normal";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_NORMAL;
        mSupportFilters.add(model);

        model = new FilterDataModel();
        model.filterName = "Cyan";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_CYAN;
        mSupportFilters.add(model);

        model = new FilterDataModel();
        model.filterName = "Grey scale";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_GREY_SCALE;
        mSupportFilters.add(model);

        model = new FilterDataModel();
        model.filterName = "Sepia stone";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_SEPIA_STONE;
        mSupportFilters.add(model);

        model = new FilterDataModel();
        model.filterName = "Negative color";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_NEGATIVE_COLOR;
        mSupportFilters.add(model);

        model = new FilterDataModel();
        model.filterName = "Fish eye";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_FISH_EYE;
        mSupportFilters.add(model);

        model = new FilterDataModel();
        model.filterName = "Radial blur";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_RADIAL_BLUR;
        mSupportFilters.add(model);

        model = new FilterDataModel();
        model.filterName = "Mirror h";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_H_MIRROR;
        mSupportFilters.add(model);

        model = new FilterDataModel();
        model.filterName = "Mirror v";
//        model.filterImageResId = R.mipmap.ic_launcher;
        model.filterType = FILTER_TYPE_V_MIRROR;
        mSupportFilters.add(model);
    }

    public static Shader findShader(int filterType) {
        String vertex;
        String fragment;
        switch (filterType) {
            default:
            case FinEngine.FILTER_TYPE_NORMAL:
                vertex = "";
                fragment = "";
                break;
            case FinEngine.FILTER_TYPE_CYAN:
                vertex = "vertex.glsl";
                fragment = "fragment_cyan.glsl";
                break;
            case FinEngine.FILTER_TYPE_FISH_EYE:
                vertex = "vertex.glsl";
                fragment = "fragment_fish_eye.glsl";
                break;
            case FinEngine.FILTER_TYPE_GREY_SCALE:
                vertex = "vertex.glsl";
                fragment = "fragment_grey.glsl";
                break;
            case FinEngine.FILTER_TYPE_NEGATIVE_COLOR:
                vertex = "vertex.glsl";
                fragment = "fragment_negative_color.glsl";
                break;
            case FinEngine.FILTER_TYPE_H_MIRROR:
                vertex = "vertex.glsl";
                fragment = "fragment_h_mirror.glsl";
                break;
            case FinEngine.FILTER_TYPE_RADIAL_BLUR:
                vertex = "vertex.glsl";
                fragment = "fragment_radial_blur.glsl";
                break;
            case FinEngine.FILTER_TYPE_SEPIA_STONE:
                vertex = "vertex.glsl";
                fragment = "fragment_sepia_stone.glsl";
                break;
            case FinEngine.FILTER_TYPE_V_MIRROR:
                vertex = "vertex.glsl";
                fragment = "fragment_v_mirror.glsl";
                break;
        }
        return new Shader(vertex,fragment);
    }

    public static List<FilterDataModel> getSupportFilters() {
        return mSupportFilters;
    }

    public static class Shader {
        public final String vertex;
        public final String fragment;
        public Shader(String ver, String frag){
            this.vertex = ver;
            this.fragment = frag;
        }
    }
}
