package com.p1.mobile.p1android.filter.impl;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterBuilder;
import com.p1.mobile.p1android.filter.FilterType;

public class LomoFilter extends AbstractFilter {

    public LomoFilter(Context context) {
        setName(context.getString(R.string.filter_lomo));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.LOMO;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        return new FilterBuilder.Builder().vignette(0.3f, 0.85f)
                .lookup(context, R.drawable.lookup_lomo).build();
    }

}
