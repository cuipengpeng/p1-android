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

public class LightenFilter extends AbstractFilter {

    public LightenFilter(Context context) {
        setName(context.getString(R.string.filter_lighten));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.LIGHTEN;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        List<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();

        GPUImageOverlayBlendFilter blendFilter = new GPUImageOverlayBlendFilter();
        blendFilter.setBitmap(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.filter_lighten_vignette));
        filterList.add(blendFilter);

        GPUImageToneCurveFilter toneCurve = new GPUImageToneCurveFilter();
        PointF[] redControlPoints = new PointF[] { new PointF(0.0f, 0.043f),
                new PointF(0.067f, 0.153f), new PointF(0.267f, 0.427f),
                new PointF(0.867f, 0.890f), new PointF(1.0f, 0.949f) };

        PointF[] greenControlPoints = new PointF[] { new PointF(0.0f, 0.039f),
                new PointF(0.067f, 0.141f), new PointF(0.2f, 0.325f),
                new PointF(0.867f, 0.839f), new PointF(1.0f, 0.894f) };

        PointF[] blueControlPoints = new PointF[] { new PointF(0.0f, 0.094f),
                new PointF(0.067f, 0.188f), new PointF(0.333f, 0.482f),
                new PointF(1.0f, 0.855f) };

        toneCurve.setRedControlPoints(redControlPoints);
        toneCurve.setGreenControlPoints(greenControlPoints);
        toneCurve.setBlueControlPoints(blueControlPoints);

        filterList.add(toneCurve);

        return new GPUImageFilterGroup(filterList);
    }

}
