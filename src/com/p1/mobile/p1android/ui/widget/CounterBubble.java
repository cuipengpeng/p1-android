package com.p1.mobile.p1android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.helpers.AbstractCounterUpdater;
import com.p1.mobile.p1android.ui.helpers.AbstractCounterUpdater.CounterListener;

public class CounterBubble extends RelativeLayout implements
        CounterListener {
    static final String TAG = CounterBubble.class.getSimpleName();

    private TextView mTextView;
    private int mCount = 0;
    private AbstractCounterUpdater mUpdater;

    public CounterBubble(Context context, AbstractCounterUpdater updater) {
        super(context);
        init(context);

        setCounterUpdater(updater);
    }

    public CounterBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public CounterBubble(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setCounterUpdater(AbstractCounterUpdater updater) {
        mUpdater = updater;
    }

    private void init(Context context) {

        mTextView = (TextView) LayoutInflater.from(context).inflate(
                R.layout.notifications_textview, null);
        addView(mTextView);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mUpdater.setCounterListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUpdater.onDestroy();
    }

    @Override
    public void onCounterUpdate(int count) {
        if (count < 0) {
            count = 0;
        }

        mCount = count;
        mTextView.setText(String.valueOf(mCount));

        if (mCount < 1) {
            mTextView.setVisibility(View.GONE);
        } else if (!mTextView.isShown()) {
            mTextView.setVisibility(View.VISIBLE);
            this.bringToFront();
        }
    }
}
