package com.p1.mobile.p1android.ui.view;

import java.util.Arrays;
import java.util.TreeMap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;

public final class MulitOnTouchListView extends ListView implements
        OnTouchListener {

    TreeMap<String, OnTouchListener> ls = new TreeMap<String, OnTouchListener>();
    OnTouchListener[] lsa = new OnTouchListener[0];

    public MulitOnTouchListView(Context context) {
        super(context);
    }

    public MulitOnTouchListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MulitOnTouchListView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(this);
        if (l == null)
            return;
        ls.put(l.getClass().getSimpleName(), l);
        lsa = Arrays.copyOf(ls.values().toArray(), ls.values().size(),
                OnTouchListener[].class);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean r = false;
        for (OnTouchListener l : lsa)
            r = r || l.onTouch(v, event);
        return r;
    }

}
