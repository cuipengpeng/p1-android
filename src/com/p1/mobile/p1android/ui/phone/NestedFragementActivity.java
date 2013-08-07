package com.p1.mobile.p1android.ui.phone;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Temp fix for the bug in android.support.v4.app.FragmentManager:
 * 
 * https://code.google.com/p/android/issues/detail?id=40323
 * 
 * When using nested fragments, set tag as MAIN_FRAG, to enable nested child
 * fragment back stack in this main fragment works properly
 * 
 * @author pirriperdos
 * 
 */
public abstract class NestedFragementActivity extends FlurryFragmentActivity {

    protected static final String MAIN_FRAG = "main_frag!";

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment mainFrag = fm.findFragmentByTag(MAIN_FRAG);
        if (mainFrag != null && mainFrag.isVisible()) {
            if (mainFrag.getChildFragmentManager().getBackStackEntryCount() > 1) {
                if (!mainFrag.getChildFragmentManager().popBackStackImmediate())
                    finish();
                return;
            }
        }
        super.onBackPressed();
    }

}
