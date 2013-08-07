package com.p1.mobile.p1android.filter.impl;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageOverlayBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterType;

public class TwilightFilter extends AbstractFilter {

    public TwilightFilter(Context context) {
        setName(context.getString(R.string.filter_twilight));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.TWILIGHT;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        List<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();

        GPUImageOverlayBlendFilter blendFilter = new GPUImageOverlayBlendFilter();
        blendFilter.setBitmap(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.filter_twilight_vignette));
        filterList.add(blendFilter);

        PointF[] redControlPoints = new PointF[] { new PointF(0.0f, 0.067f),
                new PointF(0.067f, 0.145f), new PointF(0.267f, 0.490f),
                new PointF(0.533f, 0.725f), new PointF(0.933f, 0.914f),
                new PointF(1.0f, 0.933f) };

        PointF[] greenControlPoints = new PointF[] { new PointF(0.0f, 0.0f),
                new PointF(0.067f, 0.090f), new PointF(0.267f, 0.455f),
                new PointF(0.667f, 0.8f), new PointF(0.933f, 0.918f),
                new PointF(1.0f, 0.937f) };

        PointF[] blueControlPoints = new PointF[] { new PointF(0.0f, 0.063f),
                new PointF(0.067f, 0.161f), new PointF(0.267f, 0.537f),
                new PointF(0.4f, 0.647f), new PointF(0.733f, 0.780f),
                new PointF(1.0f, 0.922f) };

        GPUImageToneCurveFilter toneCurve = new GPUImageToneCurveFilter();
        toneCurve.setRedControlPoints(redControlPoints);
        toneCurve.setGreenControlPoints(greenControlPoints);
        toneCurve.setBlueControlPoints(blueControlPoints);
        filterList.add(toneCurve);

        return new GPUImageFilterGroup(filterList);

    }
}
