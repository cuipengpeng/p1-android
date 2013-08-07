/**
 * FriendsListFragment.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.FollowList;
import com.p1.mobile.p1android.content.FollowList.FollowListIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadFollow;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.adapters.FollowListAdapter;
import com.p1.mobile.p1android.ui.helpers.NotificationsCounterUpdater;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;
import com.p1.mobile.p1android.ui.phone.UserProfileWrapperActivity;
import com.p1.mobile.p1android.ui.view.KeyboardDetectorFrameLayout;
import com.p1.mobile.p1android.ui.view.KeyboardDetectorFrameLayout.IKeyboardChanged;
import com.p1.mobile.p1android.ui.widget.CounterBubble;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.p1.mobile.p1android.ui.widget.UserPictureView;

/**
 * @author
 * 
 */
public class FollowingFragment extends ListFragment implements
        IContentRequester, OnClickListener, OnFocusChangeListener,
        IKeyboardChanged {
    private static final String TAG = FollowingFragment.class.getSimpleName();
    private static final String LIST_STATE_KEY = "list_state";

    private FollowListAdapter mFollowingAdapter;
    private ListView mListView;
    private View mProgressBar;
    private View mErrorMessage;

    private P1ActionBar mActionBar;
    private ImageView mProfileImageView;
    private P1TextView mFollowerTextView;
    private P1TextView mFollowingTextView;

    private FollowListListener mFollowListListener;
    private RelativeLayout mHeaderFollower;

    private EditText mSearchFriendEditText;
    private ImageButton mSearchClearImageButton;
    private TextView mNoSearchResultsTextView;
    private View mActionbarPlaceholder;

    // private Array

    /** Top search for friends, find following/followers action bar */
    private LinearLayout mTopActionView;

    private FollowersRequester mFollowersRequester = new FollowersRequester();
    private UserProfilePictureRequester mUserProfilePictureRequester = new UserProfilePictureRequester();
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();

    public static FollowingFragment newInstance() {
        FollowingFragment fragment = new FollowingFragment();

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

        initTopActionViewContent(inflater);

        View view = inflater.inflate(R.layout.following_fragment_layout,
                container, false);
        KeyboardDetectorFrameLayout keyboardView = (KeyboardDetectorFrameLayout) view;
        keyboardView.setKeyboardStateChangedListener(this);
        initActionBar(inflater, view);

        mProgressBar = view.findViewById(R.id.following_progressbar);
        mErrorMessage = view.findViewById(R.id.following_error_message);
        mErrorMessage.setVisibility(View.GONE);
        mActionbarPlaceholder = view
                .findViewById(R.id.v_following_actionbar_placeholder);

        mActiveIContentRequesters.add(mFollowersRequester);
        mActiveIContentRequesters.add(mUserProfilePictureRequester);
        mActiveIContentRequesters.add(this);

        mSearchFriendEditText = (EditText) view
                .findViewById(R.id.et_following_search_friends);
        mSearchFriendEditText
                .addTextChangedListener(new SearchFriendsTextWatcher());
        mSearchFriendEditText.setOnFocusChangeListener(this);

        mSearchClearImageButton = (ImageButton) view
                .findViewById(R.id.ib_following_search_friends_clear);
        mSearchClearImageButton.setOnClickListener(this);

        mNoSearchResultsTextView = (TextView) view
                .findViewById(R.id.tv_following_no_search_results);

        mListView = (ListView) view.findViewById(android.R.id.list);

        mListView.addHeaderView(mTopActionView);
        mListView.setHeaderDividersEnabled(false);

        mListView.addFooterView(View.inflate(getActivity(),
                R.layout.empty_list_header, null));
        mListView.setFooterDividersEnabled(false);
        return view;
    }

    public void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView
                .findViewById(R.id.followlistActionBar);

        P1TextView titleView = new P1TextView(getActivity());
        titleView.setText(getActivity().getResources().getString(
                R.string.friends_navigation_title));
        titleView.setTextAppearance(getActivity(), R.style.P1LargerTextLight);
        titleView.setGravity(Gravity.CENTER);

        mActionBar.setCenterView(titleView);

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

    public void initTopActionViewContent(LayoutInflater inflater) {
        mTopActionView = (LinearLayout) inflater.inflate(
                R.layout.following_top_action_view, null);
        mHeaderFollower = (RelativeLayout) mTopActionView
                .findViewById(R.id.rl_following_list_header);
        mHeaderFollower.setOnClickListener(this);
        mFollowerTextView = (P1TextView) mTopActionView
                .findViewById(R.id.follow_list_user_follower_tv);
        mFollowerTextView.setText(R.string.friends_follower_loading_desc);

        mFollowingTextView = (P1TextView) mTopActionView
                .findViewById(R.id.tv_following_count_show);
        mFollowingTextView.setText(R.string.friends_follower_loading_desc);

        // get follower list
        FollowList followerList = ReadFollow.requestFollowersList(
                NetworkUtilities.getLoggedInUserId(), mFollowersRequester);
        setFollowerTextView(followerList);

        mProfileImageView = (ImageView) mTopActionView
                .findViewById(R.id.follow_list_user_follower_iv);

        User user = ReadUser.requestUser(NetworkUtilities.getLoggedInUserId(),
                mUserProfilePictureRequester);
        if (user != null) {
            setFollowerImageView(user);
        }
    }

    /** Set current user profile image */
    public void setFollowerImageView(User user) {

        UserIOSession io = user.getIOSession();
        try {
            if (io.isValid()) {
                P1Application.picasso
                        .load(Uri.parse(io.getProfileThumb50Url()))
                        .placeholder(null).noFade().into(mProfileImageView);
            }
        } finally {
            io.close();
        }
    }

    /** Set the how many followers text */
    public void setFollowerTextView(FollowList followList) {
        FollowListIOSession followListIO = (followList).getIOSession();
        try {
            mFollowerTextView.setText(getResources().getString(
                    R.string.following_header_you_have)
                    + followListIO.getPaginationTotal()
                    + getResources().getString(
                            R.string.following_header_followers));
        } finally {
            followListIO.close();
        }
    }

    /** Set the how many followings text */
    public void setFollowingTextView(FollowList followList) {
        FollowListIOSession followListIO = (followList).getIOSession();

        try {
            if (followListIO.isValid()) {
                mFollowingTextView.setText(getResources().getString(
                        R.string.following_header_you_are_following)
                        + followListIO.getPaginationTotal()
                        + getResources().getString(
                                R.string.following_header_members));
            }
        } finally {
            followListIO.close();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get following
        FollowList list = ReadFollow.requestFollowingList(
                NetworkUtilities.getLoggedInUserId(), this); // Content
        setFollowingTextView(list);
        // request
        mFollowingAdapter = new FollowListAdapter(getActivity(), list, false);

        setListAdapter(mFollowingAdapter);

        contentChanged(list);
    }

    @Override
    public void onDestroyView() {
        mFollowingAdapter.destroy();

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

        if (view == mTopActionView) {

        } else {
            String userId = (String) listView.getAdapter().getItem(position);
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(UserProfileFragment.USER_ID_KEY, userId);
            intent.putExtras(bundle);
            intent.setClass(getActivity(), UserProfileWrapperActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void contentChanged(Content content) {
        Log.d(TAG, "contentChanged");

        FollowListIOSession io = (FollowListIOSession) content.getIOSession();
        try {
            if (io.isValid() && !io.hasMore()) {
                mFollowingAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
                setFollowingTextView((FollowList) content);
            }
        } finally {
            io.close();
        }

    }

    public class FollowersRequester implements IContentRequester {

        @Override
        public void contentChanged(Content content) {
            setFollowerTextView((FollowList) content);
        }
    }

    public class UserProfilePictureRequester implements IContentRequester {

        @Override
        public void contentChanged(Content content) {
            setFollowerImageView((User) content);
        }
    }

    public void setFollowListListener(FollowListListener followListListener) {
        this.mFollowListListener = followListListener;
    }

    class SearchFriendsTextWatcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            if (!mSearchFriendEditText.hasFocus())
                return;
            if (s.length() > 0) {
                mSearchClearImageButton.setVisibility(View.VISIBLE);

            } else {
                mSearchClearImageButton.setVisibility(View.GONE);
            }
            int searchResultsCount = mFollowingAdapter.search(s.toString()
                    .trim());
            if (searchResultsCount > 0
                    || searchResultsCount == FollowListAdapter.NO_CHANGED) {
                if (searchResultsCount > 0) {
                    if (mTopActionView.getHeight() > 0) {
                        mTopActionView.setPadding(0,
                                -1 * mTopActionView.getHeight(), 0, 0);
                    }
                    mTopActionView.setVisibility(View.GONE);
                    mActionbarPlaceholder.setVisibility(View.VISIBLE);
                } else {
                    mTopActionView.setPadding(0, 0, 0, 0);
                    mTopActionView.setVisibility(View.VISIBLE);
                    mActionbarPlaceholder.setVisibility(View.GONE);
                }
                mListView.setVisibility(View.VISIBLE);
                mListView.setAdapter(mFollowingAdapter);
                mNoSearchResultsTextView.setVisibility(View.GONE);
            } else {
                mNoSearchResultsTextView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                mActionbarPlaceholder.setVisibility(View.GONE);

            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.ib_following_search_friends_clear:
            mSearchFriendEditText.setText("");
            mListView.setVisibility(View.VISIBLE);
            mNoSearchResultsTextView.setVisibility(View.GONE);
            break;
        case R.id.rl_following_list_header:
            mFollowListListener.switchToFollower();
            break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        }
    }

    @Override
    public void onKeyboardShown() {
        if (getActivity() instanceof NavigationListener) {
            ((NavigationListener) getActivity())
                    .showNavigationBar(false, false);
        }
    }

    @Override
    public void onKeyboardHidden() {
        if (getActivity() instanceof NavigationListener) {
            ((NavigationListener) getActivity()).showNavigationBar(true);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            retryFetchingInformation();
        }
    }

    public void retryFetchingInformation() {
        ReadFollow.requestLoggedInFollowingList(null);
    }

}
