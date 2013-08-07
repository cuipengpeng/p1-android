package com.p1.mobile.p1android.ui.phone;

import android.support.v4.app.FragmentActivity;

import com.flurry.android.FlurryAgent;
import com.p1.mobile.p1android.BuildConfig;
import com.p1.mobile.p1android.P1Application;

public abstract class FlurryFragmentActivity extends FragmentActivity {
    @Override
    protected void onStart() {
        super.onStart();
        if (!BuildConfig.DEBUG) {
            FlurryAgent.onStartSession(this, P1Application.FLURRY_API_KEY);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!BuildConfig.DEBUG) {
            FlurryAgent.onEndSession(this);
        }
    }

}
