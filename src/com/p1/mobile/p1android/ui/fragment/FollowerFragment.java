/**
 * FriendsListFragment.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ListView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Content.ContentIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.FollowList;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.logic.ReadFollow;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.adapters.FollowListAdapter;
import com.p1.mobile.p1android.ui.helpers.NotificationsCounterUpdater;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;
import com.p1.mobile.p1android.ui.phone.UserProfileWrapperActivity;
import com.p1.mobile.p1android.ui.widget.CounterBubble;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.p1.mobile.p1android.ui.widget.UserPictureView;

/**
 * @author
 * 
 */
public class FollowerFragment extends ListFragment implements
        IContentRequester, OnActionListener {
    private static final String TAG = FollowerFragment.class.getSimpleName();
    private static final String LIST_STATE_KEY = "list_state";

    public P1ActionBar mActionBar;

    private ListView mListView;

    private View mProgressBar;
    private View mErrorMessage;

    private FollowListAdapter mAdapter;
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();
    private View mListHeaderView;

    public static FollowerFragment newInstance() {
        FollowerFragment fragment = new FollowerFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.follower_fragment_layout,
                container, false);
        initActionBar(inflater, view);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListHeaderView = inflater.inflate(R.layout.empty_list_header,
                mListView, false);

        mListView.addHeaderView(mListHeaderView);
        mListView.setHeaderDividersEnabled(false);
        mListView.addFooterView(View.inflate(getActivity(),
                R.layout.empty_list_header, null));
        mListView.setFooterDividersEnabled(false);
        // get following
        mProgressBar = view.findViewById(R.id.progressbar);
        mErrorMessage = view.findViewById(R.id.error_message);
        mErrorMessage.setVisibility(View.GONE);

        mActiveIContentRequesters.add(this);

        return view;
    }

    public void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView
                .findViewById(R.id.followlistActionBar);

        P1TextView titleView = new P1TextView(getActivity());
        titleView.setText(getActivity().getResources().getString(
                R.string.friends_navigation_follower));
        titleView.setTextAppearance(getActivity(), R.style.P1LargerTextLight);
        titleView.setGravity(Gravity.CENTER);

        mActionBar.setCenterView(titleView);

        mActionBar.setLeftAction(new ListenerAction(
                R.drawable.back_arrow_button, this));

        // TODO set profile picture and notifications counter
        if (getActivity() instanceof NavigationListener) {
            UserPictureView picView = (UserPictureView) inflater.inflate(
                    R.layout.user_picture_view, null);
            picView.setAction(new P1ActionBar.ShowNotificationsAction(
                    R.drawable.ic_about_white,
                    ((NavigationListener) getActivity())));
            picView.setNotificationsView(new CounterBubble(getActivity(),
                    new NotificationsCounterUpdater()));
            mActionBar.setRightView(picView);
        } else {
            Log.e(TAG,
                    "Activity of BrowseFragment is not a NavigationListener. BrowseFragment is probably placed in a bad activity");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FollowList followerList = ReadFollow.requestFollowersList(
                NetworkUtilities.getLoggedInUserId(), this);
        mAdapter = new FollowListAdapter(getActivity(), followerList, true);
        mListView.setAdapter(mAdapter);

        contentChanged(followerList);
    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();

        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();
        // No longer request updates to prevent memory leak.
        super.onDestroyView();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position,
            long id) {

        String userId = (String) listView.getAdapter().getItem(position);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(UserProfileFragment.USER_ID_KEY, userId);
        intent.putExtras(bundle);
        intent.setClass(getActivity(), UserProfileWrapperActivity.class);
        startActivity(intent);
    }

    @Override
    public void contentChanged(Content content) {
        Log.d(TAG, "contentChanged");

        ContentIOSession io = content.getIOSession();
        try {
            if (io.isValid()) {
                mAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
            }
        } finally {
            io.close();
        }

    }

    @Override
    public void onAction() {
        getActivity().onBackPressed();
    }
}
