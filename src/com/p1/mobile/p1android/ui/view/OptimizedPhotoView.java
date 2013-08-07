package com.p1.mobile.p1android.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Basically a PhotoView with the new JellyBean Optimizations
 * 
 * @author Viktor Nyblom
 * 
 */
public class OptimizedPhotoView extends ImageView {
    private boolean mIgnoreNextRequestLayout = false;

    public OptimizedPhotoView(Context context) {
        super(context);
    }

    public OptimizedPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptimizedPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageDrawable(final Drawable newDrawable) {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR1) {

            // The currently set Drawable
            final Drawable oldDrawable = getDrawable();

            if (null != oldDrawable && oldDrawable != newDrawable) {
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

}
