package com.p1.mobile.p1android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.p1.mobile.p1android.R;

/**
 * Custom {@link android.widget.Button Button} using the Helvetica Neue font
 * 
 * @author Viktor Nyblom
 * 
 */
public class P1Button extends Button {

    public P1Button(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public P1Button(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public P1Button(Context context) {
        super(context);
        init(context, null, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        // Typeface.createFromAssets does not work in the layout editor
        if (!isInEditMode()) {
            int typefaceValue = 0;
            if (attrs != null) {
                TypedArray values = context.obtainStyledAttributes(attrs,
                        R.styleable.P1TextView, defStyle, 0);
                typefaceValue = values.getInt(R.styleable.P1TextView_typeface,
                        0);
                values.recycle();
            }

            Typeface typeface = P1TypefaceManager.getTypeface(context,
                    typefaceValue);
            setTypeface(typeface);

            // This flag is required to make the font render properly
            this.setPaintFlags(this.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
    }
}