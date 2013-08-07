package com.p1.mobile.p1android.ui.helpers;

import android.net.Uri;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.WriteFollow;

public class FriendViewHolder implements IContentRequester,
        OnCheckedChangeListener {
    private static final String TAG = FriendViewHolder.class.getSimpleName();
    public TextView textView;
    public ImageView imageView;
    public ToggleButton follow;
    private String userId;

    public FriendViewHolder(
            TextView textView, ImageView imageView, ToggleButton follow) {
        updateView(textView, imageView, follow);
    }

    public void updateView(TextView textView, ImageView imageView,
            ToggleButton follow) {
        if (textView != this.textView && imageView != this.imageView) {
            this.textView = textView;
            this.imageView = imageView;
            this.follow = follow;
            resetViews();
        }
        this.textView = textView;
        this.imageView = imageView;

    }

    @Override
    /** Called when the User displayed by the ViewHolder views is updated */
    public void contentChanged(Content content) {
        if (content == null) {
            resetViews();
            return;
        }
        final UserIOSession io = ((com.p1.mobile.p1android.content.User) content)
                .getIOSession();
        try {
            userId = io.getId();
            String userName = io.getPreferredFullName();
            textView.setText(userName != null ? userName : "Unknown");
            P1Application.picasso.load(Uri.parse(io.getProfileThumb100Url()))
                    .noFade().placeholder(null).into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        } finally {
            io.close();
        }
    }

    public void resetViews() {
        userId = null;
        textView.setText("Unknown");
        imageView.setImageBitmap(null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (userId != null)
            WriteFollow.toggleFollow(userId);
    }
}