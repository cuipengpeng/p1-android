/**
 * SquareImageView.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Imageview automatically scaling hight to match width. Thus making it square.
 * 
 * 
 * @author Viktor Nyblom
 * 
 */
public class P1GridView extends GridView {

    public P1GridView(Context context) {
        super(context);
    }

    public P1GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public P1GridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // HACK! TAKE THAT ANDROID!
        if (isExpanded()) {
            // Calculate entire height by providing a very large height hint.
            // But do not use the highest 2 bits of this integer; those are
            // reserved for the MeasureSpec mode.
            int expandSpec = MeasureSpec.makeMeasureSpec(
                    Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
            getLayoutParams().height = getMeasuredHeight();
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private boolean isExpanded() {
        // TODO Auto-generated method stub
        return true;
    }

}
