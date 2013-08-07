package com.p1.mobile.p1android.filter.impl;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSoftLightBlendFilter;
import android.content.Context;
import android.graphics.BitmapFactory;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterType;

public class LuminanceFilter extends AbstractFilter {

    public LuminanceFilter(Context context) {
        setName(context.getString(R.string.filter_luminance));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.LUMINANCE;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        List<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();

        GPUImageLookupFilter lookup = new GPUImageLookupFilter();
        lookup.setBitmap(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.lookup_luminance));
        filterList.add(lookup);

        GPUImageSoftLightBlendFilter blendFilter = new GPUImageSoftLightBlendFilter();
        blendFilter.setBitmap(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.filter_luminance_vignette));
        filterList.add(blendFilter);

        return new GPUImageFilterGroup(filterList);
    }

}
