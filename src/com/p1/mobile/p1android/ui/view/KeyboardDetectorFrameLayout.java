package com.p1.mobile.p1android.ui.view;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class KeyboardDetectorFrameLayout extends FrameLayout {


    public interface IKeyboardChanged {
        void onKeyboardShown();

        void onKeyboardHidden();
    }

    private IKeyboardChanged keyboardListener;

    public KeyboardDetectorFrameLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    public KeyboardDetectorFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardDetectorFrameLayout(Context context) {
        super(context);
    }

    public void setKeyboardStateChangedListener(IKeyboardChanged listener) {
        keyboardListener = listener;
    }

    private int maxHeight = 0;
    private int currentHeight = 0;


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            maxHeight = 0;
            currentHeight = 0;
        }
        if (h > 10) {
            currentHeight = h;
            if (currentHeight < maxHeight) {
                notifyKeyboardShown();
            } else if (currentHeight == maxHeight) {
                notifyKeyboardHidden();
            }
            if (currentHeight > maxHeight)
                maxHeight = currentHeight;
        }
    }
    
    private void notifyKeyboardHidden() {
        keyboardListener.onKeyboardHidden();
    }

    private void notifyKeyboardShown() {
        keyboardListener.onKeyboardShown();
    }

}