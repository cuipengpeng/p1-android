/**
 * MenuFragment.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadAccount;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.content.logic.WriteNotification;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.adapters.NotificationsAdapter;
import com.p1.mobile.p1android.ui.phone.SettingActivity;
import com.p1.mobile.p1android.ui.phone.UserProfileWrapperActivity;
import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.squareup.picasso.Picasso;

/**
 * @author Viktor Nyblom
 * 
 */
public class MenuFragment extends Fragment implements IContentRequester,
        OnClosedListener, OnOpenedListener {
    public static final String TAG = MenuFragment.class.getSimpleName();

    private static final boolean RETAIN_INSTANCE = true;
    private ListView mListView;
    private ImageView mCoverPicture;
    private ImageView mProfilePicture;
    private TextView mUsername;
    private FrameLayout mMenuFragmentFrameLayout;
    private NotificationsAdapter mNotificationAdapter;

    private IContentRequester mAccountRequester = new IContentRequester() {

        @Override
        public void contentChanged(Content content) {
            AccountIOSession io = (AccountIOSession) content.getIOSession();
            try {
                mVisibility.setVisibility(io.isInvisible() ? View.VISIBLE
                        : View.INVISIBLE);
            } finally {
                io.close();
            }
        }
    };

    private View mVisibility;

    public static MenuFragment newInstance() {
        MenuFragment menuFragment = new MenuFragment();
        return menuFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(RETAIN_INSTANCE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMenuFragmentFrameLayout = (FrameLayout) inflater.inflate(
                R.layout.slidingmenu_layout, null);
        mListView = (ListView) mMenuFragmentFrameLayout
                .findViewById(R.id.slidinglist);

        View profileHeader = inflater.inflate(R.layout.user_profile_header,
                null);
        View notiHeader = inflater.inflate(R.layout.notifications_header, null);
        mListView.addHeaderView(profileHeader);
        mListView.addHeaderView(notiHeader);
        profileHeader.findViewById(R.id.side_panel_profile_summary)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startLoggedInUserProfile();
                    }
                });
        profileHeader.findViewById(R.id.userCoverPicture).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startLoggedInUserProfile();
                    }
                });

        OnClickListener openSettings = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                getActivity().startActivity(intent);
            }
        };
        profileHeader.findViewById(R.id.btn_sliding_settings)
                .setOnClickListener(openSettings);
        mVisibility = profileHeader
                .findViewById(R.id.slideout_profile_visibility);
        mVisibility.setOnClickListener(openSettings);
        ReadAccount.requestAccount(mAccountRequester);
        mUsername = (TextView) profileHeader.findViewById(R.id.userProfileName);
        mProfilePicture = (ImageView) profileHeader
                .findViewById(R.id.userProfilePicture);
        mCoverPicture = (ImageView) profileHeader
                .findViewById(R.id.userCoverPicture);
        if (mNotificationAdapter == null)
            mNotificationAdapter = new NotificationsAdapter();
        mListView.setAdapter(mNotificationAdapter);

        return mMenuFragmentFrameLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        contentChanged(ReadUser.requestUser("me", this));
        mNotificationAdapter.requestUpdates(true);
    }

    @Override
    public void onDestroyView() {
        ContentHandler.getInstance().removeRequester(this);
        ContentHandler.getInstance().removeRequester(mAccountRequester);
        mNotificationAdapter.requestUpdates(false);
        super.onDestroyView();
    }

    private void startLoggedInUserProfile() {
        String loggedInUserId = NetworkUtilities.getLoggedInUserId();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(UserProfileFragment.USER_ID_KEY, loggedInUserId);
        intent.putExtras(bundle);
        intent.setClass(getActivity(), UserProfileWrapperActivity.class);
        startActivity(intent);
    }

    @Override
    public void contentChanged(Content content) {
        UserIOSession ioSession = (UserIOSession) content.getIOSession();
        try {
            mUsername
                    .setText(ioSession.getPreferredFullName() != null ? ioSession
                            .getPreferredFullName() : "");
            if (ioSession.getProfileThumb30Url() != null)
                P1Application.picasso
                        .load(Uri.parse(ioSession.getProfileThumb100Url()))
                        .noFade().placeholder(null).into(mProfilePicture);
            if (ioSession.getCoverUrl() != null)
                P1Application.picasso.load(Uri.parse(ioSession.getCoverUrl()))
                        .noFade().placeholder(null).into(mCoverPicture);
        } finally {
            ioSession.close();
        }

    }

    @Override
    public void onOpened() {
        WriteNotification.markAllAsRead();
    }

    @Override
    public void onClosed() {
        WriteNotification.markAllAsRead();
    }
}
