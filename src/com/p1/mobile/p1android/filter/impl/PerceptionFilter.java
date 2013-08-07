package com.p1.mobile.p1android.filter.impl;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterBuilder;
import com.p1.mobile.p1android.filter.FilterType;

public class PerceptionFilter extends AbstractFilter {

    public PerceptionFilter(Context context) {
        setName(context.getString(R.string.filter_perception));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.PERCEPTION;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        return new FilterBuilder.Builder().sharpen(0.2f)
                .lookup(context, R.drawable.lookup_perception).build();
    }

}
