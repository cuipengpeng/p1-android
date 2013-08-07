package com.p1.mobile.p1android.filter.impl;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageHardLightBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterType;

public class SunsightFilter extends AbstractFilter {

    public SunsightFilter(Context context) {
        setName(context.getString(R.string.filter_sunsight));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SUNSIGHT;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        List<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();

        GPUImageToneCurveFilter toneCurve = new GPUImageToneCurveFilter();
        PointF[] redControlPoints = new PointF[] { new PointF(0.0f, 0.102f),
                new PointF(0.408f, 0.631f), new PointF(1.0f, 1.0f) };

        PointF[] greenControlPoints = new PointF[] { new PointF(0.0f, 0.0f),
                new PointF(0.235f, 0.333f), new PointF(0.6f, 0.773f),
                new PointF(1.0f, 0.969f) };

        PointF[] blueControlPoints = new PointF[] { new PointF(0.0f, 0.067f),
                new PointF(0.435f, 0.518f), new PointF(0.831f, 0.812f),
                new PointF(1.0f, 0.871f) };

        toneCurve.setRedControlPoints(redControlPoints);
        toneCurve.setGreenControlPoints(greenControlPoints);
        toneCurve.setBlueControlPoints(blueControlPoints);

        GPUImageHardLightBlendFilter blendFilter = new GPUImageHardLightBlendFilter();
        blendFilter.setBitmap(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.filter_sunsight_vignette));
        filterList.add(blendFilter);
        filterList.add(toneCurve);

        return new GPUImageFilterGroup(filterList);
    }

}
