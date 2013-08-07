/**
 * FriendsListFragment.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.p1.mobile.p1android.R;

/**
 * @author
 *
 */
public class FollowListFragment extends Fragment implements FollowListListener{
    private static final String TAG = FollowListFragment.class.getSimpleName();
    private static final String FOLLOWING_TAG = "followingFragmentTag";
    private static final String FOLLOWER_TAG = "followerFragmentTag";

    private FollowerFragment mFollowerFragment;
    private FollowingFragment mFollowingFragment;

    public static FollowListFragment newInstance() {
        FollowListFragment fragment = new FollowListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.follow_list_fragment_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFollowingFragment = (FollowingFragment) FollowingFragment.newInstance();
        mFollowingFragment.setFollowListListener(this);
        mFollowerFragment = (FollowerFragment) FollowerFragment.newInstance();
        switchToFollowing();
    }

    @Override
    public void switchToFollower() {
        Log.d(TAG, "switchToFollowing ");

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        if (mFollowerFragment == null) {
            Log.d(TAG, "PICTURES_TAG gives null fragment");
            mFollowerFragment = (FollowerFragment) FollowerFragment.newInstance();
        }

        ft.replace(R.id.followlistContainer, mFollowerFragment, FOLLOWER_TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void switchToFollowing() {
        Log.d(TAG, "switchToFollowing ");

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        if (mFollowingFragment == null) {
            Log.d(TAG, "PICTURES_TAG gives null fragment");
            mFollowingFragment = (FollowingFragment) FollowingFragment.newInstance();
        }

//        ft.replace(R.id.followlistContainer, mFollowingFragment, FOLLOWING_TAG);
        ft.replace(R.id.followlistContainer, mFollowingFragment, FOLLOWING_TAG);
        ft.commit();
    }
}
