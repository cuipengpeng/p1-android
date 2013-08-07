package com.p1.mobile.p1android.filter.impl;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageOverlayBlendFilter;
import android.content.Context;
import android.graphics.BitmapFactory;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterType;

public class SerenityFilter extends AbstractFilter {

    public SerenityFilter(Context context) {
        setName(context.getString(R.string.filter_serenity));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SERENITY;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        List<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();

        GPUImageOverlayBlendFilter blendFilter = new GPUImageOverlayBlendFilter();
        blendFilter.setBitmap(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.filter_serenity_vignette2));
        filterList.add(blendFilter);

        GPUImageLookupFilter lookup = new GPUImageLookupFilter();
        lookup.setBitmap(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.lookup_serenity));
        filterList.add(lookup);

        return new GPUImageFilterGroup(filterList);
    }

}
