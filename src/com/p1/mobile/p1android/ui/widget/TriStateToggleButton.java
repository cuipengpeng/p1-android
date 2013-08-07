package com.p1.mobile.p1android.ui.widget;

import com.p1.mobile.p1android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Toggle Button holding three states instead of two. On click changing state
 * from 0 to 1 to 2 to 0.
 * 
 * @author Viktor Nyblom
 * 
 */
public class TriStateToggleButton extends ImageButton {
    static final String TAG = TriStateToggleButton.class.getSimpleName();

    public static final int STATE_ONE = 0;
    public static final int STATE_TWO = 1;
    public static final int STATE_THREE = 2;
    // States 0, 1 or 2
    private int mState;

    private static final int[] STATE_ONE_SET = { R.attr.tri_state_one };
    private static final int[] STATE_TWO_SET = { R.attr.tri_state_two };
    private static final int[] STATE_THREE_SET = { R.attr.tri_state_three };

    public TriStateToggleButton(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        setState(0);
    }

    public TriStateToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setState(0);
    }

    public TriStateToggleButton(Context context) {
        super(context, null);
        setState(0);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        // Add the number of states you have
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);
        if (mState == 0) {
            mergeDrawableStates(drawableState, STATE_ONE_SET);
        } else if (mState == 1) {
            mergeDrawableStates(drawableState, STATE_TWO_SET);
        } else if (mState == 2) {
            mergeDrawableStates(drawableState, STATE_THREE_SET);
        }

        return drawableState;
    }

    @Override
    public boolean performClick() {
        nextState();
        return super.performClick();
    }

    public void nextState() {
        // Loop if at last state
        if (mState < 2) {
            ++mState;
        } else {
            mState = 0;
        }
    }

    public int getState() {
        return mState;
    }

    /**
     * Sets the new state if 0, 1 or 2 and returns the new state otherwise sets
     * the state to 0.
     * 
     * @param newState
     * @return
     */
    public int setState(int newState) {
        if (newState > 2 || newState < 0) {
            mState = 0;
        } else {
            mState = newState;
        }
        return mState;
    }

}
