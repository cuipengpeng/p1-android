package com.p1.mobile.p1android.filter;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import android.content.Context;
import android.widget.ImageView;

public interface Filter {
    public String getName();

    public void applyFilter(Context context, ImageView imageView);

    public void applyFilter(Context context, GPUImageView imageView);

    public FilterType getFilterType();

    void setName(String name);

    public GPUImageFilter getGPUFilter();

}
