package com.p1.mobile.p1android.ui.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IChildContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.Venue.VenueIOSession;
import com.p1.mobile.p1android.content.logic.ReadPicture;
import com.p1.mobile.p1android.content.logic.ReadShare;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.content.logic.ReadVenue;
import com.p1.mobile.p1android.content.logic.WriteLike;
import com.p1.mobile.p1android.content.logic.WriteShare;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.fragment.LikerFragment.LikeContentType;
import com.p1.mobile.p1android.ui.fragment.UserProfileFragment;
import com.p1.mobile.p1android.ui.phone.FeedItemActivity;
import com.p1.mobile.p1android.ui.phone.UserProfileWrapperActivity;
import com.p1.mobile.p1android.ui.view.FeedImageView;
import com.p1.mobile.p1android.ui.view.FeedLinearLayout;
import com.p1.mobile.p1android.util.ScreenUtil;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class FeedItem implements IContentRequester, IChildContentRequester,
        OnCheckedChangeListener {
    private static final boolean ENABLE_MENU_BTN = false;

    public static final String TAG = FeedItem.class.getSimpleName();

    private Activity activity;

    private Picture.ImageFormat format;

    private List<String> likeUserIds = new ArrayList<String>();
    private List<String> pictureIds = new ArrayList<String>();
    private ArrayList<String> commentIds = new ArrayList<String>();
    private List<String> tagIds = new ArrayList<String>();

    private CheckBox likeCheck;
    private TextView timeStamp;
    private LinearLayout commentList;
    private TextView captionView;
    private TextView totalCommentsView;
    private ArrayList<ImageView> imageViews = new ArrayList<ImageView>();
    private TextView imageCountView;
    private LinearLayout likersLayout;

    private String shareId;
    private String caption = "";
    private boolean hasLiked;
    private Date createdTime;
    private int totalComments;
    private String ownerId;
    private String multiPictureSpecifiedPicture;
    private int totalLikes;
    private final boolean isSingleViewMode;
    private boolean commentsIsDirty = false;

    private FriendViewHolder userHolder;

    public FeedItem(Activity act, boolean isSingleViewMode) {
        this.activity = act;
        this.isSingleViewMode = isSingleViewMode;
    }

    private FeedLinearLayout view;

    private boolean isTrueShare() {
        return shareId != null && !shareId.isEmpty();
    }

    public boolean isMultishare() {
        return pictureIds.size() > 1;
    }

    public void setView(View v) {
        view = (FeedLinearLayout) v;
        view.setTag(this);
        if (view.getId() == R.id.feed_item_single) {
            view.findViewById(R.id.feed_top).setBackgroundDrawable(null);
            view.findViewById(R.id.feed_action).setBackgroundDrawable(null);
        } else {
            view.findViewById(R.id.feed_top).setBackgroundResource(
                    R.drawable.feed_item_background);
            view.findViewById(R.id.feed_action).setBackgroundResource(
                    R.color.images_feed_background);
        }
        timeStamp = (TextView) view.findViewById(R.id.feed_timestamp);
        likeCheck = (CheckBox) view.findViewById(R.id.feed_heart_icon);
        captionView = (TextView) view.findViewById(R.id.feed_caption);
        imageCountView = (TextView) view
                .findViewById(R.id.feed_grid_images_count);
        totalCommentsView = (TextView) view
                .findViewById(R.id.feed_total_comments);
        likersLayout = (LinearLayout) view
                .findViewById(R.id.likers_image_container);

        likersLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTrueShare()) {
                    Utils.startLikerActivity(activity, shareId,
                            LikeContentType.SHARE);
                } else {
                    Utils.startLikerActivity(activity,
                            multiPictureSpecifiedPicture,
                            LikeContentType.PICTURE);
                }
            }
        });
        TextView textView = (TextView) view.findViewById(R.id.feed_user_name);
        ImageView userImage = (ImageView) view
                .findViewById(R.id.feed_event_user_img);
        textView.setOnClickListener(clickStartUserProfile);
        userImage.setOnClickListener(clickStartUserProfile);

        userHolder = new FriendViewHolder(textView, userImage, null);
        commentList = (LinearLayout) view.findViewById(R.id.feed_comment_list);
        commentList.removeAllViews();
        for (int i = 0; i < 9; i++) {
            ImageView imageView = (ImageView) view
                    .findViewById(IMAGE_VIEW_IDS[i]);
            if (imageView != null) {
                imageViews.add(imageView);
                imageView.setTag(new ImageRequester(imageView));
            } else {
                break;
            }
        }

        if (!isSingleViewMode) {
            for (ImageView imageView : imageViews) {
                imageView.setOnClickListener(clickImageStartPictureView);
            }
            view.findViewById(R.id.feed_text).setOnClickListener(
                    clickStartSingleItemView);
            view.findViewById(R.id.feed_text).setBackgroundResource(
                    R.drawable.feed_item_bottom_background);

            view.findViewById(R.id.feed_caption).setOnClickListener(
                    clickStartSingleItemView);
            commentList.setVisibility(View.GONE);
        } else {

            commentList.setVisibility(View.VISIBLE);

        }

        if (ENABLE_MENU_BTN) {
            view.findViewById(R.id.feed_event_options).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String loggedInUserId = NetworkUtilities
                                    .getLoggedInUserId();
                            Builder builder = new AlertDialog.Builder(activity);
                            if (loggedInUserId.equals(ownerId)) {
                                builder.setItems(new String[] { activity
                                        .getString(R.string.delete) },
                                        new AlertDialog.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                if (which == 0) {
                                                }
                                            }
                                        });
                            } else {
                                builder.setItems(
                                        new String[] { activity
                                                .getString(R.string.report_inappropriate) },
                                        new AlertDialog.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                if (which == 0) {
                                                }
                                            }
                                        });
                            }
                            AlertDialog dialog = builder.create();
                            dialog.setCancelable(true);
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.show();
                        }
                    });
        } else {
            view.findViewById(R.id.feed_event_options).setVisibility(View.GONE);
        }
    }

    private OnClickListener clickStartSingleItemView = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startSingleItemView(v.getContext());
        }
    };

    private OnClickListener clickImageStartPictureView = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageRequester p = (ImageRequester) v.getTag();
            if (p != null && p.id != null)
                startPictureView(ownerId, p.id);
        }

    };

    private OnClickListener clickStartUserProfile = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageRequester p = (ImageRequester) v.getTag();
            if (p != null && p.id != null)
                startPictureView(ownerId, p.id);
            Intent intent = new Intent(activity,
                    UserProfileWrapperActivity.class);
            intent.putExtra(UserProfileFragment.USER_ID_KEY, ownerId);
            activity.startActivity(intent);
        }
    };

    class ImageRequester implements IContentRequester, Target {
        private ImageView imageView;
        String id;

        public ImageRequester(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void contentChanged(Content content) {
            if (content == null)
                return;
            PictureIOSession io = ((PictureIOSession) content.getIOSession());

            final String imageUrl = io.getImageUrl(format);
            try {
                id = io.getId();
                if (io.getTemporaryThumbnail() != null) {
                    imageView.setImageBitmap(io.getTemporaryThumbnail());
                } else if (io.getTemporaryFullImage() != null) {
                    imageView.setImageBitmap(io.getTemporaryFullImage());
                } else {
                    if (imageView.getDrawable() != null) {
                        P1Application.picasso.load(Uri.parse(imageUrl))
                                .noFade().into(this);
                    } else {
                        if (imageUrl != null) {
                            P1Application.picasso.load(Uri.parse(imageUrl))
                                    .placeholder(null).into(imageView);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
            }
        }

        public void destory() {
            ContentHandler.getInstance().removeRequester(this);
            P1Application.picasso.cancelRequest(imageView);
            imageView.setImageDrawable(null);
        }

        @Override
        public void onError() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSuccess(Bitmap arg0, LoadedFrom arg1) {
            imageView.setImageBitmap(arg0);
        }
    };

    private void startSingleItemView(final Context c) {
        Intent intent = new Intent(c, FeedItemActivity.class);
        intent.putExtra("shareId", shareId);
        c.startActivity(intent);
    }

    private void startPictureView(String ownerId, String picID) {
        Intent intent = new Intent(Actions.USER_PICTURES);
        intent.putExtra("userId", ownerId);
        intent.putExtra("pictureId", picID);
        activity.startActivity(intent);
    }

    public static final int NORMAL = 1;
    public static final int UPLOADING = 2;
    public static final int FAILED = 3;
    private int state = NORMAL;
    private String venueName;

    private IContentRequester venueRequester = new IContentRequester() {

        @Override
        public void contentChanged(Content content) {
            VenueIOSession io = (VenueIOSession) content.getIOSession();
            try {
                if (!io.isValid()) {
                    return;
                }
                venueName = io.getName();
                if (venueName == null) {
                    venueName = "";
                }
                refreshTitle();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
            }
        }
    };

    @Override
    public void contentChanged(Content content) {
        if (content == null)
            return;
        if (content instanceof Share) {
            ShareIOSession io = ((ShareIOSession) content.getIOSession());
            try {
                if (!io.isValid())
                    return;
                if (io.hasFailedNetworkOperation())
                    state = FAILED;
                else if (io.isSending())
                    state = UPLOADING;
                else
                    state = NORMAL;
                if (io.getVenueId() != null)
                    venueRequester.contentChanged(ReadVenue.requestVenue(
                            io.getVenueId(), venueRequester));
                likeUserIds = io.getLikeUserIds();
                caption = io.getSafeCaption();
                hasLiked = io.hasLiked();
                List<String> tmp = io.getCommentIds();
                if (commentIds.size() != tmp.size() || !tmp.equals(commentIds)) {
                    commentsIsDirty = true;
                    commentIds.clear();
                    for (int i = tmp.size() - 1; i >= 0; i--) {
                        commentIds.add(tmp.get(i));
                    }
                }

                createdTime = io.getCreatedTime();
                totalComments = io.getTotalComments();
                ownerId = io.getOwnerId();
                tagIds = io.getTagIds();
                totalLikes = io.getTotalLikes();
                pictureIds = io.getPictureIds();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
                refreshView();
            }
        } else {
            // Must be Picture
            PictureIOSession io = ((PictureIOSession) content.getIOSession());
            try {
                if (!io.isValid())
                    return;

                likeUserIds = io.getLikeUserIds();
                caption = io.getCaption();
                hasLiked = io.hasLiked();
                List<String> tmp = io.getCommentIds();
                if (commentIds.size() != tmp.size() || !tmp.equals(commentIds)) {
                    commentsIsDirty = true;
                    commentIds.clear();
                    commentIds.addAll(tmp);
                    if (!isTrueShare()) {
                        Collections.reverse(commentIds);
                    }
                }
                createdTime = io.getCreatedTime();
                totalComments = io.getTotalComments();
                ownerId = io.getOwnerId();
                totalLikes = io.getTotalLikes();
                pictureIds.clear();
                pictureIds.add(multiPictureSpecifiedPicture);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
                refreshView();
            }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        Content likeableContent;
        if (isTrueShare()) {
            likeableContent = ContentHandler.getInstance().getShare(shareId,
                    null);
        } else {
            likeableContent = ContentHandler.getInstance().getPicture(
                    multiPictureSpecifiedPicture, null);
        }
        WriteLike.toggleLike(likeableContent);
    }

    public void refreshView() {

        if (isMultishare()) {
            format = Picture.ImageFormat.IMAGE_SQUARE_180;
        } else {
            int screenWidth = ScreenUtil.getScreenWidth(activity);
            if (screenWidth >= 720) {
                format = Picture.ImageFormat.IMAGE_WIDTH_720;
            } else {
                format = Picture.ImageFormat.IMAGE_WIDTH_480;
            }
        }

        if (ownerId != null && ownerId.length() > 0) {
            userHolder
                    .contentChanged(ReadUser.requestUser(ownerId, userHolder));
        }
        if (!isSingleViewMode) {
            view.setState(state);
        }
        String singleId = multiPictureSpecifiedPicture;
        if (singleId == null && pictureIds.size() == 1)
            singleId = pictureIds.get(0);
        if (singleId != null) {
            PictureIOSession io = ReadPicture.requestPicture(singleId, null)
                    .getIOSession();
            try {
                FeedImageView fiv = (FeedImageView) imageViews.get(0);
                fiv.setImageSize(io.getSize());
                ImageRequester r = (ImageRequester) fiv.getTag();
                r.contentChanged(ReadPicture.requestPicture(singleId, r));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
            }
        } else {
            for (int i = 0; i < pictureIds.size(); i++) {
                ImageRequester r = (ImageRequester) imageViews.get(
                        pictureIds.size() - i - 1).getTag();
                r.contentChanged(ReadPicture.requestPicture(pictureIds.get(i),
                        r));
            }
        }

        imageCountView.setVisibility(isMultishare() ? View.VISIBLE
                : View.INVISIBLE);
        imageCountView.setText(pictureIds.size() + " "
                + activity.getString(R.string.feed_grid_title));

        likeCheck.setText("" + totalLikes);
        likeCheck.setTextColor(view.getContext().getResources()
                .getColor(hasLiked ? R.color.like_red : R.color.like_grey));
        String commentDefinition = view.getContext().getString(
                (totalComments == 1 ? R.string._comment : R.string._comments));
        totalCommentsView.setText(totalComments + commentDefinition);
        likeCheck.setOnCheckedChangeListener(null);
        likeCheck.setChecked(hasLiked);
        likeCheck.setOnCheckedChangeListener(this);
        refreshTitle();
        if (createdTime != null) {
            timeStamp.setText(Utils.getTimeDifference(createdTime,
                    timeStamp.getContext()));
        }

        if (isSingleViewMode) {
            if (commentsIsDirty) {
                commentsIsDirty = false;
                refreshComments();
            }
            captionView.setMaxLines(Integer.MAX_VALUE);
        } else {
            captionView.setMaxLines(3);
        }
        addLikersView();
    }

    private void refreshTitle() {
        if (caption.isEmpty() && TextUtils.isEmpty(venueName)) {
            captionView.setVisibility(View.GONE);
        } else {
            captionView.setVisibility(View.VISIBLE);
            String htmlString = "";
            if (!caption.isEmpty()) {
                htmlString = caption + "&nbsp;&nbsp;";
            }
            if (!TextUtils.isEmpty(venueName)) {
                htmlString += activity.getString(R.string.at)
                        + "<font color=\"#74a4cd\">" + venueName + "</font>";
            }
            captionView.setText(Html.fromHtml(htmlString));
        }
    }

    private static final int[] IMAGE_VIEW_IDS = { R.id.feed_image_1,
            R.id.feed_image_2, R.id.feed_image_3, R.id.feed_image_4,
            R.id.feed_image_5, R.id.feed_image_6, R.id.feed_image_7,
            R.id.feed_image_8, R.id.feed_image_9 };

    private void addLikersView() {
        int count = likeUserIds.size();
        Utils.removePreviousRequesters(likersLayout);
        likersLayout.removeAllViews();
        for (int i = 0; (i < count && i < 5); i++) {
            View view = LayoutInflater.from(activity).inflate(
                    R.layout.feed_likers_image_item, likersLayout, false);
            likersLayout.addView(view);

            final ImageView imgView = (ImageView) view
                    .findViewById(R.id.feed_liker_image_item);
            SmallImageRequester imgreq = new SmallImageRequester(imgView);
            imgreq.contentChanged(ReadUser.requestUser(likeUserIds.get(i),
                    imgreq));
            imgView.setTag(imgreq);
        }
        if (count > 5) {
            View view = LayoutInflater.from(activity).inflate(
                    R.layout.feed_likers_image_item, likersLayout, false);
            ImageView img = ((ImageView) view
                    .findViewById(R.id.feed_liker_image_item));
            img.setImageResource(R.drawable.horizontal_options);
            img.setScaleType(ScaleType.CENTER_INSIDE);
            img.setBackgroundResource(R.drawable.liker_preview_bg_semi_transparent);
            likersLayout.addView(view);
        }
    }

    private class SmallImageRequester implements IContentRequester {
        private ImageView imageView;

        public SmallImageRequester(ImageView img) {
            this.imageView = img;
        }

        @Override
        public void contentChanged(Content content) {
            if (content == null)
                return;
            UserIOSession io = ((UserIOSession) content.getIOSession());
            final String imageUrl = io.getProfileThumb100Url();
            try {
                if (imageUrl != null) {
                    P1Application.picasso.load(Uri.parse(imageUrl)).noFade()
                            .placeholder(null).into(imageView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
            }
        }
    }

    private void refreshComments() {
        Utils.removePreviousRequesters(commentList);
        commentList.removeAllViews();
        if (commentIds.size() > 0) {
            view.findViewById(R.id.feed_text).setBackgroundResource(
                    R.color.off_white);
        } else {
            view.findViewById(R.id.feed_text).setBackgroundResource(
                    R.drawable.feed_item_bottom_background);
        }
        for (String commentId : commentIds) {
            CommentItemHolder commentItemHolder = new CommentItemHolder(
                    commentId, commentList);

        }
    }

    public void requestUpdatesForShare(String shareId) {
        this.shareId = shareId;
        contentChanged(ReadShare.requestShare(shareId, this));
    }

    public void requestUpdatesForPicture(String pictureId) {
        this.multiPictureSpecifiedPicture = pictureId;
        contentChanged(ReadPicture.requestPicture(pictureId, this));
    }

    @Override
    public void removeChildRequestors() {
        for (View v : imageViews) {
            ImageRequester imageRequester = (ImageRequester) v.getTag();
            imageRequester.destory();
        }
        state = NORMAL;
        view.setState(NORMAL);
        Utils.removePreviousRequesters(likersLayout);
        likersLayout.removeAllViews();
        ContentHandler.getInstance().removeRequester(venueRequester);
        ContentHandler.getInstance().removeRequester(userHolder);
    }

    public void reload() {
        WriteShare.retrySendingShare(ReadShare.requestShare(shareId, null));
    }

}
