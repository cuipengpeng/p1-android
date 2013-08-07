package com.p1.mobile.p1android.filter.impl;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSoftLightBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterType;

public class RetroFilter extends AbstractFilter {

    public RetroFilter(Context context) {
        setName(context.getString(R.string.filter_retro));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.RETRO;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        List<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();

        GPUImageSoftLightBlendFilter blendFilter = new GPUImageSoftLightBlendFilter();
        blendFilter.setBitmap(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.filter_retro_vignette));
        filterList.add(blendFilter);

        GPUImageToneCurveFilter toneCurve = new GPUImageToneCurveFilter();
        PointF[] redControlPoints = new PointF[] { new PointF(0.0f, 0.145f),
                new PointF(0.063f, 0.153f), new PointF(0.251f, 0.278f),
                new PointF(0.573f, 0.776f), new PointF(0.624f, 0.863f),
                new PointF(0.682f, 0.922f), new PointF(0.792f, 0.965f),
                new PointF(1.0f, 1.0f) };

        PointF[] greenControlPoints = new PointF[] { new PointF(0.0f, 0.0f),
                new PointF(0.255f, 0.196f), new PointF(0.447f, 0.576f),
                new PointF(0.686f, 0.875f), new PointF(1.0f, 1.0f) };

        PointF[] blueControlPoints = new PointF[] { new PointF(0.0f, 0.137f),
                new PointF(0.251f, 0.251f), new PointF(0.345f, 0.376f),
                new PointF(0.608f, 0.698f), new PointF(0.890f, 0.91f),
                new PointF(1.0f, 0.941f) };

        toneCurve.setRedControlPoints(redControlPoints);
        toneCurve.setGreenControlPoints(greenControlPoints);
        toneCurve.setBlueControlPoints(blueControlPoints);

        filterList.add(toneCurve);

        GPUImageSaturationFilter saturation = new GPUImageSaturationFilter();
        saturation.setSaturation(0.6f);
        filterList.add(saturation);

        return new GPUImageFilterGroup(filterList);
    }

}
