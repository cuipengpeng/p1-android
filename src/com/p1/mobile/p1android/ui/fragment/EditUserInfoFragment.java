package com.p1.mobile.p1android.ui.fragment;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Profile;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.logic.ReadAccount;
import com.p1.mobile.p1android.content.logic.ReadProfile;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.content.logic.WriteAccount;
import com.p1.mobile.p1android.content.logic.WriteProfile;
import com.p1.mobile.p1android.content.logic.WriteUser;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;

/**
 * 
 * @author Cui pengpeng
 * 
 */

public class EditUserInfoFragment extends Fragment implements OnActionListener {
    private static final String TAG = UserProfileFragment.class.getSimpleName();
    private static final String CONTENT_KEY = "content";
    private static final String START_CODE_KEY = "startCode";

    private P1ActionBar mActionBar;
    private TextView mActionBarTitleTextView;

    private P1TextView mBottomDescriptionTextView;
    private EditText mContentEditText;
    private String mOriginalContentStr;

    private int mStartCode;
    private String mContentStr;

    private User mLoggedInUser;
    private Profile mLoggedInProfile;
    private Account mLoggedInAccount;

    public static Fragment newInstance(int startCode, String previousText) {
        EditUserInfoFragment fragment = new EditUserInfoFragment();
        if (previousText != null) {
            Bundle bundle = new Bundle();
            bundle.putString(CONTENT_KEY, previousText);
            bundle.putInt(START_CODE_KEY, startCode);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mStartCode = (Integer) getArguments().get(START_CODE_KEY);
            mContentStr = (String) getArguments().get(CONTENT_KEY);
            mOriginalContentStr = (String) getArguments().get(CONTENT_KEY);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.edit_user_info_fragment,
                container, false);
        mContentEditText = (EditText) view.findViewById(R.id.et_edit_user_info);
        mBottomDescriptionTextView = (P1TextView) view
                .findViewById(R.id.tv_edit_user_info_desc);

        initActionBar(inflater, view);

        return view;
    }

    private void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView
                .findViewById(R.id.user_profile_action_bar);
        mActionBarTitleTextView = new P1TextView(getActivity());
        mActionBarTitleTextView.setTextAppearance(getActivity(),
                R.style.P1LargerTextLight);
        mActionBarTitleTextView.setGravity(Gravity.CENTER);
        mActionBar.setCenterView(mActionBarTitleTextView);

        mActionBar.setLeftAction(new ListenerAction(
                R.drawable.back_arrow_button, this));

        Button button = (Button) inflater.inflate(R.layout.edit_user_info_save,
                null);
        button.setOnClickListener(new ActionBarSaveListener());
        if (mStartCode == SettingFragment.EDIT_EMAIL) {
            mContentEditText.setMinLines(1);
            mContentEditText.setSingleLine(true);
            button.setText(getResources().getString(
                    R.string.edit_user_info_update));
        } else {
            button.setText(getResources().getString(
                    R.string.edit_user_info_save));
            mContentEditText.setMinLines(5);
            mContentEditText.setSingleLine(false);
        }
        mActionBar.setRightView(button);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mLoggedInUser = ReadUser.requestLoggedInUser(null);
        mLoggedInProfile = ReadProfile.requestLoggedInProfile(null);
        mLoggedInAccount = ReadAccount.requestAccount(null);

        mActionBarTitleTextView.setText(getTitle(mStartCode));

        if (mContentStr.equals(getResources().getString(
                R.string.edit_profile_desc_yourself))
                || mContentStr.equals(getResources().getString(
                        R.string.profile_detail_unknown))
                || mContentStr.equals(EditProfileFragment.ELLIPSIS)) {
            mContentEditText.setText("");
            mOriginalContentStr = null;
        } else {
            mContentEditText.setText(mContentStr);
        }

        // set bottom text below EditText
        if (mStartCode == EditProfileFragment.EDIT_DESCRIPTION) {
            mBottomDescriptionTextView.setText(getResources().getString(
                    R.string.edit_user_info_bottom_desc));
        } else if (mStartCode == EditProfileFragment.EDIT_GIVEN_NAME) {
            mBottomDescriptionTextView.setText(getResources().getString(
                    R.string.edit_user_info_bottom_given_name));
        } else if (mStartCode == EditProfileFragment.EDIT_FAMILY_NAME) {
            mBottomDescriptionTextView.setText(getResources().getString(
                    R.string.edit_user_info_bottom_family_name));
        } else if (mStartCode == EditProfileFragment.EDIT_SCHOOL) {
            mBottomDescriptionTextView.setText(getResources().getString(
                    R.string.edit_user_info_bottom_school));
        } else if (mStartCode == EditProfileFragment.EDIT_POSITION) {
            mBottomDescriptionTextView.setText(getResources().getString(
                    R.string.edit_user_info_bottom_position));
        } else if (mStartCode == EditProfileFragment.EDIT_COMPANY) {
            mBottomDescriptionTextView.setText(getResources().getString(
                    R.string.edit_user_info_bottom_company));
        } else if (mStartCode == EditProfileFragment.EDIT_CITY) {
            mBottomDescriptionTextView.setText(getResources().getString(
                    R.string.edit_user_info_bottom_location));
        }

        // Wait 200ms to finish loading view and data, and then show the
        // SoftInputMethod
        mContentEditText.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mContentEditText
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mContentEditText, 0);
            }

        }, 200);
    }

    private String getTitle(int startCode) {
        String title = "";
        switch (startCode) {
        case EditProfileFragment.EDIT_DESCRIPTION:
            title = getResources().getString(R.string.edit_profile_description);
            break;
        case EditProfileFragment.EDIT_GIVEN_NAME:
            title = getResources().getString(R.string.edit_profile_given_name);
            break;
        case EditProfileFragment.EDIT_FAMILY_NAME:
            title = getResources().getString(R.string.edit_profile_family_name);
            break;
        case EditProfileFragment.EDIT_SCHOOL:
            title = getResources().getString(R.string.edit_profile_school);
            break;
        case EditProfileFragment.EDIT_RELATIONSHIP:
            title = getResources().getString(
                    R.string.profile_detail_relationship);
            break;
        case EditProfileFragment.EDIT_BLOODTYPE:
            title = getResources()
                    .getString(R.string.profile_detail_blood_type);
            break;
        case EditProfileFragment.EDIT_POSITION:
            title = getResources().getString(R.string.profile_detail_position);
            break;
        case EditProfileFragment.EDIT_COMPANY:
            title = getResources().getString(R.string.profile_detail_company);
            break;
        case EditProfileFragment.EDIT_CITY:
            title = getResources().getString(R.string.profile_detail_location);
            break;
        case SettingFragment.EDIT_EMAIL:
            title = getResources().getString(R.string.setting_email);
            break;
        }
        return title;
    }

    private class ActionBarSaveListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            String contentStr = mContentEditText.getText().toString().trim();

            if (mStartCode == SettingFragment.EDIT_EMAIL) {
                if(StringUtils.isEmpty(contentStr)) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getString(
                                    R.string.edit_user_info_email),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                WriteAccount.changeEmail(mLoggedInAccount, contentStr);
            } else if (mStartCode == EditProfileFragment.EDIT_DESCRIPTION) {
                WriteProfile.changeDescription(mLoggedInProfile, contentStr);
            } else if (mStartCode == EditProfileFragment.EDIT_GIVEN_NAME) {
                if (StringUtils.isEmpty(contentStr)) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getString(
                                    R.string.edit_user_info_given_name),
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    WriteUser.changeEnglishGivenName(mLoggedInUser, contentStr);
                }
            } else if (mStartCode == EditProfileFragment.EDIT_FAMILY_NAME) {
                if (StringUtils.isEmpty(contentStr)) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getString(
                                    R.string.edit_user_info_family_name),
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    WriteUser.changeEnglishSurname(mLoggedInUser, contentStr);
                }
            } else if (mStartCode == EditProfileFragment.EDIT_SCHOOL) {
                WriteUser.changeEducation(mLoggedInUser, contentStr);
            } else if (mStartCode == EditProfileFragment.EDIT_POSITION) {
                WriteUser.changeCareerPosition(mLoggedInUser, contentStr);
            } else if (mStartCode == EditProfileFragment.EDIT_COMPANY) {
                WriteUser.changeCareerCompany(mLoggedInUser, contentStr);
            } else if (mStartCode == EditProfileFragment.EDIT_CITY) {
                WriteUser.changeCity(mLoggedInUser, contentStr);
            }
            backToLastFragment();

        }
    }

    @Override
    public void onAction() {
        if (mOriginalContentStr.equals(mContentEditText.getText().toString())
                || (StringUtils.isEmpty(mOriginalContentStr) && StringUtils
                        .isEmpty(mContentEditText.getText().toString()))) {
            backToLastFragment();
        } else {
            showChangedDialog();
        }
    }

    // show a Dialog
    private void showChangedDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.dialog_title))
                .setMessage(getResources().getString(R.string.dialog_message))
                .setPositiveButton(
                        getResources().getString(R.string.dialog_discard),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                                backToLastFragment();
                            }
                        })
                .setNegativeButton(
                        getResources().getString(R.string.dialog_cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    private void backToLastFragment() {
        // hide SoftInputMethod
        mContentEditText.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) EditUserInfoFragment.this
                .getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                mContentEditText.getWindowToken(), 0);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fm.popBackStack();
        ft.commit();
    }
}
