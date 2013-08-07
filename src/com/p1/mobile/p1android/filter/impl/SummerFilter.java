package com.p1.mobile.p1android.filter.impl;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;
import android.graphics.PointF;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterBuilder;
import com.p1.mobile.p1android.filter.FilterType;

public class SummerFilter extends AbstractFilter {

    public SummerFilter(Context context) {
        setName(context.getString(R.string.filter_summer));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SUMMER;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        PointF[] redControlPoints = new PointF[] { new PointF(0.0f, 0.078f),
                new PointF(0.341f, 0.475f), new PointF(0.835f, 0.882f),
                new PointF(1.0f, 0.949f) };

        PointF[] greenControlPoints = new PointF[] { new PointF(0.0f, 0.0f),
                new PointF(0.243f, 0.282f), new PointF(0.580f, 0.698f),
                new PointF(0.831f, 0.882f), new PointF(1.0f, 1.0f) };

        PointF[] blueControlPoints = new PointF[] { new PointF(0.0f, 0.067f),
                new PointF(0.490f, 0.565f), new PointF(0.761f, 0.773f),
                new PointF(1.0f, 0.910f) };

        return new FilterBuilder.Builder().toneCurve(redControlPoints,
                greenControlPoints, blueControlPoints).build();
    }

}
