/**
 * GalleryViewerFragment.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.ImageFormat;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.UserPicturesList;
import com.p1.mobile.p1android.content.UserPicturesList.UserPicturesListIOSession;
import com.p1.mobile.p1android.content.logic.ReadUserPictures;
import com.p1.mobile.p1android.content.logic.WriteLike;
import com.p1.mobile.p1android.ui.adapters.PictureViewerAdapter;
import com.p1.mobile.p1android.ui.helpers.FadeAnimationHelper;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;
import com.p1.mobile.p1android.ui.phone.GalleryPicturePagerActivity;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.UserPictureView;
import com.p1.mobile.p1android.util.ScreenUtil;
import com.p1.mobile.p1android.util.Utils;

/**
 * @author Viktor Nyblom
 * 
 */
public class PictureViewPagerFragment extends Fragment implements
        ViewPager.OnPageChangeListener, IContentRequester, OnClickListener {
    public static final String TAG = PictureViewPagerFragment.class
            .getSimpleName();

    public static final int PRELOADED_PAGES = 2;
    public static final int ANIMATION_DURAION_MILLIS = 500;
    public static final int PAGE_MARGIN_DP = 20;

    private int mSelectedPosition;
    private String mUserId;
    private boolean mLiked = false;
    private PictureViewerAdapter mAdapter;
    private ViewPager mViewPager;
    private ToggleButton mLikeButton;
    private ImageButton mCommentButton;
    private UserPictureView mUserView;
    private ContextualBackListener mBackListener;
    private String mOriginPicId;
    private UserPicturesList mPicturesList;
    private boolean mIsDownloading;
    private Picture mSelectedPicture;
    private String mSelectedPictureId;
    private FadeAnimationHelper mAnimationHelper;
    private List<View> mViewsToAnimateList = new ArrayList<View>();

    private ImageButton mCloseButton;

    public static PictureViewPagerFragment newInstance(String pictureId,
            String userId) {
        Bundle args = new Bundle();
        args.putString("pictureId", pictureId);
        args.putString("userId", userId);
        PictureViewPagerFragment fragment = new PictureViewPagerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ContextualBackListener == false) {
            throw new ClassCastException("Activities using " + TAG
                    + " must implement the interface ContextualBackListener");
        }
        mBackListener = (ContextualBackListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mOriginPicId = args.getString("pictureId");
        mUserId = args.getString("userId");
        mSelectedPictureId = mOriginPicId;
        mAnimationHelper = new FadeAnimationHelper(ANIMATION_DURAION_MILLIS);
        Log.d(TAG, "Picture id " + mOriginPicId);
        Log.d(TAG, "User id " + mUserId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View parent = (View) inflater.inflate(
                R.layout.picture_view_pager_layout, container, false);

        final RelativeLayout userPictureView = (RelativeLayout) parent
                .findViewById(R.id.picturePagerUserLayout);
        mUserView = (UserPictureView) inflater.inflate(
                R.layout.user_picture_view, null);
        userPictureView.addView(mUserView);

        mCommentButton = (ImageButton) parent
                .findViewById(R.id.galleryCommentsButton);
        mCommentButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openComments();
            }
        });

        mLikeButton = (ToggleButton) parent
                .findViewById(R.id.galleryLikeButton);
        mLikeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                requestLikeChange();
            }
        });

        mCloseButton = (ImageButton) parent
                .findViewById(R.id.pictureViewPagerClose);
        mCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onContextualBack();
            }

        });

        mViewPager = (ViewPager) parent.findViewById(R.id.galleryViewPager);
        mViewPager.setOffscreenPageLimit(PRELOADED_PAGES);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setPageMargin(Utils.dpToPx(getActivity(), PAGE_MARGIN_DP));

        mAdapter = new PictureViewerAdapter(getActivity(), mOriginPicId, this,
                getImageFormat());
        mViewPager.setAdapter(mAdapter);
        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUserView();

        mPicturesList = ReadUserPictures.requestUserPicturesList(mUserId, this,
                mOriginPicId);
        UserPicturesListIOSession io = mPicturesList.getIOSession();
        try {
            List<String> l = io.getPictureIdList(mOriginPicId);
            if (l.size() == 0) {
                l = new ArrayList<String>();
                l.add(mOriginPicId);
            }
            mAdapter.setPicturesList(l);
        } finally {
            io.close();
        }

        mAdapter.notifyDataSetChanged();
        setViewsToAnimateList();
    }

    private void setViewsToAnimateList() {
        mViewsToAnimateList.add(mCommentButton);
        mViewsToAnimateList.add(mLikeButton);
        mViewsToAnimateList.add(mUserView);
        mViewsToAnimateList.add(mCloseButton);
    }

    private void setUserView() {
        Log.d(TAG, "setUserView user id" + mUserId);
        mUserView.setUser(mUserId);

        Intent intent = new Intent(Actions.SHOW_PROFILE);
        intent.putExtra(UserProfileFragment.USER_ID_KEY, mUserId);
        P1ActionBar.IntentAction action = new P1ActionBar.IntentAction(0,
                getActivity(), intent);
        mUserView.setAction(action);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("firstGo", false);
        outState.putString("selectedPage", mSelectedPictureId);
    }

    @Override
    public void onDestroyView() {
        ContentHandler.getInstance().removeRequester(this);
        mAdapter.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        setSelectedPicture(mSelectedPictureId);
    }

    private void requestLikeChange() {
        if (mSelectedPicture != null) {
            WriteLike.toggleLike(mSelectedPicture);
        }
    }

    private void openComments() {
        ((GalleryPicturePagerActivity) getActivity())
                .openComment(mSelectedPictureId);
    }

    private void onContextualBack() {
        mBackListener.onContextualBack();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // noop
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // noop
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "On page selected position " + position);

        mSelectedPosition = position;
        mSelectedPicture = mAdapter.getPicture(position, this);
        PictureIOSession io = mSelectedPicture.getIOSession();
        try {
            mSelectedPictureId = io.getId();
            toggleLike(io.hasLiked());
        } finally {
            io.close();
        }
        Log.d(TAG, "Picture Id " + mSelectedPictureId);
        determineDownload(position);
    }

    private void toggleLike(boolean like) {
        mLiked = like;
        mLikeButton.setChecked(mLiked);
    }

    private void determineDownload(int position) {
        int count = mAdapter.getCount();
        boolean download = (!mIsDownloading && (position <= 3 || position >= count - 3));
        Log.d(TAG, "download? " + download);
        if (download) {
            Log.e(TAG, "Download ");
        }
        if (download) {
            mIsDownloading = true;
            if (count / 2 < position) {
                downloadPicturesPositive();
            } else {
                downloadPicturesNegative();
            }
        }

    }

    private void downloadPicturesNegative() {
        Log.d(TAG, "downloadPicturesNegative");
        ReadUserPictures.fillUserPicturesListNegative(mPicturesList,
                mOriginPicId);
    }

    private void downloadPicturesPositive() {
        Log.d(TAG, "downloadPicturesPositive");
        ReadUserPictures.fillUserPicturesListPositive(mPicturesList,
                mOriginPicId);
    }

    @Override
    public void contentChanged(Content content) {
        Log.d(TAG, "Fragment ContentChanged");
        if (content instanceof UserPicturesList) {
            Log.d(TAG, "UserPictuerList content changed");
            mPicturesList = (UserPicturesList) content;
            UserPicturesListIOSession io = mPicturesList.getIOSession();
            try {
                List<String> list = io.getPictureIdList(mOriginPicId);
                if (list.size() == 0)
                    return;
                if (list.size() != mAdapter.getCount()) {
                    int currentPosition = mViewPager.getCurrentItem();
                    int selectedPosition = PagerAdapter.POSITION_NONE;
                    String selectedID = mAdapter.getItem(currentPosition);
                    selectedPosition = list.indexOf(selectedID);
                    mAdapter.setPicturesList(list);
                    mAdapter.notifyDataSetChanged();
                    mViewPager.setCurrentItem(selectedPosition, false);
                }
            } finally {
                io.close();
                mIsDownloading = false;
            }
        } else if (content instanceof Picture) {
            Log.d(TAG, "Picture content changed");
            PictureIOSession io = ((Picture) content).getIOSession();
            try {
                int position = mAdapter.getPicturePosition(io.getId());
                if (position == mSelectedPosition) {
                    toggleLike(io.hasLiked());
                }
            } finally {
                io.close();
            }
        }
    }

    private void setSelectedPicture(String pictureId) {
        UserPicturesListIOSession io = mPicturesList.getIOSession();
        try {
            List<String> list = io.getPictureIdList(mOriginPicId);
            int selectionPosition = list.indexOf(pictureId);
            mViewPager.setCurrentItem(selectionPosition);
        } finally {
            io.close();
        }
    }

    private ImageFormat getImageFormat() {
        int screenWidth = ScreenUtil.getScreenWidth(getActivity());

        if (screenWidth >= 720) {
            return ImageFormat.IMAGE_WIDTH_720;
        } else {
            return ImageFormat.IMAGE_WIDTH_480;
        }

    }

    private class FadeInAnimationListener implements AnimationListener {
        private View view;

        public FadeInAnimationListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
            view.setVisibility(View.VISIBLE);
        }

    }

    private class FadeOutAnimationListener implements AnimationListener {
        private View view;

        public FadeOutAnimationListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            view.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    @Override
    public void onClick(View v) {
        for (View fadeView : mViewsToAnimateList) {
            if (fadeView.isShown()) {
                mAnimationHelper.fadeOutView(fadeView,
                        new FadeOutAnimationListener(fadeView));
            } else {
                mAnimationHelper.fadeInView(fadeView,
                        new FadeInAnimationListener(fadeView));
            }
        }
    }

}
