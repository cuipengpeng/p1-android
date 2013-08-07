/**
 * LoginActivity.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.p1.mobile.p1android.ForgotPasswordListener;
import com.p1.mobile.p1android.LoginHandler;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.net.ApiCalls;
import com.p1.mobile.p1android.net.NonAuthenticatedMethods;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.AbstractAction;
import com.p1.mobile.p1android.ui.widget.P1TextView;

/**
 * @author Viktor Nyblom
 * 
 */
public class LoginActivity extends FlurryActivity implements LoginHandler,
        ForgotPasswordListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private ProgressDialog mProgressDialog;
    // private UserLoginTask mLoginTask;

    private Button mLoginButton;
    private EditText mNameText;
    private EditText mPassText;
    private ViewSwitcher mForgetSwitcher;

    private String mUsername;

    private static final int ANIM_DUE = 400;

    private String mPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);

        P1ActionBar actionBar = (P1ActionBar) findViewById(R.id.login_actionbar);
        TextView title = new P1TextView(this);
        title.setTextAppearance(this, R.style.P1LargerTextLight);
        title.setGravity(Gravity.CENTER);
        title.setText(R.string.login);
        actionBar.setCenterView(title);
        final AbstractAction closeAction = new AbstractAction(
                R.drawable.btn_contextual_close) {
            @Override
            public void performAction() {
                finish();
            }
        };
        actionBar.setLeftAction(closeAction);
        Log.e(TAG, "Starting login activity");

        mLoginButton = (Button) findViewById(R.id.login_login);
        mNameText = (EditText) findViewById(R.id.login_username);
        mPassText = (EditText) findViewById(R.id.login_password);
        mForgetSwitcher = (ViewSwitcher) findViewById(R.id.login_forget_switcher);
        mForgetSwitcher.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mForgetSwitcher.showNext();
                // due to the anim system in API 9 is every limited, focus must
                // change before anim
                mLoginButton.requestFocus();
                boolean forget = mForgetSwitcher.getDisplayedChild() == 1;
                mLoginButton.setText(forget ? R.string.reset_password
                        : R.string.login);
                mPassText.setVisibility(forget ? View.GONE : View.VISIBLE);
                mLoginButton.setOnClickListener(forget ? mOnReset : mOnLogin);
            }
        });

        mLoginButton.setOnClickListener(mOnLogin);
    }

    private OnClickListener mOnLogin = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mUsername = mNameText.getText().toString();
            mPassword = mPassText.getText().toString();
            tryLogin();
        }
    };

    private OnClickListener mOnReset = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mUsername = mNameText.getText().toString();
            if (!TextUtils.isEmpty(mUsername)) {
                NonAuthenticatedMethods.forgotPassword(mUsername,
                        LoginActivity.this);
                showProgress();
            } else {
                Toast.makeText(LoginActivity.this,
                        getText(R.string.login_empty_credentials),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void forgotPasswordSuccessful() {
        hideProgress();
        toast(getString(R.string.login_forget_success), Toast.LENGTH_LONG);
    }

    @Override
    public void forgotPasswordFailed() {
        hideProgress();
        toast(getString(R.string.login_forget_failed), Toast.LENGTH_LONG);
    }

    private void tryLogin() {

        // onStartInvitationSignup("22c11c790833b56cf623dcf91966b5f1");
        if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
            Toast.makeText(this, getText(R.string.login_empty_credentials),
                    Toast.LENGTH_LONG).show();
        } else {
            showProgress();
            ((P1Application) getApplication())
                    .login(mUsername, mPassword, this);
        }
    }

    @Override
    public void onSuccessfulLogin() {
        hideProgress();
        Log.d(TAG, "Login success");
        setResult(Activity.RESULT_OK);
        MainActivity.startMainActivity(this);
    }

    @Override
    public void onFailedLogin() {
        hideProgress();
        
        //hide SoftInputMethod
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                mPassText.getWindowToken(), 0);
        
        showDialog(getString(R.string.login_wrong_title),
                getString(R.string.login_wrong_credentials));
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        getResources().getString(R.string.dialog_ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                                
                                //show SoftInputMethod
                                InputMethodManager inputManager = (InputMethodManager) LoginActivity.this.getSystemService(
                                                Context.INPUT_METHOD_SERVICE);
                                inputManager.showSoftInput(mPassText, 0);
                                inputManager.showSoftInput(mNameText, 0);
                            }
                        }).show();
    }

    // not called now
    @Override
    public void onFailedConnection() {
        hideProgress();
        toast(getString(R.string.login_failed_connection), Toast.LENGTH_LONG);
    }

    public void onStartMigration(String migrationAccessToken) {
        hideProgress();
        String url = ApiCalls.getWebMigrationUrl(migrationAccessToken);
        startWebViewActivity(url);
    }

    @Override
    public void onStartActivation(String activationAccessToken) {
        hideProgress();
        String url = ApiCalls.getWebInvitationUrl(activationAccessToken);
        startWebViewActivity(url);
    }

    private void startWebViewActivity(String url) {
        Intent intent = new Intent(this, LoginWebViewActivity.class);
        intent.putExtra(LoginWebViewActivity.URL_KEY, url);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            tryLogin();
    }

    public void toast(CharSequence message, int length) {
        Toast.makeText(this, message, length).show();
    }

    @SuppressWarnings("deprecation")
    private void showProgress() {
        showDialog(0);
    }

    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        mProgressDialog = dialog;
        return dialog;
    }

}
