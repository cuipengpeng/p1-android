package com.p1.mobile.p1android.filter.impl;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterType;

public class OriginalFilter extends AbstractFilter {

    public OriginalFilter(Context context) {
        setName(context.getString(R.string.filter_original));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.ORIGINAL;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        return new GPUImageFilter();
    }

}
