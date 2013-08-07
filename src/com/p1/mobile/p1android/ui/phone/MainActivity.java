package com.p1.mobile.p1android.ui.phone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.BrowseConversationsFragment;
import com.p1.mobile.p1android.ui.fragment.BrowseFeedFragment;
import com.p1.mobile.p1android.ui.fragment.BrowseFragment;
import com.p1.mobile.p1android.ui.fragment.FollowListFragment;
import com.p1.mobile.p1android.ui.fragment.MenuFragment;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;
import com.p1.mobile.p1android.ui.widget.NavigationBar;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivityBase;
import com.slidingmenu.lib.app.SlidingActivityHelper;

/**
 * 
 * @author Anton
 * 
 *         The Activity containing the bottom tabbar and handling the switching
 *         of the four main features and notification menu
 */
public class MainActivity extends FlurryFragmentActivity implements
        SlidingActivityBase, NavigationListener {

    // protected Fragment mActiveFragment;
    private MenuFragment mNotificationFragment;
    private SlidingActivityHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SlidingActivityHelper(this);
        mHelper.onCreate(savedInstanceState);

        setBehindContentView(R.layout.menu_frame);

        if (checkLogin())
            return;
        navigateToFeed();
        FragmentTransaction ft = this.getSupportFragmentManager()
                .beginTransaction();

        mNotificationFragment = MenuFragment.newInstance();
        ft.replace(R.id.menu_frame, mNotificationFragment);

        ft.commit();

        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.slide_shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setAboveOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.3f);
        sm.setOnOpenedListener(mNotificationFragment);
        sm.setOnClosedListener(mNotificationFragment);

        sm.setMode(SlidingMenu.RIGHT);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        setContentView(R.layout.main_activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLogin();
    }

    private boolean checkLogin() {
        if (!((P1Application) getApplication()).isLoggedIn()) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationFragment = null;
        mHelper = null;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate(savedInstanceState);
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v != null)
            return v;
        return mHelper.findViewById(id);
    }

    @Override
    public void showNavigationBar(boolean show, boolean anim) {
        NavigationBar bar = (NavigationBar) findViewById(R.id.navigationBar);
        if (bar != null)
            bar.showNavigationBar(show, anim);

    }

    @Override
    public void showNavigationBar(boolean show) {
        NavigationBar bar = (NavigationBar) findViewById(R.id.navigationBar);
        if (bar != null)
            bar.showNavigationBar(show, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#setContentView(int)
     */
    @Override
    public void setContentView(int id) {
        setContentView(getLayoutInflater().inflate(id, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#setContentView(android.view.View)
     */
    @Override
    public void setContentView(View v) {
        setContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#setContentView(android.view.View,
     * android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void setContentView(View v, LayoutParams params) {
        super.setContentView(v, params);
        mHelper.registerAboveContentView(v, params);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.slidingmenu.lib.app.SlidingActivityBase#setBehindContentView(int)
     */
    public void setBehindContentView(int id) {
        setBehindContentView(getLayoutInflater().inflate(id, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.slidingmenu.lib.app.SlidingActivityBase#setBehindContentView(android
     * .view.View)
     */
    public void setBehindContentView(View v) {
        setBehindContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.slidingmenu.lib.app.SlidingActivityBase#setBehindContentView(android
     * .view.View, android.view.ViewGroup.LayoutParams)
     */
    public void setBehindContentView(View v, LayoutParams params) {
        mHelper.setBehindContentView(v, params);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.slidingmenu.lib.app.SlidingActivityBase#getSlidingMenu()
     */
    public SlidingMenu getSlidingMenu() {
        return mHelper.getSlidingMenu();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.slidingmenu.lib.app.SlidingActivityBase#toggle()
     */
    public void toggle() {
        mHelper.toggle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.slidingmenu.lib.app.SlidingActivityBase#showAbove()
     */
    public void showContent() {
        mHelper.showContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.slidingmenu.lib.app.SlidingActivityBase#showBehind()
     */
    public void showMenu() {
        mHelper.showMenu();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.slidingmenu.lib.app.SlidingActivityBase#showSecondaryMenu()
     */
    public void showSecondaryMenu() {
        mHelper.showSecondaryMenu();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.slidingmenu.lib.app.SlidingActivityBase#setSlidingActionBarEnabled
     * (boolean)
     */
    public void setSlidingActionBarEnabled(boolean b) {
        mHelper.setSlidingActionBarEnabled(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean b = mHelper.onKeyUp(keyCode, event);
        if (b)
            return b;
        return super.onKeyUp(keyCode, event);
    }

    private static String[] FRAGMENT_TAGS = new String[] {
            BrowseFeedFragment.class.getSimpleName(),
            BrowseFragment.class.getSimpleName(),
            FollowListFragment.class.getSimpleName(),
            BrowseConversationsFragment.class.getSimpleName() };

    private static class FragmentIndex {
        public static final int FEED = 0;
        public static final int BROWSE = 1;
        public static final int FOLLOWING = 2;
        public static final int MESSAGE = 3;
    }

    private int mFragmentIndex = 0;

    @Override
    public void navigateToFeed() {
        mFragmentIndex = FragmentIndex.FEED;
        FragmentManager manager = getSupportFragmentManager();
        Fragment f = manager.findFragmentByTag(FRAGMENT_TAGS[mFragmentIndex]);
        if (f == null) {
            f = BrowseFeedFragment.newInstance();
        }
        pushFragment(f, false);
    }

    @Override
    public void navigateToBrowse() {
        mFragmentIndex = FragmentIndex.BROWSE;
        FragmentManager manager = getSupportFragmentManager();
        Fragment f = manager.findFragmentByTag(FRAGMENT_TAGS[mFragmentIndex]);
        if (f == null) {
            f = new BrowseFragment();
        }
        pushFragment(f, false);
    }

    @Override
    public void navigateToCamera() {
        startCameraActivity();
    }

    @Override
    public void navigateToFollowers() {
        mFragmentIndex = FragmentIndex.FOLLOWING;
        FragmentManager manager = getSupportFragmentManager();
        Fragment f = manager.findFragmentByTag(FRAGMENT_TAGS[mFragmentIndex]);
        if (f == null) {
            f = new FollowListFragment();
        }
        pushFragment(f, false);
    }

    @Override
    public void navigateToChat() {
        mFragmentIndex = FragmentIndex.MESSAGE;
        FragmentManager manager = getSupportFragmentManager();
        Fragment f = manager.findFragmentByTag(FRAGMENT_TAGS[mFragmentIndex]);
        if (f == null) {
            f = new BrowseConversationsFragment();
        }
        pushFragment(f, false);
    }

    private void pushFragment(Fragment fragment, boolean withAnimation) {
        if (withAnimation) {
            takeSnapshot();
        }
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        for (String tag : FRAGMENT_TAGS) {
            Fragment f = manager.findFragmentByTag(tag);
            if (f != null) {
                ft.hide(f);
            }
        }
        if (!fragment.isAdded())
            ft.add(R.id.replacableFrame, fragment, fragment.getClass()
                    .getSimpleName());
        ft.show(fragment);
        ft.commit();
    }

    @Override
    public void navigateToNotifications() {
        mHelper.showMenu();
    }

    @SuppressWarnings("deprecation")
    private void takeSnapshot() {
        View fragmentStack = findViewById(R.id.replacableFrame);
        fragmentStack.setDrawingCacheEnabled(true);
        try {
            fragmentStack.setBackgroundDrawable(new BitmapDrawable(
                    getResources(), fragmentStack.getDrawingCache().copy(
                            Bitmap.Config.ARGB_8888, true)));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        fragmentStack.setDrawingCacheEnabled(false);
    }

    private void startCameraActivity() {
        Intent intent = new Intent(Actions.CUSTOM_GALLERY);
        startActivity(intent);
    }

    public static void startMainActivity(Activity activity) {
        Intent i = new Intent(Actions.DEFAULT_START);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(i);
        activity.finish();
    }

    /**
     * first check nested fragment back stack then fragment that is not one of
     * the 4 then nav bar then normal back
     */
    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment mainFrag = fm.findFragmentByTag(FRAGMENT_TAGS[mFragmentIndex]);
        if (mainFrag != null && mainFrag.isVisible()) {
            if (mainFrag.getChildFragmentManager().getBackStackEntryCount() > 0) {
                if (!mainFrag.getChildFragmentManager().popBackStackImmediate())
                    finish();
                return;
            }
        }
        if (mainFrag instanceof BrowseFeedFragment
                || mainFrag instanceof BrowseFragment
                || mainFrag instanceof FollowListFragment
                || mainFrag instanceof BrowseConversationsFragment) {
            NavigationBar bar = (NavigationBar) findViewById(R.id.navigationBar);
            if (!bar.onBackPressed())
                super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
