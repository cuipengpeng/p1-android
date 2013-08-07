/**
 * AspectRatioImageView.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * ImageView that automatically scales with and height to match parent view
 * while keeping the aspect ratio of the original image
 * 
 * @author Viktor Nyblom
 * 
 */
public class AspectRatioImageView extends OptimizedImageView {

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int diw = drawable.getIntrinsicWidth();
            if (diw > 0) {
                int height = width * drawable.getIntrinsicHeight() / diw;
                setMeasuredDimension(width, height);
            } else
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}