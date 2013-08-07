package com.p1.mobile.p1android.filter.impl;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageOverlayBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.AbstractFilter;
import com.p1.mobile.p1android.filter.FilterType;

public class FocusFilter extends AbstractFilter {

    public FocusFilter(Context context) {
        setName(context.getString(R.string.filter_focus));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FOCUS;
    }

    @Override
    protected GPUImageFilter initFilter(Context context) {
        List<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();

        GPUImageSharpenFilter sharpen = new GPUImageSharpenFilter();
        sharpen.setSharpness(0.1f);
        filterList.add(sharpen);

        GPUImageOverlayBlendFilter blendFilter = new GPUImageOverlayBlendFilter();
        blendFilter.setBitmap(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.filter_focus_vignette));
        filterList.add(blendFilter);

        GPUImageToneCurveFilter toneCurve = new GPUImageToneCurveFilter();
        PointF[] redControlPoints = new PointF[] { new PointF(0.0f, 0.0f),
                new PointF(0.173f, 0.11f), new PointF(0.502f, 0.514f),
                new PointF(0.733f, 0.82f), new PointF(1.0f, 1.0f) };

        PointF[] greenControlPoints = new PointF[] { new PointF(0.0f, 0.0f),
                new PointF(0.165f, 0.102f), new PointF(0.502f, 0.51f),
                new PointF(0.827f, 0.906f), new PointF(1.0f, 0.969f) };

        PointF[] blueControlPoints = new PointF[] { new PointF(0.0f, 0.078f),
                new PointF(1.0f, 0.961f) };

        toneCurve.setRedControlPoints(redControlPoints);
        toneCurve.setGreenControlPoints(greenControlPoints);
        toneCurve.setBlueControlPoints(blueControlPoints);

        filterList.add(toneCurve);

        return new GPUImageFilterGroup(filterList);
    }

}
