package com.p1.mobile.p1android.filter.impl;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterBuilder;
import com.p1.mobile.p1android.filter.FilterType;

public class ClairityFilter extends AbstractFilter {

    public ClairityFilter(Context context) {
        setName(context.getResources().getString(R.string.filter_clairity));
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        return new FilterBuilder.Builder().sharpen(0.3f).vignette(0.45f, 1.0f)
                .lookup(context, R.drawable.lookup_clarity).build();
    }

    @Override
    public FilterType getFilterType() {

        return FilterType.CLAIRITY;
    }

}
