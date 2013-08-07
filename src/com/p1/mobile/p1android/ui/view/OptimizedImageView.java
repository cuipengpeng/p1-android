/**
 * OptimizedImageView.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.p1.mobile.p1android.ui.listeners.OnViewMeasuredListener;

/**
 * 
 * ImageView using the optimization from Jelly Bean.
 * 
 * @author Viktor Nyblom
 * 
 */
public class OptimizedImageView extends ImageView {

    public static final String TAG = OptimizedImageView.class.getSimpleName();
    private boolean mIgnoreNextRequestLayout = false;
    private List<OnViewMeasuredListener> mMeasureListeners = new ArrayList<OnViewMeasuredListener>();

    public OptimizedImageView(Context context) {
        super(context);
    }

    public OptimizedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptimizedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageDrawable(final Drawable newDrawable) {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR1) {

            // The currently set Drawable
            final Drawable oldDrawable = getDrawable();

            if (null != oldDrawable && oldDrawable != newDrawable
                    && newDrawable != null) {
                final int oldWidth = oldDrawable.getIntrinsicWidth();
                final int oldHeight = oldDrawable.getIntrinsicHeight();

                /**
                 * Ignore the next requestLayout call if the new Drawable is the
                 * same size as the currently displayed one.
                 * */
                mIgnoreNextRequestLayout = oldHeight == newDrawable
                        .getIntrinsicHeight()
                        && oldWidth == newDrawable.getIntrinsicWidth();
            }
        }

        // Finally, call up to super
        super.setImageDrawable(newDrawable);
    }

    @Override
    public void requestLayout() {

        if (!mIgnoreNextRequestLayout) {
            super.requestLayout();
        }

        // Reset Flag so that the requestLayout() will work again

        mIgnoreNextRequestLayout = false;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (OnViewMeasuredListener listener : mMeasureListeners) {
            listener.onViewMeasured();
        }

    }

    public void addMeasureListener(OnViewMeasuredListener listener) {
        mMeasureListeners.add(listener);
    }

}
