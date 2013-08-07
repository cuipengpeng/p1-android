package com.p1.mobile.p1android.ui.helpers;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Comment;
import com.p1.mobile.p1android.content.Comment.CommentIOSession;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IhasTimers;
import com.p1.mobile.p1android.content.IdTypePair;
import com.p1.mobile.p1android.content.IdTypePair.Type;
import com.p1.mobile.p1android.content.NotificationStory;
import com.p1.mobile.p1android.content.NotificationStory.NotificationIOSession;
import com.p1.mobile.p1android.content.NotificationStory.Relevance;
import com.p1.mobile.p1android.content.NotificationStory.StoryType;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadComment;
import com.p1.mobile.p1android.content.logic.ReadNotification;
import com.p1.mobile.p1android.content.logic.ReadPicture;
import com.p1.mobile.p1android.content.logic.ReadShare;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.ui.phone.FeedItemActivity;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class NotificationInformation implements IContentRequester, IhasTimers {
    private static final String TAG = NotificationInformation.class
            .getSimpleName();

    private Context viewContext;
    private Timer timer = new Timer();
    private NotificationImageHelper imgHolder;
    private String userId;
    private String caption;
    private String eventText;
    private String userName;
    private String commentPreview;
    private View view;
    private String debugToast;
    private IdTypePair mToplevel;
    private Relevance mRelevance;

    private IContentRequester userRequester = new IContentRequester() {
        @Override
        public void contentChanged(Content content) {
            UserIOSession io = ((com.p1.mobile.p1android.content.User) content)
                    .getIOSession();
            try {
                userName = io.getPreferredFullName();
                ImageView imageView = (ImageView) view
                        .findViewById(R.id.noti_user_img);
                P1Application.picasso
                        .load(Uri.parse(io.getProfileThumb100Url())).noFade()
                        .placeholder(null).into(imageView);
                updateEventText();
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }
    };
    private IContentRequester commentRequester = new IContentRequester() {
        @Override
        public void contentChanged(Content content) {
            CommentIOSession io = ((Comment) content).getIOSession();
            try {
                commentPreview = io.getValue();
                updateEventText();
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }
    };
    private IContentRequester shareRequester = new IContentRequester() {
        @Override
        public void contentChanged(Content content) {
            ShareIOSession io = ((Share) content).getIOSession();
            try {
                caption = io.getSafeCaption();
                updateEventText();
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }
    };
    private IContentRequester pictureRequester = new IContentRequester() {
        @Override
        public void contentChanged(Content content) {
            PictureIOSession io = ((Picture) content).getIOSession();
            try {
                caption = io.getCaption();
                updateEventText();
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }
    };

    public NotificationInformation(View view) {
        setView(view);
    }

    public void removeRequesters() {
        ContentHandler.getInstance().removeRequester(this);
        ContentHandler.getInstance().removeRequester(userRequester);
        ContentHandler.getInstance().removeRequester(imgHolder);
        ContentHandler.getInstance().removeRequester(commentRequester);
        ContentHandler.getInstance().removeRequester(shareRequester);
        ContentHandler.getInstance().removeRequester(pictureRequester);
    }

    public void setView(View view) {
        commentPreview = null;
        caption = null;
        this.view = view;
        viewContext = view.getContext();
    }

    public void requestUpdateForId(String string) {
        removeRequesters();
        commentPreview = null;
        caption = null;
        contentChanged(ReadNotification.requestNotification(string, this));
    }

    @Override
    public void contentChanged(Content content) {
        NotificationIOSession ioSession = ((NotificationStory) content)
                .getIOSession();
        try {
            String id = ioSession.getSourceUserId();
            if (id != null && !id.equals(userId)) {
                userId = id;
                ContentHandler.getInstance().removeRequester(userRequester);
            }
            userRequester.contentChanged(ReadUser
                    .requestUser(id, userRequester));
            view.findViewById(R.id.noti_new_badge).setVisibility(
                    ioSession.isRead() ? View.GONE : View.VISIBLE);
            mToplevel = ioSession.getTopLevelObject();
            mRelevance = ioSession.getRelevance();
            // Utils.attachRefreshTimer(timer, ioSession.getCreatedTime(), 30,
            // (TextView) this.view.findViewById(R.id.noti_time_stamp));
            setTimeStampTask(ioSession.getCreatedTime());
            StringBuilder storyText = new StringBuilder();
            switch (ioSession.getRelevance()) {
            case OWNER:
                addOwnerText(ioSession, storyText);
                break;
            case COMMENTED:
                addCommentText(ioSession, storyText);
                addEventImage(ioSession.getTopLevelObject());
                break;
            case MENTIONED:
                addMentionText(ioSession, storyText);
                hideEventImage();
                break;
            case FOLLOWED:
                storyText.append(viewContext
                        .getString(R.string.noti_started_following_you));
                hideEventImage();
                break;
            default:
                Log.w(TAG, "Notification missing");
                break;
            }
            setDebugToast(content.toString());
            eventText = storyText.toString();
            updateEventText();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ioSession.close();
        }
    }

    private void addOwnerText(NotificationIOSession ioSession,
            StringBuilder storyText) {

        StoryType storyType = ioSession.getStoryType();
        switch (storyType) {
        case COMMENT:
            storyText
                    .append(viewContext.getString(R.string.noti_commented_on_));
            break;
        case LIKE:
            storyText.append(viewContext.getString(R.string.noti_liked_));
            break;
        case TAG:
        default:
            Log.w(TAG, "Missing tag notification");
            break;
        }
        Type type = ioSession.getLinkedObject().type;
        if (type.equals(IdTypePair.Type.PICTURE)) {
            storyText.append(viewContext.getString(R.string.noti_your_picture));
            addEventImage(ioSession.getLinkedObject());
        } else if (type.equals(IdTypePair.Type.COMMENT)) {
            storyText.append(viewContext
                    .getString(R.string.noti_a_comment_on_your_));
            if (ioSession.getTopLevelObject().type.equals(Type.PICTURE)) {
                storyText.append(viewContext.getString(R.string.noti_picture));
                addEventImage(ioSession.getTopLevelObject());
            } else if (ioSession.getTopLevelObject().type.equals(Type.SHARE)) {
                storyText.append(viewContext.getString(R.string.noti_share));
                addEventImage(ioSession.getLinkedObject());
            }
            addEventCommentPreview(ioSession.getLinkedObject());
        } else if (type.equals(IdTypePair.Type.SHARE)) {
            storyText.append(viewContext.getString(R.string.noti_your_post));
            addEventImage(ioSession.getLinkedObject());
        } else {
            Log.w(TAG, "Missing owner notification: " + type);
            hideEventImage();
        }

        if (storyType.equals(StoryType.COMMENT)) {
            attemptToAddCommentPreview(ioSession);
        }
    }

    private void attemptToAddCommentPreview(NotificationIOSession ioSession) {
        if (ioSession.getCreatedObject().type.equals(Type.COMMENT)) {
            addEventCommentPreview(ioSession.getCreatedObject());
        } else if (ioSession.getLinkedObject().type.equals(Type.COMMENT)) {
            addEventCommentPreview(ioSession.getLinkedObject());
        } else if (ioSession.getTopLevelObject().type.equals(Type.COMMENT)) {
            addEventCommentPreview(ioSession.getTopLevelObject());
        } else {
            Log.w(TAG, "Missing comment preview");
        }
    }

    private void addEventCommentPreview(IdTypePair commentObject) {
        if (commentObject.type.equals(Type.COMMENT)) {
            ContentHandler.getInstance().removeRequester(commentRequester);
            commentRequester.contentChanged(ReadComment.requestComment(
                    commentObject.id, commentRequester));
        }
    }

    private void addCommentText(NotificationIOSession ioSession,
            StringBuilder storyText) {
        storyText.append(viewContext.getString(R.string.noti_commented_on_));
        switch (ioSession.getTopLevelObject().type) {
        case PICTURE:
            storyText.append(viewContext.getString(R.string.noti_this_picture));
            break;
        case SHARE:
            storyText.append(viewContext.getString(R.string.noti_this_post));
            break;
        default:
            break;
        }
        addEventCommentPreview(ioSession.getCreatedObject());
    }

    private void addMentionText(NotificationIOSession ioSession,
            StringBuilder storyText) {
        switch (ioSession.getLinkedObject().type) {
        case COMMENT:
            storyText.append(viewContext
                    .getString(R.string.noti_mentioned_you_));
            storyText.append(viewContext
                    .getString(R.string.noti_in_this_comment));
            addEventCommentPreview(ioSession.getLinkedObject());
            break;
        case SHARE:
            storyText.append(viewContext
                    .getString(R.string.noti_mentioned_you_));
            storyText.append(viewContext.getString(R.string.noti_on_a_post));
            addShareCaption(ioSession.getLinkedObject());
            break;
        case PICTURE:
            storyText.append(viewContext
                    .getString(R.string.noti_mentioned_you_));
            storyText.append(viewContext
                    .getString(R.string.noti_in_a_picture_caption));
            addPictureCaption(ioSession.getLinkedObject());
            break;
        case TAG:
            Log.w(TAG, "Missing mention for tags");
            break;
        default:
            break;
        }
    }

    private void addShareCaption(IdTypePair linkedObject) {
        if (linkedObject.type.equals(Type.SHARE)) {
            ContentHandler.getInstance().removeRequester(shareRequester);
            shareRequester.contentChanged(ReadShare.requestShare(
                    linkedObject.id, shareRequester));
        }
    }

    private void addPictureCaption(IdTypePair linkedObject) {
        if (linkedObject.type.equals(Type.PICTURE)) {
            ContentHandler.getInstance().removeRequester(pictureRequester);
            pictureRequester.contentChanged(ReadShare.requestShare(
                    linkedObject.id, pictureRequester));
        }
    }

    private void addEventImage(IdTypePair pair) {
        switch (pair.type) {
        case PICTURE:
            addEventImage(pair.id,
                    (ImageView) view.findViewById(R.id.noti_event_img));
            break;
        default:
            hideEventImage();
            break;
        }
    }

    private void hideEventImage() {
        view.findViewById(R.id.noti_event_img).setVisibility(View.GONE);
    }

    private void addEventImage(String pictureId, ImageView mImgView) {
        ContentHandler.getInstance().removeRequester(imgHolder);
        imgHolder = new NotificationImageHelper(mImgView);
        imgHolder.contentChanged(ReadPicture.requestPicture(pictureId,
                imgHolder));
    }

    private void setTimeStampTask(final Date createdTime) {
        if (createdTime != null) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
            TextView tv = (TextView) view.findViewById(R.id.noti_time_stamp);
            tv.setText(Utils.getRelativeTime(createdTime, tv.getContext()));
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    final TextView tv = (TextView) view
                            .findViewById(R.id.noti_time_stamp);
                    if (tv != null) {
                        tv.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText(Utils.getRelativeTime(createdTime,
                                        tv.getContext()));
                            }
                        }, 0);
                    }
                }
            }, 0, 1000 * 30); // Every 30sec
        }
    }

    private void updateEventText() {
        if (userName != null) {
            // TODO we should add caption string here if it is desireable. It's
            // a matter of design choice

            TextView userTV = (TextView) view.findViewById(R.id.noti_main_text);
            String storyText = userName + " " + eventText;
            if (isStringSet(eventText)) {
                if (isStringSet(commentPreview)) {
                    storyText = storyText + ": " + commentPreview;
                } else if (isStringSet(caption)) {
                    storyText = storyText + ": " + caption;
                }
            }
            Spannable WordtoSpan = new SpannableString(storyText);
            WordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0,
                    userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (isStringSet(eventText)) {
                WordtoSpan.setSpan(
                        new ForegroundColorSpan(viewContext.getResources()
                                .getColor(R.color.conversation_msg_grey)),
                        (userName + " ").length(),
                        getEventTextEndLength(storyText),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (isStringSet(commentPreview)) {
                WordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE),
                        (userName + " " + nullToEmpty(eventText) + ": ")
                                .length(), storyText.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            userTV.setText(WordtoSpan);
        }
    }

    private int getEventTextEndLength(String CompleteString) {
        int length = (isStringSet(eventText)) ? (userName + " " + eventText + ": ")
                .length() : (userName + " ").length();
        return Math.min(length, CompleteString.length());
    }

    private String nullToEmpty(String str) {
        return (str == null) ? "" : str;
    }

    private boolean isStringSet(String s) {
        return s != null && !s.isEmpty();
    }

    public String getDebugToast() {
        return debugToast;
    }

    public void setDebugToast(String debugToast) {
        this.debugToast = debugToast;
    }

    public void openProfile() {
        Utils.openProfile(viewContext, userId);
    }

    public void openContent() {
        // TODO open whichever linked/created/top level object desireable here.
        // Save it from ContentChanged so you have it when the item is clicked.
        if (Relevance.FOLLOWED.equals(mRelevance)) {
            openProfile();
            return;
        }
        Intent intent;
        switch (mToplevel.type) {
        case PICTURE:
            intent = new Intent(viewContext, FeedItemActivity.class);
            intent.putExtra("pictureId", mToplevel.id);
            viewContext.startActivity(intent);
            break;
        case SHARE:
            intent = new Intent(viewContext, FeedItemActivity.class);
            intent.putExtra("shareId", mToplevel.id);
            viewContext.startActivity(intent);
            break;
        case USER:
            Utils.openProfile(viewContext, mToplevel.id);
            break;
        default:
            Log.w(TAG, "Top level object not handled");
            break;
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
