package com.p1.mobile.p1android.ui.helpers;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class FadeAnimationHelper {

    private long mFadeInDuration;
    private long mFadeOutDuration;

    public FadeAnimationHelper(long fadeDuration) {
        mFadeInDuration = fadeDuration;
        mFadeOutDuration = fadeDuration;
    }

    public FadeAnimationHelper(long fadeInDuration, long fadeOutDuration) {
        mFadeInDuration = fadeInDuration;
        mFadeOutDuration = fadeOutDuration;
    }

    public void fadeOutView(View view, AnimationListener animationListener) {

        Animation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnimation.setDuration(mFadeOutDuration);
        if (animationListener != null) {
            fadeOutAnimation.setAnimationListener(animationListener);
        }
        view.startAnimation(fadeOutAnimation);

    }

    public void fadeInView(View view, AnimationListener animationListener) {
        Animation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnimation.setDuration(mFadeInDuration);
        if (animationListener != null) {
            fadeInAnimation.setAnimationListener(animationListener);
        }
        view.startAnimation(fadeInAnimation);
    }
}
