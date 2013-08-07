package com.p1.mobile.p1android.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.ChangePasswordListener;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.logic.WriteAccount;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;

public class ChangePasswordFragment extends Fragment implements  OnActionListener, ChangePasswordListener,OnClickListener {
    private static final String TAG = SettingFragment.class.getSimpleName();
    private static final int PASSWORD_AT_LEAST_LENGTH = 6;
    
    private String mLoggedInUserId;
    
    private P1ActionBar mActionBar;
    private TextView mActionBarTitleTextView;
    
    private EditText mCurrentPasswordEditText; 
    private EditText mNewPasswordEditText; 
    private EditText mConfirmPasswordEditText; 
    
    private LinearLayout mCurrentPasswordLinearLayout;
    private LinearLayout mNewPasswordLinearLayout;
    private LinearLayout mConfirmPasswordLinearLayout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoggedInUserId = NetworkUtilities.getSafeLoggedInUserId();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.change_password_fragment,
                container, false);

        initActionBar(inflater, view);
        mCurrentPasswordLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_change_password_current_passwd);
        mCurrentPasswordLinearLayout.setOnClickListener(this);
        mNewPasswordLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_change_password_new_passwd);
        mNewPasswordLinearLayout.setOnClickListener(this);
        mConfirmPasswordLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_change_password_confirm_passwd);
        mConfirmPasswordLinearLayout.setOnClickListener(this);
        
        mCurrentPasswordEditText = (EditText) view.findViewById(R.id.et_change_passwd_current_passwd);
        mNewPasswordEditText = (EditText) view.findViewById(R.id.et_change_passwd_new_passwd); 
        mConfirmPasswordEditText = (EditText) view.findViewById(R.id.et_change_passwd_confirm_passwd); 
        
        return view;
    }

    private void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView
                .findViewById(R.id.user_profile_action_bar);
        mActionBarTitleTextView = new P1TextView(getActivity());
        mActionBarTitleTextView.setText(getResources().getString(R.string.change_passwd_title));
        mActionBarTitleTextView.setTextAppearance(getActivity(),
                R.style.P1LargerTextLight);
        mActionBarTitleTextView.setGravity(Gravity.CENTER);
        mActionBar.setCenterView(mActionBarTitleTextView);

        mActionBar.setLeftAction(new ListenerAction(
                R.drawable.back_arrow_button, this));

        Button button = (Button) inflater.inflate(R.layout.small_blue_button, null);
        button.setOnClickListener(new ActionBarRightListener());
        button.setText(getResources().getString(R.string.edit_user_info_save));
        mActionBar.setRightView(button);
    }

    private class ActionBarRightListener implements OnClickListener {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
                String currentPassword = mCurrentPasswordEditText.getText().toString().trim();
                String newPassword = mNewPasswordEditText.getText().toString().trim();
                String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();
                
                if(newPassword.length()<PASSWORD_AT_LEAST_LENGTH
                        ||confirmPassword.length()<PASSWORD_AT_LEAST_LENGTH ) {
                    showDialog(getResources().getString(R.string.change_passwd_dialog_passwd_at_least));
                    return ;
                }
                
                if(!newPassword.equals(confirmPassword)) {
                    showDialog(getResources().getString(R.string.change_passwd_dialog_not_match));
                    return ;
                }
                
                //TODO send requester to  changePassword                    
            WriteAccount.changePassword(currentPassword, newPassword,
                    ChangePasswordFragment.this, (P1Application) getActivity()
                            .getApplication());
            
        }
    }

    @Override
    public void onAction() {
        showSettingFragment();
    }
    
    private void showDialog(String message) {
        new AlertDialog.Builder(getActivity()).setTitle("")
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showSettingFragment() {
        hideSoftInputMethod();
        
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_setting_activity_content, SettingFragment.newInstance(null));
        ft.commit();
    }
    
    private void hideSoftInputMethod() {
        mCurrentPasswordEditText.clearFocus();
        mNewPasswordEditText.clearFocus();
        mConfirmPasswordEditText.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) ChangePasswordFragment.this
                .getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                mCurrentPasswordEditText.getWindowToken(), 0);
    }

    @Override
    public void passwordChangeSuccessful() {
        showSettingFragment();
        showDialog(getResources().getString(R.string.change_passwd_dialog_update_success));
    }

    @Override
    public void passwordChangeFailed(FailureReason failureReason) {
      showDialog(getResources().getString(R.string.change_passwd_dialog_password_incorrect));
      return;        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.ll_change_password_current_passwd:
            mCurrentPasswordEditText.requestFocus();
            showSoftInputMethod();
            break;
        case R.id.ll_change_password_new_passwd:
            mNewPasswordEditText.requestFocus();
            showSoftInputMethod();
            break;
        case R.id.ll_change_password_confirm_passwd:
            mConfirmPasswordEditText.requestFocus();
            showSoftInputMethod();
            break;
        }
    }

    private void showSoftInputMethod() {
        InputMethodManager inputManager = (InputMethodManager) mCurrentPasswordEditText
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mCurrentPasswordEditText, 0);
    }

}
