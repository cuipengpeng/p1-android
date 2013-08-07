package com.p1.mobile.p1android.ui.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.Action;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class UserPictureView extends RelativeLayout implements
        IContentRequester, OnClickListener {
    private static final String TAG = UserPictureView.class.getSimpleName();

    private ImageView mImageView;
    private User mUser;
    private String mUserId;
    private Action mAction;
    private RelativeLayout mNotificationsHolder;

    /**
     * Default constructor with no Action and the user set to me.
     * 
     * @param context
     * @param attrs
     */
    public UserPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserPictureView(Context context, AttributeSet attrs, String userId,
            Action action) {
        this(context, attrs);
        mAction = action;
        mUserId = userId;
    }

    public void setAction(Action action) {
        mAction = action;
        Log.d(TAG, "setAction");
    }

    public void setUser(String userId) {
        ContentHandler.getInstance().removeRequester(this);
        mUserId = userId;
        if (userId != null) {
            mUser = ReadUser.requestUser(mUserId, null);
            Log.d(TAG, "setUser id " + mUserId);
            displayUserPicture();
        } else {
            Log.d(TAG, "USer ID null ");
        }
    }

    public void setNotificationsView(CounterBubble notificationsView) {
        mNotificationsHolder.addView(notificationsView);
    }

    private void init(Context context) {
        Log.d(TAG, "init");

        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.user_picture_view_layout, this, false);
        mImageView = (ImageView) layout.findViewById(R.id.userPictureImageView);
        mNotificationsHolder = (RelativeLayout) layout
                .findViewById(R.id.notificationsHolder);
        this.addView(layout);




        setOnClickListener(this);
    }

    private void displayUserPicture() {
        Log.d(TAG, "displayUserPicture id " + mUserId);
        if (mUser != null) {
            UserIOSession io = mUser.getIOSession();
            try {
                if (io.getProfileThumb50Url() != null) {
                    mImageView.setImageDrawable(null);
                    P1Application.picasso
                            .load(Uri.parse(io.getProfileThumb50Url()))
                            .noFade().placeholder(null).into(mImageView);
                }
            } finally {
                io.close();
            }
            mNotificationsHolder.bringToFront();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onDetachedFromWindow();
        if (mUserId != null) {
            mUser = ReadUser.requestUser(mUserId, this);
            displayUserPicture();
        } else {
            mUser = ReadUser.requestLoggedInUser(this);
            displayUserPicture();
        }

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "UserPictureView detached from window");
        ContentHandler.getInstance().removeRequester(this);
    }

    @Override
    public void contentChanged(Content content) {
        Log.d(TAG, "UserPictureView contentChanged");
        if (!(content instanceof User)) {
            return;
        }
        mUser = (User) content;
        displayUserPicture();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick acatio " + mAction);
        InputMethodManager imm = (InputMethodManager) v.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        mAction.performAction();
    }

}
