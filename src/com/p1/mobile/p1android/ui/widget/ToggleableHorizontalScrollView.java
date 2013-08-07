package com.p1.mobile.p1android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.p1.mobile.p1android.R;

public class ToggleableHorizontalScrollView extends HorizontalScrollView
        implements OnClickListener {

    public interface OnItemSelectedListener {
        public void onItemSelected(View view);
    }

    private View mSelectedView;
    private LinearLayout mChildLayout;
    private OnItemSelectedListener mListener;

    public ToggleableHorizontalScrollView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public ToggleableHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public ToggleableHorizontalScrollView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }

    private void init(Context context) {
        mChildLayout = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.toggleable_scroll_layout, null);
        this.addView(mChildLayout);
    }

    public void addItem(View view) {
        view.setOnClickListener(this);
        mChildLayout.addView(view);
    }

    public View getItem(Object tag) {
        return mChildLayout.findViewWithTag(tag);
    }

    @Override
    public void onClick(View view) {
        if (mSelectedView != null) {
            mSelectedView.setSelected(false);
        }
        mSelectedView = view;
        mSelectedView.setSelected(true);

        if (mListener != null) {
            mListener.onItemSelected(mSelectedView);
        }

    }

    public void setSelectedItem(int index) {
        View childView = mChildLayout.getChildAt(index);
        if (childView != null) {
            mSelectedView = childView;
            childView.setSelected(true);
        }
    }
}
