package com.p1.mobile.p1android.ui.fragment;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.p1.mobile.p1android.content.Profile.Zodiac;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadProfile;
import com.p1.mobile.p1android.content.logic.ReadUser;
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

public class ProfileDetailsFragment extends Fragment implements
        IContentRequester, OnActionListener {
    private static final String TAG = ProfileDetailsFragment.class
            .getSimpleName();
    private static String USER_ID = "userid";

    private P1TextView mActionBarTitle;
    private P1ActionBar mActionBar;
    private ImageView mCoverImage;
    private ImageView mThumbImage;
    private P1TextView mName;
    private P1TextView mCareerTextView;

    private P1TextView mRelationshipTextView;
    private P1TextView mZodiacTextView;
    private P1TextView mBloodTypeTextView;
    private P1TextView mSchoolTextView;
    private P1TextView mPositionTextView;
    private P1TextView mCompanyTextView;
    private P1TextView mLocationTextView;
    private P1TextView mDescriptionTextView;

    private LinearLayout mDescriptionLinearLayout;
    private LinearLayout mProfessionalLinearLayout;
    private LinearLayout mSchoolLinearLayout;
    private LinearLayout mPositionLinearLayout;
    private LinearLayout mCompanyLinearLayout;
    private RelativeLayout mLocationRelativeLayout;

    private ProfileRequester mProfileRequester;

    private String mUserId;
    private User mUser;
    private Profile mProfile;

    public static Fragment newInstance(String userId) {
        ProfileDetailsFragment fragment = new ProfileDetailsFragment();
        if (userId != null) {
            Bundle args = new Bundle();
            args.putString(USER_ID, userId);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getString(USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.profile_details_fragment,
                container, false);

        mCoverImage = (ImageView) view
                .findViewById(R.id.profile_details_cover_image);
        mThumbImage = (ImageView) view
                .findViewById(R.id.profile_details_thumb_image);
        mName = (P1TextView) view.findViewById(R.id.tv_profile_detail_username);
        mCareerTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_career);

        mRelationshipTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_relationship);
        mZodiacTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_zodiac);
        mBloodTypeTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_blood_type);
        mSchoolTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_school);
        mPositionTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_position);
        mCompanyTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_company);
        mLocationTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_location);

        mDescriptionTextView = (P1TextView) view
                .findViewById(R.id.tv_profile_detail_desc);
        mDescriptionLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_profile_detail_desc);
        mProfessionalLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_profile_detail_professional);
        mSchoolLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_profile_deatil_school);
        mPositionLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_profile_deatil_position);
        mCompanyLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_profile_deatil_company);
        mLocationRelativeLayout = (RelativeLayout) view
                .findViewById(R.id.rl_profile_deatil_location);

        initActionBar(inflater, view);

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
                R.drawable.back_arrow_button, this));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUser = ReadUser.requestUser(mUserId, this);
        contentChanged(mUser);

        mProfileRequester = new ProfileRequester();
        mProfile = ReadProfile.requestProfile(mUserId, mProfileRequester);
        mProfileRequester.contentChanged(mProfile);
    }

    @Override
    public void onDestroyView() {
        ContentHandler.getInstance().removeRequester(this);
        ContentHandler.getInstance().removeRequester(mProfileRequester);
        super.onDestroyView();
    }

    @Override
    public void onAction() {
        FragmentManager fm= getActivity().getSupportFragmentManager();
        FragmentTransaction ft =fm.beginTransaction();
        fm.popBackStack();
        ft.commit();
    }

    class ProfileRequester implements IContentRequester {

        @Override
        public void contentChanged(Content content) {
            if (content != null) {
                Profile profile = (Profile) content;
                ProfileIOSession io = profile.getIOSession();
                try {

                    mDescriptionTextView.setText(io.getDescription());
                    if (StringUtils.isEmpty(io.getDescription())) {
                        mDescriptionLinearLayout.setVisibility(View.GONE);
                    } else {
                        mDescriptionLinearLayout.setVisibility(View.VISIBLE);
                    }

                    if (io.getMarital() != null) {
                        if (io.getMarital().equals(
                                MaritalStatus.IN_RELATIONSHIP)) {
                            mRelationshipTextView
                                    .setText(getResources().getString(R.string.relationship_ship_in_relationship));
                        } else if (io.getMarital().equals(
                                MaritalStatus.COMPLICATED)) {
                            mRelationshipTextView
                                    .setText(getResources().getString(R.string.relationship_ship_complicated));
                        } else if (io.getMarital().equals(MaritalStatus.OTHER)) {
                            mRelationshipTextView
                                    .setText(getResources().getString(R.string.relationship_ship_private));
                        } else {
                            mRelationshipTextView
                                    .setText(makeFirstLetterUpper(io
                                            .getMarital() + ""));
                        }
                    }
                    if (io.getBloodtype() != null) {
                        if (io.getBloodtype().equals(BloodType.UNKNOWN)) {
                            mBloodTypeTextView
                                    .setText(getResources().getString(
                                            R.string.blood_type_unknown));
                        } else {
                            mBloodTypeTextView.setText(io.getBloodtype() + "");
                        }
                    }
                    //set zodiac
                    if (io.getZodiac() != null) {
                        if(io.getZodiac().equals(Zodiac.AQUARIUS)) {
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_aquarius));
                        }else if(io.getZodiac().equals(Zodiac.ARIES)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_aries));
                        }else if(io.getZodiac().equals(Zodiac.CANCER)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_cancer));
                        }else if(io.getZodiac().equals(Zodiac.CAPRICORNUS)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_capricornus));
                        }else if(io.getZodiac().equals(Zodiac.GEMINI)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_gemini));
                        }else if(io.getZodiac().equals(Zodiac.LEO)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_leo));
                        }else if(io.getZodiac().equals(Zodiac.LIBRA)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_libra));
                        }else if(io.getZodiac().equals(Zodiac.PISCES)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_pisces));
                        }else if(io.getZodiac().equals(Zodiac.SAGITTARUS)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_sagittarus));
                        }else if(io.getZodiac().equals(Zodiac.SCORPIO)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_scorpio));
                        }else if(io.getZodiac().equals(Zodiac.TAURUS)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_taurus));
                        }else if(io.getZodiac().equals(Zodiac.VIRGO)){
                            mZodiacTextView.setText(getResources().getString(R.string.zodiac_virgo));
                        }
                    }

                } finally {
                    io.close();
                }
            }
        }
    }

    private String makeFirstLetterUpper(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(s.charAt(0))).append(
                s.substring(1).toLowerCase(Locale.CHINA));
        return sb.toString();
    }

    @Override
    public void contentChanged(Content content) {
        if (content != null) {
            User user = (User) content;
            UserIOSession io = user.getIOSession();
            try {

                mActionBarTitle.setText(getResources().getString(
                        R.string.profile_details_title));

                P1Application.picasso.load(Uri.parse(io.getCoverUrl()))
                        .placeholder(null).into(mCoverImage);
                P1Application.picasso
                        .load(Uri.parse(io.getProfileThumb100Url())).noFade()
                        .placeholder(null).into(mThumbImage);
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
                mCareerTextView.setText(positionStr + link + companyStr);

                mSchoolTextView.setText(io.getEducation());
                mPositionTextView.setText(io.getCareerPosition());
                mCompanyTextView.setText(io.getCareerCompany());
                mLocationTextView.setText(io.getCity());

                if (StringUtils.isEmpty(io.getCareerCompany())) {
                    mCompanyLinearLayout.setVisibility(View.GONE);
                } else {
                    mCompanyLinearLayout.setVisibility(View.VISIBLE);
                }

                if (StringUtils.isEmpty(io.getCareerPosition())) {
                    mSchoolLinearLayout.setVisibility(View.GONE);
                } else {
                    mSchoolLinearLayout.setVisibility(View.VISIBLE);
                }
                
                if (StringUtils.isEmpty(io.getCareerPosition())) {
                    mPositionLinearLayout.setVisibility(View.GONE);
                } else {
                    mPositionLinearLayout.setVisibility(View.VISIBLE);
                }

                if (StringUtils.isEmpty(io.getCity())) {
                    mLocationRelativeLayout.setVisibility(View.GONE);
                } else {
                    mLocationRelativeLayout.setVisibility(View.VISIBLE);
                }

                if (StringUtils.isEmpty(io.getCity())
                        && StringUtils.isEmpty(io.getEducation())
                        && StringUtils.isEmpty(io.getCareerPosition())
                        && StringUtils.isEmpty(io.getCareerCompany())) {
                    mProfessionalLinearLayout.setVisibility(View.GONE);
                } else {
                    mProfessionalLinearLayout.setVisibility(View.VISIBLE);
                }
            } finally {
                io.close();
            }

        }
    }
}
