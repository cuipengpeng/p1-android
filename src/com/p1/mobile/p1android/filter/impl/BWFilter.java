package com.p1.mobile.p1android.filter.impl;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterBuilder;
import com.p1.mobile.p1android.filter.FilterType;

public class BWFilter extends AbstractFilter {

    public BWFilter(Context context) {
        setName(context.getString(R.string.filter_bw));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.BW;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        return new FilterBuilder.Builder().vignette(0.45f, 0.9f)
                .lookup(context, R.drawable.lookup_bw).build();
    }

}
