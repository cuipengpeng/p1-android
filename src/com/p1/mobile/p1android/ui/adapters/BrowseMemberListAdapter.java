package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.BrowseList;
import com.p1.mobile.p1android.content.BrowseList.BrowseListIOSession;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IChildContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IhasTimers;
import com.p1.mobile.p1android.content.Member;
import com.p1.mobile.p1android.content.Member.MemberIOSession;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.ImageFormat;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadBrowse;
import com.p1.mobile.p1android.content.logic.ReadMember;
import com.p1.mobile.p1android.content.logic.ReadPicture;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.ui.fragment.UserProfileFragment;
import com.p1.mobile.p1android.ui.phone.UserProfileWrapperActivity;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class BrowseMemberListAdapter extends BaseAdapter {
    public static final String TAG = BrowseMemberListAdapter.class
            .getSimpleName();

    /** Load more data before getting the last one */
    private static final int LOAD_BEFORE_HAND_NUM = 10;
    private Timer timer = new Timer();

    private BrowseList mMemberList;
    private BrowseFilter mFilter;
    private Context mContext;

    private List<String> mMemberIdList = new ArrayList<String>();
    private int mHighestRequest = 0;
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();

    private String mUserId;

    private LayoutInflater mInflater;

    private IContentRequester mRequester;

    public BrowseMemberListAdapter(BrowseList memberList, BrowseFilter mFilter,
            Context mContext,
            IContentRequester requester) {
        super();
        this.mMemberList = memberList;
        this.mFilter = mFilter;
        this.mContext = mContext;
        this.mRequester = requester;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public void notifyDataSetChanged() {
        BrowseListIOSession io = this.mMemberList.getIOSession();
        try {
            mMemberIdList.clear();
            mMemberIdList.addAll(io.getIdList());
        } finally {
            io.close();
        }

        Log.d(TAG, "Dataset changed. Available information size is "
                + mMemberIdList.size() + ", requested is " + mHighestRequest);
        if (mHighestRequest >= mMemberIdList.size()) { // Not enough information
                                                       // is present
            ReadBrowse.requestBrowseMembersList(mRequester, mFilter);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMemberIdList.size() + 10; // Lets you scroll through more items
                                          // than what's loaded. Actual number
                                          // is not very important
        // return Integer.MAX_VALUE;
    }

    @Override
    public Object getItem(int position) {
        if (position > mHighestRequest) {
            mHighestRequest = position;
            Log.d(TAG, "Highest request is " + mHighestRequest);
        }

        /** Get more data before getting the last one */
        if (position >= (mMemberIdList.size() - LOAD_BEFORE_HAND_NUM)) {
            ReadBrowse.requestBrowseMembersList(mRequester, mFilter);
        }

        if (position >= mMemberIdList.size()) {
            return null;
        }
        return mMemberIdList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String memberId = (String) getItem(position);
        Log.d(TAG, TAG + "......getView()....memberId = " + memberId);

        final MemberViewHolder memberHolder;
        ViewGroup userInfoLinearLayout;
        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.browse_member_fragment_list_item, parent, false);

            memberHolder = new MemberViewHolder();
            mActiveIContentRequesters.add(memberHolder);

            userInfoLinearLayout = (ViewGroup) convertView
                    .findViewById(R.id.ll_browse_fragment_list_header);
            memberHolder.setUserInfoLinearLayout(userInfoLinearLayout);

            memberHolder.mPicturesFirstLine = (LinearLayout) convertView
                    .findViewById(R.id.ll_browse_fragment_member_pictures_first_line);
            memberHolder.mPicturesSecondLine = (LinearLayout) convertView
                    .findViewById(R.id.ll_browse_fragment_member_pictures_second_line);

            memberHolder.mUserViewHolder.mProfileImageView = (ImageView) convertView
                    .findViewById(R.id.iv_browse_fragment_list_profile);
            memberHolder.mUserViewHolder.mUserNameTextView = (TextView) convertView
                    .findViewById(R.id.tv_browse_fragment_list_username);
            memberHolder.mUserViewHolder.mPositionTextView = (TextView) convertView
                    .findViewById(R.id.tv_browse_fragment_list_position);
            memberHolder.mUserViewHolder.mCityTextView = (TextView) convertView
                    .findViewById(R.id.tv_browse_fragment_list_city);
            memberHolder.mTimeTextView = (TextView) convertView
                    .findViewById(R.id.tv_browse_fragment_list_time);

            for (int i = 0; i < 4; i++) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                ImageView baseView = (ImageView) View
                        .inflate(
                                mContext,
                                R.layout.browse_member_fragment_member_image_item,
                                null);
                memberHolder.mPictureHolderList.get(i).mImageView = baseView;

                int margin = mContext.getResources().getDimensionPixelSize(
                        R.dimen.browse_member_item_margin);
                if (i == 0) {
                    params.setMargins(margin, margin, margin, margin);
                } else {
                    params.setMargins(0, margin, margin, margin);
                }

                memberHolder.mPicturesFirstLine.addView(baseView, params);
            }

            for (int i = 0; i < 4; i++) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                ImageView baseView = (ImageView) View
                        .inflate(
                                mContext,
                                R.layout.browse_member_fragment_member_image_item,
                                null);
                memberHolder.mPictureHolderList.get(4 + i).mImageView = baseView;

                int margin = mContext.getResources().getDimensionPixelSize(
                        R.dimen.browse_member_item_margin);
                if (i == 0) {
                    params.setMargins(margin, 0, margin, margin);
                } else {
                    params.setMargins(0, 0, margin, margin);
                }

                memberHolder.mPicturesSecondLine.addView(baseView, params);
            }

            convertView.setTag(memberHolder);
        } else {
            userInfoLinearLayout = (ViewGroup) convertView
                    .findViewById(R.id.ll_browse_fragment_list_header);
            memberHolder = (MemberViewHolder) convertView.getTag();
            memberHolder.setUserInfoLinearLayout(userInfoLinearLayout);
            ContentHandler.getInstance().removeRequester(memberHolder);
        }

        if (memberId != null) {
            Member member = ReadMember.requestMember(memberId, memberHolder);
            memberHolder.contentChanged(member);
        } else {
            memberHolder.contentChanged(null);
        }

        // Set MemberList userInfo listener
        userInfoLinearLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (((String) getItem(position)) != null) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString(UserProfileFragment.USER_ID_KEY,
                            (String) getItem(position));
                    intent.putExtras(bundle);
                    intent.setClass(((Activity) mContext).getApplication(),
                            UserProfileWrapperActivity.class);
                    ((Activity) mContext).startActivity(intent);
                }

            }
        });

        return convertView;
    }

    public class MemberViewHolder implements IContentRequester,
            IChildContentRequester, IhasTimers {

        public LinearLayout mPicturesFirstLine;
        public LinearLayout mPicturesSecondLine;
        private ViewGroup userInfoLinearLayout;

        public void setUserInfoLinearLayout(ViewGroup userInfoLinearLayout) {
            this.userInfoLinearLayout = userInfoLinearLayout;
        }

        public TextView mTimeTextView;
        // User requester to set user variable
        public UserViewHolder mUserViewHolder = new UserViewHolder();
        public List<PictureViewHolder> mPictureHolderList = new ArrayList<PictureViewHolder>();

        public MemberViewHolder() {
            for (int i = 0; i < 8; i++) {
                mPictureHolderList.add(new PictureViewHolder());
            }
        }

        @Override
        public void contentChanged(Content content) {
            if (content == null) {
                mUserViewHolder.contentChanged(null);
                mPicturesFirstLine.setVisibility(View.GONE);
                mPicturesSecondLine.setVisibility(View.GONE);

                mTimeTextView.setText("");
                return;
            }

            MemberIOSession io = ((com.p1.mobile.p1android.content.Member) content)
                    .getIOSession();

            try {
                // set user variables
                mUserId = io.getOwnerId();
                Log.d(TAG, TAG
                        + "....MemberIOSession.getOwnerId()......mUserId= "
                        + mUserId);
                User user = ReadUser.requestUser(mUserId, mUserViewHolder);
                mUserViewHolder.contentChanged(user);

                List<String> pictureIds = io.getPictureIds();
                if (pictureIds.size() > 0 && pictureIds.size() <= 8) {
                    userInfoLinearLayout
                            .setBackgroundResource(R.drawable.round_corner_top_small);
                    mPicturesFirstLine.setVisibility(View.VISIBLE);
                    if (pictureIds.size() > 4) {
                        mPicturesSecondLine.setVisibility(View.VISIBLE);
                    } else {
                        mPicturesSecondLine.setVisibility(View.GONE);
                    }

                    for (int i = 0; i < pictureIds.size(); i++) {
                        PictureViewHolder pictureHolder = mPictureHolderList
                                .get(i);

                        final String userId = mUserId;
                        final String picId = pictureIds.get(i);
                        final ImageView imageView = pictureHolder.mImageView;
                        pictureHolder.mImageView
                                .setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        // Toast.makeText(mContext,
                                        // "mUserId = "+userId,
                                        // Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(mContext,
                                        // "picId = "+picId,
                                        // Toast.LENGTH_SHORT).show();
                                        //
                                        startPictureView(userId, picId,
                                                imageView);
                                    }
                                });

                        Picture picture = ReadPicture.requestPicture(
                                pictureIds.get(i), pictureHolder);
                        pictureHolder.contentChanged(picture);
                    }
                } else {
                    userInfoLinearLayout
                            .setBackgroundResource(R.drawable.round_corner_shape_white_small);
                    mPicturesFirstLine.setVisibility(View.GONE);
                    mPicturesSecondLine.setVisibility(View.GONE);
                }

                // // set other pictures to empty
                for (int i = pictureIds.size(); i < 8; i++) {
                    PictureViewHolder pictureHolder = mPictureHolderList.get(i);
                    pictureHolder.contentChanged(null);
                }

                // mTimeTextView.setText(io.getFormattedLatestActivity());
                setTimeStampTask(io.getLatestActivity(), mTimeTextView);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }

        @Override
        public void removeChildRequestors() {
            ContentHandler.getInstance().removeRequester(mUserViewHolder);
            for (PictureViewHolder pHolder : mPictureHolderList) {
                ContentHandler.getInstance().removeRequester(pHolder);
            }
        }

        @Override
        public void removetimer() {
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        }
    }

    private void setTimeStampTask(final Date createdTime,
            final TextView timeTextView) {
        if (createdTime != null) {
            timer.cancel();
            timer = new Timer();
            timeTextView.setText(Utils.getRelativeTime(createdTime,
                    timeTextView.getContext()));
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (timeTextView != null) {
                        timeTextView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                timeTextView.setText(Utils.getRelativeTime(
                                        createdTime, timeTextView.getContext()));
                            }
                        }, 0);
                    }
                }
            }, 0, 1000 * 30); // Every 30sec
        }
    }

    private void startPictureView(String ownerId, String picID, View view) {
        Intent intent = new Intent(Actions.USER_PICTURES);
        intent.putExtra("userId", ownerId);
        intent.putExtra("pictureId", picID);
        mContext.startActivity(intent);
    }

    public class UserViewHolder implements IContentRequester {

        public ImageView mProfileImageView;
        public TextView mUserNameTextView;
        public TextView mPositionTextView;
        public TextView mCityTextView;

        @Override
        public void contentChanged(Content content) {
            if (content == null) {
                mProfileImageView.setImageDrawable(null);
                mUserNameTextView.setText("Unknown");
                mPositionTextView.setText("");
                mCityTextView.setText("");
                return;
            }
            UserIOSession userIO = ((com.p1.mobile.p1android.content.User) content)
                    .getIOSession();

            try {
                P1Application.picasso.load(userIO.getProfileThumb50Url())
                        .placeholder(null).noFade().into(mProfileImageView);
                mUserNameTextView.setText(userIO.getPreferredFullName());
                mPositionTextView.setText(userIO.getCareerPosition() + " "
                        + userIO.getCareerCompany());
                mCityTextView.setText(userIO.getCity());
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                userIO.close();
            }

        }
    }

    public class PictureViewHolder implements IContentRequester {
        public ImageView mImageView;

        @Override
        public void contentChanged(Content content) {
            P1Application.picasso.cancelRequest(mImageView);
            if (content == null) {
                // mImageView.setImageResource(R.drawable.landing_page_slide_1);
                return;
            }

            PictureIOSession pictureIO = ((com.p1.mobile.p1android.content.Picture) content)
                    .getIOSession();
            try {
                P1Application.picasso
                        .load(pictureIO
                                .getImageUrl(ImageFormat.IMAGE_SQUARE_154))
                        .placeholder(null).into(mImageView);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                pictureIO.close();
            }
        }
    }

    public void destroy() {
        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();
    }
}
