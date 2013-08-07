package com.p1.mobile.p1android.ui.fragment;

import org.jraf.android.backport.switchwidget.Switch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadAccount;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.content.logic.WriteAccount;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;
import com.p1.mobile.p1android.ui.phone.CustomGalleryActivity;
import com.p1.mobile.p1android.ui.phone.EditProfileWrapperActivity;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.squareup.picasso.Picasso;

/**
 * 
 * @author Cui pengpeng
 * 
 */

public class SettingFragment extends Fragment implements IContentRequester,
            OnClickListener, OnCheckedChangeListener {
    private static final String TAG = SettingFragment.class.getSimpleName();
    private static final String EMAIL_KEY = "userId";
    public static final String CHANGE_PROFILE_PICTURE = "changeProfilePicture";
    public static final String CHANGE_COVER_PICTURE = "changeCoverPicture";
    public static final String CHANGE_KEY = "changeKey";
    public static final int EDIT_EMAIL = 10;


    private ContextualBackListener mBackListener;
    private String mUserId;

    private P1ActionBar mActionBar;
    private P1TextView mActionBarTitle;

    private ImageView mCoverImage;
    private ImageView mThumbImage;
    private P1TextView mUserName;
    private P1TextView mEmailTextView;
    private Switch mToggleSwitch;
    private boolean mIsToggleChangedByUser;

    private RelativeLayout mChangeProfilePictureRelativeLayout;
    private RelativeLayout mChangeCoverPictureRelativeLayout;
    private RelativeLayout mEditPersonalInformationRelativeLayout;
    private RelativeLayout mEmailRelativeLayout;
    private RelativeLayout mChangePasswordRelativeLayout;

    private Account mLoggedInAccount;
    private User mLoggedInUser;
    private AccountRequester mAccountRequester;

    private Button mSignOutButton;
    private String mEmailStr = "xxx@gmail.com";
    private boolean mIsUpdateEmail = false;

    public void setIsUpdateEmail(boolean isUpdateEmail) {
        this.mIsUpdateEmail = isUpdateEmail;
    }

    public static Fragment newInstance(String args) {
        SettingFragment fragment = new SettingFragment();
        if (args != null) {
            Bundle bundle = new Bundle();
            bundle.putString(EMAIL_KEY, args);
            fragment.setArguments(bundle);
        }

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
        if (getArguments() != null) {
            mEmailStr = getArguments().getString(EMAIL_KEY).split(
                    EditProfileFragment.SEPARATOR)[1];
        }
        mUserId = NetworkUtilities.getLoggedInUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.setting_fragment, container,
                false);

        initActionBar(inflater, view);

        mCoverImage = (ImageView) view
                .findViewById(R.id.iv_setting_cover_image);
        mUserName = (P1TextView) view.findViewById(R.id.tv_setting_username);
        mThumbImage = (ImageView) view
                .findViewById(R.id.iv_setting_thumb_image);
        mEmailTextView = (P1TextView) view.findViewById(R.id.tv_setting_email);
        mToggleSwitch = (Switch) view.findViewById(R.id.switchWidget);
        mToggleSwitch.setTextOff(getResources().getString(
                R.string.setting_switch_off));
        mToggleSwitch.setTextOn(getResources().getString(
                R.string.setting_switch_on));
        mToggleSwitch.setOnClickListener(this);
        mToggleSwitch.setOnCheckedChangeListener(this);

        mChangeProfilePictureRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_setting_change_profile_picture);
        mChangeProfilePictureRelativeLayout.setOnClickListener(this);
        mChangeCoverPictureRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_setting_change_cover_picture);
        mChangeCoverPictureRelativeLayout.setOnClickListener(this);
        mEditPersonalInformationRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_setting_edit_personal_information);
        mEditPersonalInformationRelativeLayout.setOnClickListener(this);
        mEmailRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_setting_email);
        mEmailRelativeLayout.setOnClickListener(this);
        mChangePasswordRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_setting_change_passwd);
        mChangePasswordRelativeLayout.setOnClickListener(this);

        mSignOutButton = (Button) view.findViewById(R.id.btn_setting_sign_out);
        mSignOutButton.setOnClickListener(this);

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

        mActionBar.setLeftAction(new ListenerAction(
                R.drawable.back_arrow_button, new BackListener()));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLoggedInUser = ReadUser.requestUser(mUserId, this);
        contentChanged(mLoggedInUser);

        mAccountRequester = new AccountRequester();
        mLoggedInAccount = ReadAccount.requestAccount(mAccountRequester);
        mAccountRequester.contentChanged(mLoggedInAccount);

        if (mIsUpdateEmail) {
            WriteAccount.changeEmail(mLoggedInAccount, mEmailStr);
        }
        mIsUpdateEmail = false;
    }

    @Override
    public void onDestroyView() {
        WriteAccount.changeInvisibility(mLoggedInAccount,mToggleSwitch.isChecked());        
        ContentHandler.getInstance().removeRequester(this);
        ContentHandler.getInstance().removeRequester(mAccountRequester);
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                .beginTransaction();

        switch (v.getId()) {
        case R.id.switchWidget:
            mIsToggleChangedByUser = true;
            break;
        case R.id.rl_setting_change_profile_picture:
            startCameraActivity(CHANGE_PROFILE_PICTURE);
            break;
        case R.id.rl_setting_change_cover_picture:
            startCameraActivity(CHANGE_COVER_PICTURE);
            break;
        case R.id.rl_setting_edit_personal_information:
            // Intent intent = new Intent(getActivity(),
            // EditProfileWrapperActivity.class);
            // getActivity().startActivity(intent);
            break;
        case R.id.rl_setting_email:
            Fragment fragment = EditUserInfoFragment.newInstance(EDIT_EMAIL, mEmailTextView.getText().toString().trim());
            ft.replace(R.id.fl_setting_activity_content, fragment);
            ft.addToBackStack(null);
            ft.commit();
            break;
        case R.id.rl_setting_change_passwd:
            ft.replace(R.id.fl_setting_activity_content,
                    new ChangePasswordFragment());
            ft.addToBackStack(null);
            ft.commit();
            break;
        case R.id.btn_setting_sign_out:
            ((P1Application) getActivity().getApplication()).logout();
            getActivity().finish();
            break;
        }
    }

    private void startCameraActivity(String change) {
        Intent intent = new Intent(getActivity(), CustomGalleryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CHANGE_KEY, change);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    class BackListener implements OnActionListener {

        @Override
        public void onAction() {
            WriteAccount.changeInvisibility(mLoggedInAccount,mToggleSwitch.isChecked());            
            mBackListener.onContextualBack();
        }
        
    }



    private class AccountRequester implements IContentRequester {

        @Override
        public void contentChanged(Content content) {
            if (content != null && content instanceof Account) {
                Account account = (Account) content;
                AccountIOSession io = account.getIOSession();
                try {
                    if (!io.isValid()) {
                        return; // Wait for a return with valid information
                    }
                    if (!mIsToggleChangedByUser) {
                        if (io.isInvisible()) {
                            mToggleSwitch.setChecked(true);
                        } else {
                            mToggleSwitch.setChecked(false);
                        }
                    }
                    // get email address
                    mEmailTextView.setText(io.getEmail());
                } finally {
                    io.close();
                }
            }
        }

    }

    @Override
    public void contentChanged(Content content) {
        if (content != null) {
            User user = (User) content;
            UserIOSession io = user.getIOSession();
            try {
                if (!io.isValid()) {
                    return; // Wait for a return with valid information
                }

                mActionBarTitle.setText(getResources().getString(
                        R.string.setting_title));

                P1Application.picasso.load(Uri.parse(io.getCoverUrl()))
                        .placeholder(null).into(mCoverImage);
                P1Application.picasso
                        .load(Uri.parse(io.getProfileThumb100Url())).noFade()
                        .placeholder(null).into(mThumbImage);
                mUserName.setText(io.getPreferredFullName());
            } finally {
                io.close();
            }

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mIsToggleChangedByUser = true;
    }
}
