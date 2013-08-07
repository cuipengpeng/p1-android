package com.p1.mobile.p1android.ui.widget;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.helpers.MessagesCounterUpdater;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;

/**
 * 
 * @author Anton
 * 
 */
public class NavigationBar extends RelativeLayout implements OnClickListener {
    public static final String TAG = NavigationBar.class.getSimpleName();

    private View selectedNavigationButton;

    public NavigationBar(Context context) {
        super(context);
        init(context);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NavigationBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.navigation_bar, this);

        findViewById(R.id.navigation_browse).setOnClickListener(this);
        findViewById(R.id.navigation_camera).setOnClickListener(this);
        findViewById(R.id.navigation_chat).setOnClickListener(this);
        findViewById(R.id.navigation_feed).setOnClickListener(this);
        findViewById(R.id.navigation_followers).setOnClickListener(this);
        ((RelativeLayout) findViewById(R.id.messagesHolder))
                .addView(new CounterBubble(context,
                        new MessagesCounterUpdater()));
        selectedNavigationButton = findViewById(R.id.navigation_feed);
        selectedNavigationButton.setSelected(true);
    }

    public void showNavigationBar(final boolean show, boolean anim) {
        Animation slideInOut = AnimationUtils
                .loadAnimation(getContext(), show ? R.anim.slide_in_from_bottom
                        : R.anim.slide_out_to_bottom);
        if (!anim)
            slideInOut.setDuration(0);
        NavigationBar.this.setVisibility(View.VISIBLE);
        slideInOut.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                NavigationBar.this.setVisibility(show ? View.VISIBLE
                        : View.GONE);
            }
        });
        this.startAnimation(slideInOut);
    }

    private ArrayList<Integer> mBackStack = new ArrayList<Integer>();

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Navigation click! " + v.getId());
        if (v.getId() == R.id.navigation_camera) {
            ((NavigationListener) getContext()).navigateToCamera();
            return;
        }
        if (v != selectedNavigationButton) {
            mBackStack.remove(Integer.valueOf(v.getId()));
            mBackStack.add(selectedNavigationButton.getId());
            goTo(v);
        }

    }

    private void goTo(View v) {
        selectedNavigationButton.setSelected(false);
        v.setSelected(true);
        selectedNavigationButton = v;
        switch (v.getId()) {
        case R.id.navigation_browse:
            ((NavigationListener) getContext()).navigateToBrowse();
            break;
        case R.id.navigation_chat:
            ((NavigationListener) getContext()).navigateToChat();
            break;
        case R.id.navigation_feed:
            ((NavigationListener) getContext()).navigateToFeed();
            break;
        case R.id.navigation_followers:
            ((NavigationListener) getContext()).navigateToFollowers();
            break;
        }
    }

    public boolean onBackPressed() {
        if (mBackStack.size() == 0)
            return false;
        goTo(findViewById(mBackStack.get(mBackStack.size() - 1)));
        mBackStack.remove(mBackStack.size() - 1);
        return true;
    }

}
