package com.p1.mobile.p1android.ui.view;

import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;

public class ViewUtils {

    public static class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float paramFloat) {
            return Math.abs(paramFloat - 1f);
        }
    }

    public static class AutoHideListener implements OnGestureListener,
            OnTouchListener, AnimationListener {

        boolean isScrolling = false;
        AnimationSet hideAnim = new AnimationSet(true);

        View toHide;

        boolean hidden = false;

        boolean isAnim = false;

        GestureDetector gd;
        
        public static final int HL = 1;

        public void syncAndShow(boolean show) {
            hidden = toHide != null && !toHide.isEnabled();
            shouldHide = show ? -HL : HL;
            hide();
        }

        public AutoHideListener(View list, View toHide, int distence) {
            this.toHide = toHide;
            list.setOnTouchListener(this);
            gd = new GestureDetector(this);
            Animation trans = new TranslateAnimation(0, 0, 0, distence);
            hideAnim.addAnimation(trans);
            Animation alpha = new AlphaAnimation(1, 0);
            hideAnim.addAnimation(alpha);
            hideAnim.setFillEnabled(true);
            hideAnim.setAnimationListener(this);
            hideAnim.setFillAfter(true);
            hideAnim.setFillBefore(true);
            hideAnim.setDuration(200);
        }

        public static void enableView(View v, boolean enable) {
            v.setEnabled(enable);
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    enableView(vg.getChildAt(i), enable);
                }
            }
        }

        float preY = 0;
        private int shouldHide;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gd.onTouchEvent(event);
            return false;
        }

        private void hide() {
            if (!isAnim) {
                if (!hidden && shouldHide == HL) {
                    hideAnim.setInterpolator(null);
                    toHide.startAnimation(hideAnim);
                    isAnim = true;
                    hidden = true;
                    enableView(toHide, false);
                    Log.d("anim", "starting to Hide!");
                } else if (hidden && shouldHide == -HL) {
                    hideAnim.setInterpolator(new ReverseInterpolator());
                    toHide.startAnimation(hideAnim);
                    Log.d("anim", "starting to show!");
                    enableView(toHide, false);
                    isAnim = true;
                    hidden = false;
                }
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {
            isAnim = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            isAnim = false;
            hide();
            if (!isAnim && !hidden)
                enableView(toHide, true);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY))
                return false;
            if (distanceY > 2)
                shouldHide++;
            else if (distanceY < -2)
                shouldHide--;
            hide();
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            shouldHide = velocityY < 0 ? HL : -HL;
            hide();
            return false;
        }

    };

    public static AutoHideListener autoHide(View toHide, AbsListView list,
            int distence) {
        return new AutoHideListener(list, toHide, distence);
    }
}
