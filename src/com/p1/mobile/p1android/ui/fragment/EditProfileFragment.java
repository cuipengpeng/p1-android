package com.p1.mobile.p1android.ui.fragment;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Profile;
import com.p1.mobile.p1android.content.Profile.BloodType;
import com.p1.mobile.p1android.content.Profile.MaritalStatus;
import com.p1.mobile.p1android.content.Profile.ProfileIOSession;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadProfile;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1Button;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.squareup.picasso.Picasso;

/**
 * 
 * @author Cui pengpeng
 * 
 */

public class EditProfileFragment extends Fragment implements IContentRequester,
        OnClickListener {
    private static final String TAG = UserProfileFragment.class.getSimpleName();
    private static final String CHANGED_CONTENT_KEY = "content";
    public static final String ELLIPSIS = "...";
    public static final String SEPARATOR = "#";
    public static final int EDIT_DESCRIPTION = 11;
    public static final int EDIT_GIVEN_NAME = 12;
    public static final int EDIT_FAMILY_NAME = 13;
    public static final int EDIT_RELATIONSHIP = 14;
    public static final int EDIT_BLOODTYPE = 15;
    public static final int EDIT_SCHOOL = 16;
    public static final int EDIT_POSITION = 17;
    public static final int EDIT_COMPANY = 18;
    public static final int EDIT_CITY = 19;

    private RelativeLayout mDescriptionRelativeLayout;
    private RelativeLayout mGivenNameRelativeLayout;
    private RelativeLayout mFamilyNameRelativeLayout;
    private RelativeLayout mRelativeshipRelativeLayout;
    private RelativeLayout mBloodTypeRelativeLayout;
    private RelativeLayout mSchoolRelativeLayout;
    private RelativeLayout mPositionRelativeLayout;
    private RelativeLayout mCompanyRelativeLayout;
    private RelativeLayout mLocationRelativeLayout;

    private P1TextView mDescriptionTextView;
    private P1TextView mGivenNameTextView;
    private P1TextView mFamilyNameTextView;
    private P1TextView mRelationshipTextView;
    private P1TextView mBloodTypeTextView;
    private P1TextView mSchoolTextView;
    private P1TextView mPositionTextView;
    private P1TextView mCompanyTextView;
    private P1TextView mLocationTextView;

    private P1TextView mPositionAtCompanyTextView;
    private P1TextView mGivenNameKeyTextView;
    private P1TextView mFamilyNameKeyTextView;
    private P1TextView mRelationshipKeyTextView;
    private P1TextView mBloodTypeKeyTextView;
    private P1TextView mSchoolKeyTextView;
    private P1TextView mPositionKeyTextView;
    private P1TextView mCompanyKeyTextView;
    private P1TextView mLocationKeyTextView;

    private P1TextView mUserName;
    private ImageView mCoverImage;
    private ImageView mThumbImage;
    private P1ActionBar mActionBar;
    private P1TextView mActionBarTitle;

    private String mLoggedInUserId;
    private User mLoggedInUser;
    private Profile mLoggedInProfile;

    private BloodType mBloodType;
    private MaritalStatus mMaritalStatus;
    private ProfileRequester mProfileRequester;

    public static Fragment newInstance() {
        EditProfileFragment fragment = new EditProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLoggedInUserId = NetworkUtilities.getLoggedInUserId();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.edit_profile_fragment, container,
                false);

        initActionBar(inflater, view);
        mPositionAtCompanyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_career);
        mDescriptionRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_desc);
        mDescriptionRelativeLayout.setOnClickListener(this);
        mGivenNameRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_given_name);
        mGivenNameRelativeLayout.setOnClickListener(this);
        mFamilyNameRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_family_name);
        mFamilyNameRelativeLayout.setOnClickListener(this);
        mRelativeshipRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_relationship);
        mRelativeshipRelativeLayout.setOnClickListener(this);
        mBloodTypeRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_blood_type);
        mBloodTypeRelativeLayout.setOnClickListener(this);
        mSchoolRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_school);
        mSchoolRelativeLayout.setOnClickListener(this);
        mPositionRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_position);
        mPositionRelativeLayout.setOnClickListener(this);
        mCompanyRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_company);
        mCompanyRelativeLayout.setOnClickListener(this);
        mLocationRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_edit_profile_location);
        mLocationRelativeLayout.setOnClickListener(this);

        mDescriptionTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_desc);
        mGivenNameTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_given_name);
        mFamilyNameTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_family_name);
        mRelationshipTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_relationship);
        mBloodTypeTextView = (P1TextView) view
                .findViewById(R.id.tv_eidt_profile_blood_type);
        mSchoolTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_school);
        mPositionTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_position);
        mCompanyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_company);
        mLocationTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_location);

        mGivenNameKeyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_given_name_key);
        mFamilyNameKeyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_family_name_key);
        mRelationshipKeyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_relationship_key);
        mBloodTypeKeyTextView = (P1TextView) view
                .findViewById(R.id.tv_eidt_profile_blood_type_key);
        mSchoolKeyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_school_key);
        mPositionKeyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_position_key);
        mCompanyKeyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_company_key);
        mLocationKeyTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_location_key);

        mCoverImage = (ImageView) view
                .findViewById(R.id.iv_edit_profile_cover_image);
        mUserName = (P1TextView) view
                .findViewById(R.id.tv_edit_profile_username);
        mThumbImage = (ImageView) view
                .findViewById(R.id.iv_edit_profile_thumb_image);

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

        P1Button picView = (P1Button) inflater.inflate(
                R.layout.small_blue_button, null);
        picView.setText(getResources().getString(R.string.done));
        picView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mActionBar.setRightView(picView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoggedInUser = ReadUser.requestUser(mLoggedInUserId, this);
        contentChanged(mLoggedInUser);

        mProfileRequester = new ProfileRequester();
        mLoggedInProfile = ReadProfile
                .requestLoggedInProfile(mProfileRequester);
        mProfileRequester.contentChanged(mLoggedInProfile);
    }

    @Override
    public void onDestroyView() {
        ContentHandler.getInstance().removeRequester(this);
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

                    if (!StringUtils.isEmpty(io.getDescription())) {
                        mDescriptionTextView.setText(io.getDescription());
                        if (!getResources().getString(
                                R.string.edit_profile_desc_yourself).equals(
                                io.getDescription())) {
                            mDescriptionTextView.setTextColor(Color.BLACK);
                        } else {
                            mDescriptionTextView.setTextColor(getResources()
                                    .getColor(R.color.dark));
                        }
                    }

                    if (io.getMarital() != null) {
                        if (io.getMarital().equals(
                                MaritalStatus.IN_RELATIONSHIP)) {
                            mRelationshipTextView
                                    .setText(getResources()
                                            .getString(
                                                    R.string.relationship_ship_in_relationship));
                        } else if (io.getMarital().equals(
                                MaritalStatus.COMPLICATED)) {
                            mRelationshipTextView
                                    .setText(getResources()
                                            .getString(
                                                    R.string.relationship_ship_complicated));
                        } else if (io.getMarital().equals(MaritalStatus.OTHER)) {
                            mRelationshipTextView
                                    .setText(getResources().getString(
                                            R.string.relationship_ship_private));
                        } else if (io.getMarital().equals(MaritalStatus.SINGLE)) {
                            mRelationshipTextView
                                    .setText(getResources().getString(
                                            R.string.relationship_ship_single));
                        } else if (io.getMarital().equals(MaritalStatus.MARRIED)) {
                            mRelationshipTextView
                                    .setText(getResources().getString(
                                            R.string.relationship_ship_married));
                        } else if (io.getMarital().equals(MaritalStatus.ENGAGED)) {
                            mRelationshipTextView
                                    .setText(getResources().getString(
                                            R.string.relationship_ship_engaged));
                        }
                    }

                    if (io.getBloodtype() != null) {
                        if (io.getBloodtype().equals(BloodType.UNKNOWN)) {
                            mBloodTypeTextView
                                    .setText(getResources().getString(
                                            R.string.profile_detail_unknown));
                        } else {
                            mBloodTypeTextView.setText(io.getBloodtype() + "");
                        }
                    }

                } finally {
                    io.close();
                }
            }
        }
    }

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

                mActionBarTitle.setText(getResources().getString(
                        R.string.edit_profile_title));
                P1Application.picasso.load(Uri.parse(io.getCoverUrl()))
                        .placeholder(null).into(mCoverImage);
                P1Application.picasso
                        .load(Uri.parse(io.getProfileThumb100Url())).noFade()
                        .placeholder(null).into(mThumbImage);
                String fullName = io.getPreferredFullName();
                String givenName = io.getEnUsGivenName();
                String familyName = io.getEnUsSurname();
                mUserName.setText(fullName);

                mGivenNameTextView.setText(givenName);
                mFamilyNameTextView.setText(familyName);

                if (!StringUtils.isEmpty(io.getEducation())) {
                    mSchoolTextView.setText(io.getEducation());
                } else {
                    mSchoolTextView.setText(ELLIPSIS);
                }

                String positionStr = io.getCareerPosition();
                String companyStr = io.getCareerCompany();
                if (!StringUtils.isEmpty(positionStr)) {
                    mPositionTextView.setText(io.getCareerPosition());
                } else {
                    mPositionTextView.setText(ELLIPSIS);
                }

                if (!StringUtils.isEmpty(companyStr)) {
                    mCompanyTextView.setText(io.getCareerCompany());
                } else {
                    mCompanyTextView.setText(ELLIPSIS);
                }

                String link = " at ";
                if (StringUtils.isEmpty(positionStr)) {
                    positionStr = "";
                    link = "";
                }
                if (StringUtils.isEmpty(companyStr)) {
                    companyStr = "";
                    link = "";
                }
                mPositionAtCompanyTextView.setText(positionStr + link
                        + companyStr);

                if (!StringUtils.isEmpty(io.getCity())) {
                    mLocationTextView.setText(io.getCity());
                } else {
                    mLocationTextView.setText(ELLIPSIS);
                }
            } finally {
                io.close();
            }
        }

    }

    @Override
    public void onClick(View v) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                .beginTransaction();
        String value;
        switch (v.getId()) {
        case R.id.rl_edit_profile_desc:
            value = mDescriptionTextView.getText().toString();
            showEditUserInfoFragment(EditProfileFragment.EDIT_DESCRIPTION,
                    value);
            break;
        case R.id.rl_edit_profile_given_name:
            value = mGivenNameTextView.getText().toString();
            showEditUserInfoFragment(EditProfileFragment.EDIT_GIVEN_NAME, value);
            break;
        case R.id.rl_edit_profile_family_name:
            value = mFamilyNameTextView.getText().toString();
            showEditUserInfoFragment(EditProfileFragment.EDIT_FAMILY_NAME,
                    value);
            break;
        case R.id.rl_edit_profile_relationship:
            // Toast.makeText(getActivity(), "relationship", Toast.LENGTH_SHORT)
            // .show();

            value = mRelationshipTextView.getText().toString();
            ft.replace(R.id.fl_edit_profile_activity_content,
                    RelationshipStatusFragment.newInstance(value));
            ft.addToBackStack(null);
            ft.commit();
            break;
        case R.id.rl_edit_profile_blood_type:
            // Toast.makeText(getActivity(), "blood_type", Toast.LENGTH_SHORT)
            // .show();

            value = mBloodTypeTextView.getText().toString();
            ft.replace(R.id.fl_edit_profile_activity_content,
                    BloodTypeFragment.newInstance(value));
            ft.addToBackStack(null);
            ft.commit();
            break;
        case R.id.rl_edit_profile_school:
            value = mSchoolTextView.getText().toString();
            showEditUserInfoFragment(EditProfileFragment.EDIT_SCHOOL, value);
            break;
        case R.id.rl_edit_profile_position:
            value = mPositionTextView.getText().toString();
            showEditUserInfoFragment(EditProfileFragment.EDIT_POSITION, value);
            break;
        case R.id.rl_edit_profile_company:
            value = mCompanyTextView.getText().toString();
            showEditUserInfoFragment(EditProfileFragment.EDIT_COMPANY, value);
            break;
        case R.id.rl_edit_profile_location:
            value = mLocationTextView.getText().toString();
            showEditUserInfoFragment(EditProfileFragment.EDIT_CITY, value);
            break;
        }
    }

    private void showEditUserInfoFragment(int editCode, String text) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.fl_edit_profile_activity_content,
                EditUserInfoFragment.newInstance(editCode, text));
        ft.addToBackStack(null);
        ft.commit();
    }

}
