package com.p1.mobile.p1android.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.filter.Filter;

/**
 * Button taking the argument of a Bitmap, a filter and a title and then applies
 * the filter to the bitmap.
 * 
 * @author Viktor Nyblom
 * 
 */
@SuppressLint("ViewConstructor")
public class FilterButton extends LinearLayout {

    private static final String TAG = FilterButton.class.getSimpleName();
    private Filter mFilter;
    private ImageView mImageView;

    public FilterButton(Context context, Filter filter) {
        this(context, null, filter);
    }

    public FilterButton(Context context, Bitmap imageBitmap, Filter filter) {
        super(context, null);

        mFilter = filter;

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.filter_button_layout, null);

        mImageView = (ImageView) layout
                .findViewById(R.id.filterButtonImageView);
        mImageView.setScaleType(ScaleType.CENTER_CROP);

        if (imageBitmap != null) {
            mImageView.setImageBitmap(imageBitmap);

            filter.applyFilter(context, mImageView);
        }
        TextView textView = (TextView) layout
                .findViewById(R.id.filterButtonTextView);
        textView.setText(filter.getName());

        addView(layout);
    }

    public void setBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
        if (mFilter != null) {
            mFilter.applyFilter(getContext(), mImageView);
        }
    }

    public void setFilter(Filter filter) {
        mFilter = filter;
        if (mImageView != null) {
            mFilter.applyFilter(getContext(), mImageView);
        }
    }

    public Filter getFilter() {
        return mFilter;
    }

    public void setImage(Bitmap bitmap) {
        Log.d(TAG, "setImage");
        mImageView.setImageBitmap(bitmap);
        if (mFilter != null) {
            Log.d(TAG, "setImage no null");
            mFilter.applyFilter(getContext(), mImageView);
        }
    }

}
