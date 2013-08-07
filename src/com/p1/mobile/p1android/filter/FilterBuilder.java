package com.p1.mobile.p1android.filter;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSoftLightBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageVignetteFilter;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.Log;

public class FilterBuilder {
    static final String TAG = FilterBuilder.class.getSimpleName();

    private Context context;
    private GPUImageFilterGroup filterGroup;
    private List<GPUImageFilter> filterList = new ArrayList<GPUImageFilter>();
    private GPUImageSharpenFilter sharpenFilter;
    private GPUImageVignetteFilter vignetteFilter;
    private GPUImageLookupFilter lookupFilter;
    private GPUImageToneCurveFilter toneCurveFilter;
    private GPUImageSoftLightBlendFilter blendFilter;
    private GPUImageSaturationFilter saturationFilter;

    public static class Builder {
        private Context context;
        private float vignetteStart;
        private float vignetteEnd;
        private float sharpenFactor;
        private int lookupResource;
        private PointF[] redControlPoints;
        private PointF[] greenControlPoints;
        private PointF[] blueControlPoints;
        private boolean shouldBlend;
        private float saturation;

        public Builder vignette(float start, float end) {
            this.vignetteStart = start;
            this.vignetteEnd = end;
            return this;
        }

        public Builder sharpen(float sharpenFactor) {
            this.sharpenFactor = sharpenFactor;
            return this;
        }

        public Builder lookup(Context context, int resource) {
            this.context = context;
            this.lookupResource = resource;
            return this;
        }

        public Builder toneCurve(PointF[] redControlPoints,
                PointF[] greenControlPoints, PointF[] blueControlPoints) {
            this.redControlPoints = redControlPoints;
            this.greenControlPoints = greenControlPoints;
            this.blueControlPoints = blueControlPoints;
            return this;
        }

        public GPUImageFilterGroup build() {
            return new FilterBuilder(this).getFilter();
        }

        public Builder blend() {
            shouldBlend = true;
            return this;
        }

        public Builder saturation(float saturation) {

            return this;
        }
    }

    private FilterBuilder(final Builder builder) {
        this.context = builder.context;

        setBlend(builder);

        setVignette(builder);

        setSaturation(builder);

        setLookup(builder);

        setSharpnen(builder);

        setToneCurve(builder);
    }

    public GPUImageFilterGroup getFilter() {
        filterGroup = new GPUImageFilterGroup(filterList);
        return filterGroup;
    }

    private void setToneCurve(Builder builder) {
        Log.d(TAG, "ToneCurve red " + builder.redControlPoints);
        Log.d(TAG, "ToneCurve green " + builder.redControlPoints);
        Log.d(TAG, "ToneCurve blue " + builder.redControlPoints);
        if (builder.redControlPoints != null
                && builder.greenControlPoints != null
                && builder.blueControlPoints != null) {
            toneCurveFilter = new GPUImageToneCurveFilter();
            toneCurveFilter.setRedControlPoints(builder.redControlPoints);
            toneCurveFilter.setGreenControlPoints(builder.greenControlPoints);
            toneCurveFilter.setBlueControlPoints(builder.blueControlPoints);

            filterList.add(toneCurveFilter);
        }
    }

    private void setSharpnen(Builder builder) {
        Log.d(TAG, "Sharpen " + builder.sharpenFactor);
        if (builder.sharpenFactor != 0.0f) {
            sharpenFilter = new GPUImageSharpenFilter();
            sharpenFilter.setSharpness(builder.sharpenFactor);

            filterList.add(sharpenFilter);
        }
    }

    private void setVignette(Builder builder) {
        Log.d(TAG, "Vignette start " + builder.vignetteStart);
        Log.d(TAG, "Vignette end " + builder.vignetteEnd);
        if (builder.vignetteStart != 0.0f && builder.vignetteEnd != 0.0f) {
            Log.d(TAG, "Adding vignette");
            vignetteFilter = new GPUImageVignetteFilter();
            PointF centerPoint = new PointF();
            centerPoint.x = 0.5f;
            centerPoint.y = 0.5f;
            vignetteFilter.setVignetteCenter(centerPoint);
            vignetteFilter.setVignetteStart(builder.vignetteStart);
            vignetteFilter.setVignetteEnd(builder.vignetteEnd);

            filterList.add(vignetteFilter);
        }
    }

    private void setLookup(Builder builder) {
        Log.d(TAG, "Lookup " + builder.lookupResource);
        if (builder.lookupResource != 0) {
            this.lookupFilter = new GPUImageLookupFilter();

            lookupFilter.setBitmap(BitmapFactory.decodeResource(
                    context.getResources(), builder.lookupResource));

            filterList.add(lookupFilter);
        }
    }

    private void setBlend(Builder builder) {
        Log.d(TAG, "Blend " + builder.shouldBlend);
        if (builder.shouldBlend) {
            this.blendFilter = new GPUImageSoftLightBlendFilter();

            filterList.add(blendFilter);
        }
    }

    private void setSaturation(Builder builder) {
        Log.d(TAG, "Blend " + builder.shouldBlend);
        if (builder.saturation != 0.0f) {
            this.saturationFilter = new GPUImageSaturationFilter();

            saturationFilter.setSaturation(builder.saturation);

            filterList.add(saturationFilter);
        }
    }
}
