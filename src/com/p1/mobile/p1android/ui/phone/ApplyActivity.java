package com.p1.mobile.p1android.ui.phone;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.p1.mobile.p1android.ApplyListener;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.net.NonAuthenticatedMethods;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.AbstractAction;
import com.p1.mobile.p1android.ui.widget.P1TextView;

public class ApplyActivity extends FlurryActivity implements OnClickListener,
        ApplyListener {

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mEmail;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_apply);
        mFirstName = (EditText) findViewById(R.id.apply_firstname);
        mLastName = (EditText) findViewById(R.id.apply_lastname);
        mEmail = (EditText) findViewById(R.id.apply_email);
        P1ActionBar actionBar = (P1ActionBar) findViewById(R.id.login_actionbar);
        TextView title = new P1TextView(this);
        title.setTextAppearance(this, R.style.P1LargerTextLight);
        title.setGravity(Gravity.CENTER);
        title.setText(R.string.apply_title);
        actionBar.setCenterView(title);
        final AbstractAction closeAction = new AbstractAction(
                R.drawable.btn_contextual_close) {
            @Override
            public void performAction() {
                finish();
            }
        };
        actionBar.setLeftAction(closeAction);
        Button done = (Button) LayoutInflater.from(this).inflate(
                R.layout.small_blue_button, null);
        done.setOnClickListener(this);
        done.setText(R.string.done);
        actionBar.setRightView(done);
    }

    @Override
    public void onClick(View v) {
        String first = mFirstName.getText().toString();
        String last = mLastName.getText().toString();
        String email = mEmail.getText().toString();
        if (!TextUtils.isEmpty(first) && !TextUtils.isEmpty(last)
                && !TextUtils.isEmpty(email)) {
            showProgress();
            NonAuthenticatedMethods.applyForAccount(first, last, email, this);
        } else {
            toast(getString(R.string.apply_missing_info), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void applySuccessful() {
        hideProgress();
        toast(getString(R.string.apply_success), Toast.LENGTH_LONG);
        finish();
    }

    @Override
    public void applyFailed() {
        hideProgress();
        toast(getString(R.string.apply_failed), Toast.LENGTH_LONG);
    }

    @SuppressWarnings("deprecation")
    private void showProgress() {
        showDialog(0);
    }

    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        mProgressDialog = dialog;
        return dialog;
    }

    public void toast(CharSequence message, int length) {
        Toast.makeText(this, message, length).show();
    }
}
