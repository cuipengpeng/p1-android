package com.p1.mobile.p1android.ui.helpers;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Comment.CommentIOSession;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IChildContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IhasTimers;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadComment;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class CommentItemHolder implements IContentRequester,
        IChildContentRequester, IhasTimers {

    private TextView commentView;
    private ImageView imageView;

    private Date createdTime;
    private Timer timer = new Timer();
    private String comment;
    private String preferredFullName;
    private View contentView;

    private IContentRequester userRequester = new IContentRequester() {

        @Override
        public void contentChanged(Content content) {
            if (content == null)
                return;
            UserIOSession io = ((User) content).getIOSession();
            try {
                preferredFullName = io.getPreferredFullName();
                String profileUrl = io.getProfileThumb100Url();
                if (profileUrl != null && imageView != null) {
                    P1Application.picasso
                            .load(Uri.parse(profileUrl)).noFade()
                            .placeholder(null).into(imageView);
                }
                updateView();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
            }
        }
    };

    public CommentItemHolder(String commentId, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        contentView = inflater.inflate(R.layout.comment_row, parent, false);
        commentView = (TextView) contentView
                .findViewById(R.id.comment_main_text);
        imageView = (ImageView) contentView.findViewById(R.id.comment_user_img);
        contentChanged(ReadComment.requestComment(commentId, this));
        contentView.setTag(this);
        parent.addView(contentView);
    }

    public View getView() {
        return contentView;
    }

    @Override
    public void contentChanged(Content content) {
        if (content == null)
            return;
        CommentIOSession io = ((CommentIOSession) content.getIOSession());
        try {
            createdTime = io.getCreatedTime();
            if (createdTime != null) {
                setTimeStampTask(createdTime);
            }
            final String ownerId = io.getOwnerId();
            if (ownerId != null && !ownerId.isEmpty()) {
                ContentHandler.getInstance().removeRequester(userRequester);
                userRequester.contentChanged(ReadUser.requestUser(ownerId,
                        userRequester));
                contentView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.openProfile(contentView.getContext(), ownerId);
                    }
                });
            }
            comment = io.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
            updateView();
        }
    }

    private void updateView() {
        if (isStringSet(preferredFullName) && isStringSet(comment)) {
            String storyTitle = preferredFullName + "  ";
            Spannable WordtoSpan = new SpannableString(TextUtils.concat(
                    storyTitle,
                    Utils.getEmoticons(contentView.getContext(), comment)));
            WordtoSpan.setSpan(new ForegroundColorSpan(Color.BLACK), 0,
                    preferredFullName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            commentView.setText(WordtoSpan);
        }
    }

    private boolean isStringSet(String s) {
        return s != null && !s.isEmpty();
    }

    @Override
    public void removeChildRequestors() {
        if (imageView != null)
            imageView.setImageDrawable(null);
        ContentHandler.getInstance().removeRequester(userRequester);
    }

    private void setTimeStampTask(final Date createdTime) {
        if (createdTime != null) {
            timer.cancel();
            timer = new Timer();
            TextView tv = (TextView) contentView
                    .findViewById(R.id.comment_time_stamp);
            tv.setText(Utils.getTimeDifference(createdTime, tv.getContext()));
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    final TextView tv = (TextView) contentView
                            .findViewById(R.id.comment_time_stamp);
                    if (tv != null) {
                        tv.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText(Utils.getTimeDifference(createdTime,
                                        tv.getContext()));
                            }
                        }, 0);
                    }
                }
            }, 0, 1000 * 30); // Every minute
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
