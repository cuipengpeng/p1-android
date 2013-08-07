package com.p1.mobile.p1android.filter.impl;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterBuilder;
import com.p1.mobile.p1android.filter.FilterType;

public class SagaFilter extends AbstractFilter {

    public SagaFilter(Context context) {
        setName(context.getString(R.string.filter_saga));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SAGA;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        return new FilterBuilder.Builder()
                .lookup(context, R.drawable.lookup_saga).vignette(0.60f, 0.9f)
                .build();
    }

}
