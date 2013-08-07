package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.BrowseMessagesFragment;
import com.p1.mobile.p1android.ui.fragment.MenuFragment;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivityBase;
import com.slidingmenu.lib.app.SlidingActivityHelper;

public class MessagesWrapperActivity extends FlurryFragmentActivity implements
        ContextualBackListener, SlidingActivityBase {

    private String mConversationId;
    private MenuFragment mNotificationFragment;
    private SlidingActivityHelper mHelper;
    private boolean isFromProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_activity);
        Bundle bundle = getIntent().getExtras();
        mConversationId = bundle
                .getString(BrowseMessagesFragment.CONVERSATION_ID);
        if (mConversationId == null)
            mConversationId = "";
        isFromProfile = bundle.getBoolean(BrowseMessagesFragment.ISFROMPROFILE,
                false);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.messages_activity_content, BrowseMessagesFragment
                .newInstance(mConversationId, isFromProfile));
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
