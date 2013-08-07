package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.LikerFragment;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivityBase;

public class LikersWrapperActivity extends FlurryFragmentActivity implements
        ContextualBackListener, SlidingActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liker_activity);
        Bundle bundle = getIntent().getExtras();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(
                R.id.liker_activity_content,
                LikerFragment.newInstance(
                        bundle.getString(LikerFragment.SHARE_ID),
                        bundle.getString(LikerFragment.CONTENT_TYPE)));
        ft.commit();
    }

    @Override
    public void onContextualBack() {
        this.finish();
    }

    @Override
    public void setBehindContentView(View view, LayoutParams layoutParams) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBehindContentView(View view) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBehindContentView(int layoutResID) {
        // TODO Auto-generated method stub

    }

    @Override
    public SlidingMenu getSlidingMenu() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void toggle() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showContent() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showMenu() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showSecondaryMenu() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSlidingActionBarEnabled(boolean slidingActionBarEnabled) {
        // TODO Auto-generated method stub

    }

}
