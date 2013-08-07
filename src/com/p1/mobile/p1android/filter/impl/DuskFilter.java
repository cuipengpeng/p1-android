package com.p1.mobile.p1android.filter.impl;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterBuilder;
import com.p1.mobile.p1android.filter.FilterType;

public class DuskFilter extends AbstractFilter {

    public DuskFilter(Context context) {
        setName(context.getString(R.string.filter_dusk));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.DUSK;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        return new FilterBuilder.Builder().vignette(0.3f, 0.95f)
                .lookup(context, R.drawable.lookup_dusk).build();
    }

}
