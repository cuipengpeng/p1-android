/**
 * SquareImageView.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Imageview automatically scaling hight to match width. Thus making it square.
 * 
 * 
 * @author Viktor Nyblom
 * 
 */
public class SquareImageView extends OptimizedImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
