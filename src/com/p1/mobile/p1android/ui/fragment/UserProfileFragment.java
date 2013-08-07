package com.p1.mobile.p1android.ui.fragment;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.p1.mobile.p1android.content.IContentRequester.IhasTimers;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Member;
import com.p1.mobile.p1android.content.Member.MemberIOSession;
import com.p1.mobile.p1android.content.Profile;
import com.p1.mobile.p1android.content.Profile.ProfileIOSession;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.UserPicturesList;
import com.p1.mobile.p1android.content.logic.ReadFollow;
import com.p1.mobile.p1android.content.logic.ReadMember;
import com.p1.mobile.p1android.content.logic.ReadProfile;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.content.logic.ReadUserPictures;
import com.p1.mobile.p1android.content.logic.WriteFollow;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.adapters.UserPictureGridAdapter;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;
import com.p1.mobile.p1android.ui.phone.EditProfileWrapperActivity;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1Button;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment implements IContentRequester,
        OnActionListener, OnClickListener {
    private static final String TAG = UserProfileFragment.class.getSimpleName();
    private String mUserId;
    public static final String USER_ID_KEY = "userId";

    private ImageView mCoverImage;
    private View mListHeader;
    private ListView mContentList;
    private P1TextView mName;
    private TextView mCareer;
    private ImageView mGender;
    private TextView mCity;
    private TextView mLastUpdate;
    private Timer timer = new Timer();
    private ImageView mThumbImage;
    private P1ActionBar mActionBar;
    private TextView mActionBarTitle;
    private UserPictureGridAdapter mPictureAdapter;
    private UserPicturesList mUserPicturesList;
    private P1TextView mDescriptionTextView;
    private RelativeLayout mDescriptionRelativeLayout;

    private P1Button mEditP1Button;

    private MemberRequester mMemberRequester = new MemberRequester();;
    private boolean mIsLoggedInUserId = false;
    private ContextualBackListener mBackListener;
    private ProfileRequester mProfileRequester;
    private Profile mLoggedInProfile;
    private View mFollowAndMessage;
    private View mFollow;
    private View mMessage;

    public static Fragment newInstance(String userId) {
        UserProfileFragment fragment = new UserProfileFragment();
        if (userId != null) {
            Bundle args = new Bundle();
            args.putString(USER_ID_KEY, userId);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getString(USER_ID_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        String loggedInUserId = NetworkUtilities.getLoggedInUserId();
        if (loggedInUserId.equals(mUserId)) {
            mIsLoggedInUserId = true;
        }

        View view = inflater.inflate(R.layout.user_profile_fragment, container,
                false);
        mContentList = (ListView) view
                .findViewById(R.id.user_profile_content_list);

        mFollowAndMessage = view.findViewById(R.id.ll_user_profile_bottom);
        mFollow = mFollowAndMessage
                .findViewById(R.id.ll_user_profile_bottom_following);
        mFollow.setOnClickListener(this);
        mMessage = mFollowAndMessage
                .findViewById(R.id.ll_user_profile_bottom_message);
        mMessage.setOnClickListener(this);
        initActionBar(inflater, view);
        initUserProfileContent(inflater, view);

        return view;
    }

    private void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView
                .findViewById(R.id.user_profile_action_bar);
        mActionBarTitle = new P1TextView(getActivity());
        mActionBarTitle.setTextAppearance(getActivity(),
                R.style.P1LargerTextLight);
        mActionBarTitle.setGravity(Gravity.CENTER);
        mActionBar.setCenterView(mActionBarTitle);

        if (mIsLoggedInUserId) {
            mActionBar.setLeftAction(new ListenerAction(
                    R.drawable.btn_contextual_close, this));
        } else {
            mActionBar.setLeftAction(new ListenerAction(
                    R.drawable.back_arrow_button, this));
        }
    }

    private void initUserProfileContent(LayoutInflater inflater, View view) {
        mListHeader = View.inflate(getActivity(),
                R.layout.user_profile_fragment_header, null);
        mListHeader.setClickable(true);
        mListHeader.setOnClickListener(this);
        mEditP1Button = (P1Button) mListHeader
                .findViewById(R.id.btn_user_profile_edit);
        mEditP1Button.setOnClickListener(this);
        mDescriptionTextView = (P1TextView) mListHeader
                .findViewById(R.id.tv_user_profile_desc);
        mDescriptionRelativeLayout = (RelativeLayout) mListHeader
                .findViewById(R.id.rl_user_profile_desc);
        mContentList.addHeaderView(mListHeader);
        mContentList.setHeaderDividersEnabled(false);
        View listFooter = View.inflate(getActivity(),
                R.layout.empty_list_header, null);
        if (!mIsLoggedInUserId) {
            mContentList.addFooterView(listFooter);
        }
        mContentList.setFooterDividersEnabled(false);

        mCoverImage = (ImageView) mListHeader
                .findViewById(R.id.user_profile_cover_image);
        mCoverImage.setOnClickListener(this);
        mName = (P1TextView) mListHeader.findViewById(R.id.user_profile_name);
        mCareer = (TextView) mListHeader.findViewById(R.id.user_profile_career);
        mGender = (ImageView) mListHeader
                .findViewById(R.id.user_profile_gender);
        mThumbImage = (ImageView) mListHeader
                .findViewById(R.id.user_profile_thumb_image);
        mCity = (TextView) mListHeader.findViewById(R.id.user_profile_city);
        mLastUpdate = (TextView) mListHeader
                .findViewById(R.id.user_profile_last_update);
        if (mIsLoggedInUserId) {
            // mEditP1Button.setVisibility(View.VISIBLE);
            mFollowAndMessage.setVisibility(View.GONE);
            mContentList.setFooterDividersEnabled(false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserPicturesList = ReadUserPictures.requestUserPicturesList(mUserId,
                mUserPicturesRequester);
        mFollowingListRqeuester.contentChanged(ReadFollow
                .requestLoggedInFollowingList(mFollowingListRqeuester));
        contentChanged(ReadUser.requestUser(mUserId, this));

        mMemberRequester.contentChanged(ReadMember.requestMember(mUserId,
                mMemberRequester));

        mPictureAdapter = new UserPictureGridAdapter(mUserPicturesList,
                getActivity());
        mUserPicturesRequester.contentChanged(mUserPicturesList);
        mContentList.setAdapter(mPictureAdapter);

        if (mIsLoggedInUserId) {
            mProfileRequester = new ProfileRequester();
            mLoggedInProfile = ReadProfile
                    .requestLoggedInProfile(mProfileRequester);
            mProfileRequester.contentChanged(mLoggedInProfile);
        }

    }

    @Override
    public void onDestroyView() {
        mPictureAdapter.destroy();
        ContentHandler.getInstance().removeRequester(this);
        ContentHandler.getInstance().removeRequester(mUserPicturesRequester);
        ContentHandler.getInstance().removeRequester(mMemberRequester);
        ContentHandler.getInstance().removeRequester(mFollowingListRqeuester);
        ContentHandler.getInstance().removeRequester(mProfileRequester);
        super.onDestroyView();
    }

    class ProfileRequester implements IContentRequester {

        @Override
        public void contentChanged(Content content) {
            if (content != null) {
                Profile profile = (Profile) content;
                ProfileIOSession io = profile.getIOSession();
                try {

                    mDescriptionTextView.setText(io.getDescription());
                    if (!StringUtils.isEmpty(io.getDescription())
                            && (!getResources().getString(
                                    R.string.edit_profile_desc_yourself)
                                    .equals(io.getDescription()))) {
                        mDescriptionRelativeLayout.setVisibility(View.VISIBLE);
                    } else {
                        mDescriptionRelativeLayout.setVisibility(View.GONE);
                    }
                } finally {
                    io.close();
                }
            }
        }
    }

    private IContentRequester mUserPicturesRequester = new IContentRequester() {
        @Override
        public void contentChanged(Content content) {
            if (mPictureAdapter != null) {
                mPictureAdapter.notifyDataSetChanged();
            }
        }

    };

    private IContentRequester mFollowingListRqeuester = new IContentRequester() {

        @Override
        public void contentChanged(Content content) {
            FollowList followList = (FollowList) content;
            FollowListIOSession io = followList.getIOSession();
            try {
                if (!io.isValid()) {
                    return;
                }
                boolean following = io.isFollowing(mUserId);
                mFollow.findViewById(R.id.image).setSelected(following);
                ((TextView) mFollow.findViewById(R.id.text))
                        .setText(getActivity()
                                .getString(
                                        following ? R.string.user_profile_bottom_following
                                                : R.string.user_profile_bottom_follow));

            } finally {
                io.close();
            }
        }

    };

    @Override
    public void contentChanged(Content content) {
        Log.d(TAG, "contentChagnedCalled!!");
        if (content != null) {

            User user = (User) content;
            UserIOSession io = user.getIOSession();

            try {
                if (!io.isValid()) {
                    return; // Wait for a return with valid information
                }

                mActionBarTitle.setText(io.getPreferredFullName());
                mName.setText(io.getPreferredFullName());

                String positionStr = io.getCareerPosition();
                String companyStr = io.getCareerCompany();
                String link = " at ";
                if (StringUtils.isEmpty(positionStr)) {
                    positionStr = "";
                    link = "";
                }
                if (StringUtils.isEmpty(companyStr)) {
                    companyStr = "";
                    link = "";
                }
                mCareer.setText(positionStr + link + companyStr);

                mGender.setImageResource(io.getGender().equals(User.MALE) ? R.drawable.browse_member_male
                        : R.drawable.browse_member_female);
                mCity.setText(io.getCity());

                P1Application.picasso.load(Uri.parse(io.getCoverUrl()))
                        .placeholder(null).into(mCoverImage);
                P1Application.picasso
                        .load(Uri.parse(io.getProfileThumb100Url())).noFade()
                        .placeholder(null).into(mThumbImage);

            } finally {
                io.close();
            }
        }
    }

    class MemberRequester implements IContentRequester, IhasTimers {

        @Override
        public void contentChanged(Content content) {
            if (content != null) {
                Member member = (Member) content;
                MemberIOSession io = member.getIOSession();
                try {
                    // mLastUpdate.setText(io.getFormattedLatestActivity());
                    setTimeStampTask(io.getLatestActivity(), mLastUpdate);
                } finally {
                    io.close();
                }
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

    @Override
    public void onAction() {
        getActivity().onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (v == mListHeader || v == mCoverImage) {
            // Toast.makeText(getActivity(), "userId = " + mUserId,
            // Toast.LENGTH_SHORT).show();

            FragmentTransaction ft = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            ft.replace(R.id.fl_user_profile_activity_content,
                    ProfileDetailsFragment.newInstance(mUserId));
            ft.addToBackStack(null);
            ft.commit();
        }
        switch (v.getId()) {
        case R.id.ll_user_profile_bottom_following:
            WriteFollow.toggleFollow(mUserId);
            break;
        case R.id.ll_user_profile_bottom_message:
            Utils.startConversationActivity(getActivity(), mUserId, true);
            break;
        case R.id.btn_user_profile_edit:
            Intent intent = new Intent(getActivity(),
                    EditProfileWrapperActivity.class);
            getActivity().startActivity(intent);

            break;
        }
    }
}
