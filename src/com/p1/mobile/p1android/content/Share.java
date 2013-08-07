package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.p1.mobile.p1android.content.Comment.CommentIOSession;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadComment;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.util.Utils;

/**
 * Shares behave slighly different if they only have a single picture. If it is
 * a single picture share, it reroutes all likes and comments to the picture
 * object.
 * 
 * @author Viktor Nyblom
 * 
 */
public class Share extends Content {
    public static final String TAG = Share.class.getSimpleName();
    private static final String TYPE = "share";

    private List<String> pictures = new ArrayList<String>();
    private List<String> comments = new ArrayList<String>();
    private List<String> likes = new ArrayList<String>();
    private List<String> tags = new ArrayList<String>();

    private int totalLikes;
    private int totalComments;

    private String ownerId;

    private String caption = "";

    private boolean hasLiked;

    private String venueId;

    @Override
    public void notifyListeners() {
        super.notifyListeners();
        if (singlePicture != null) {
            singlePicture.notifyListeners();
        }
    }

    /**
     * Non-null if it is a singlePictureShare
     */
    private Picture singlePicture = null;

    protected Share(String id) {
        super(id);
        IOSession = new ShareIOSession();
        Log.d(TAG, "Share " + id + " created");
    }

    @Override
    public ShareIOSession getIOSession() {
        return (ShareIOSession) super.getIOSession();

    }

    public class ShareIOSession extends ContentIOSession implements
            LikeableIOSession, CommentableIOSession {

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public void replaceFakeId(String oldId, String newId) {
            if (getId().equals(oldId)) {
                setId(newId);
                ContentHandler.getInstance().changeShareId(oldId, newId);
                Log.d(TAG, "Id replaced from " + oldId + " to " + newId);
            }
            Utils.checkAndReplaceId(comments, oldId, newId);
            Utils.checkAndReplaceId(likes, oldId, newId);
            Utils.checkAndReplaceId(tags, oldId, newId);
            Log.d(TAG, "Replace fake id " + oldId + " to " + newId);
        }

        public List<String> getPictureIds() {
            return pictures;
        }

        public List<String> getCommentIds() {
            if (isSinglePictureShare()) {
                PictureIOSession io = getSinglePicture().getIOSession();
                try {
                    return io.getCommentIds();
                } finally {
                    io.close();
                }
            }
            return comments;
        }

        public boolean isSinglePictureShare() {
            return pictures.size() == 1;
        }

        /**
         * Each like has the id of the User who created the like. There is no
         * need to fetch the Like object, just fetch the User object directly.
         * 
         * @return
         */
        public List<String> getLikeUserIds() {
            if (isSinglePictureShare()) {
                PictureIOSession io = getSinglePicture().getIOSession();
                try {
                    return io.getLikeUserIds();
                } finally {
                    io.close();
                }
            }
            return likes;
        }

        public List<String> getTagIds() {
            return tags;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            Share.this.ownerId = ownerId;
        }

        /**
         * For singlePictureShares, this returns the pictures caption
         * 
         * @return
         */
        public String getSafeCaption() {
            if (isSinglePictureShare()) {
                PictureIOSession io = getSinglePicture().getIOSession();
                try {
                    return io.getCaption();
                } finally {
                    io.close();
                }
            }
            return caption;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            Share.this.caption = caption;
        }

        public boolean hasLiked() {
            if (isSinglePictureShare()) {
                PictureIOSession io = getSinglePicture().getIOSession();
                try {
                    return io.hasLiked();
                } finally {
                    io.close();
                }
            }
            return hasLiked;
        }

        public void setHasLiked(boolean hasLiked) {
            if (isSinglePictureShare()) {
                PictureIOSession io = getSinglePicture().getIOSession();
                try {
                    io.setHasLiked(hasLiked);
                    return;
                } finally {
                    io.close();
                }
            }
            Share.this.hasLiked = hasLiked;
        }

        public int getTotalLikes() {
            if (isSinglePictureShare()) {
                PictureIOSession io = getSinglePicture().getIOSession();
                try {
                    return io.getTotalLikes();
                } finally {
                    io.close();
                }
            }
            return totalLikes;
        }

        public void setTotalLikes(int totalLikes) {
            Share.this.totalLikes = totalLikes;
        }

        public int getTotalComments() {
            if (isSinglePictureShare()) {
                PictureIOSession io = getSinglePicture().getIOSession();
                try {
                    return io.getTotalComments();
                } finally {
                    io.close();
                }
            }
            return totalComments;
        }

        public void setTotalComments(int totalComments) {
            Share.this.totalComments = totalComments;
        }

        public int getTotalTags() {
            return tags.size();
        }

        /**
         * 
         * @return the picture object if this is a single picture share
         */
        public Picture getSinglePicture() {
            if (isSinglePictureShare() && singlePicture == null) {
                singlePicture = ContentHandler.getInstance().getPicture(
                        pictures.get(0), null);
            }
            return singlePicture;

        }

        /**
         * A share is valid when it has at least 1 picture
         */
        public void updateValidity() {
            // setValid((pictures.size() > 0 || (caption != null && !caption
            // .equals(""))) && ownerId != null);
            setValid(pictures.size() > 0 && ownerId != null);
        }

        public String getVenueId() {
            return venueId;
        }

        public void setVenueId(String venueId) {
            Share.this.venueId = venueId;
        }

        public boolean hasMoreLikes() {
            if (getTotalLikes() > getLikeUserIds().size())
                return true;
            if (getTotalLikes() <= 0)
                return false;
            User lastLiker = ReadUser.requestUser(
                    getLikeUserIds().get(getLikeUserIds().size() - 1), null);
            UserIOSession io = lastLiker.getIOSession();
            try {

                if (!io.isValid())
                    return true;
            } finally {
                io.close();
            }
            return false;
        }

        public boolean hasMoreComments() {
            if (getTotalComments() > getCommentIds().size())
                return true;
            if (getTotalComments() <= 0)
                return false;
            Comment lastComment = ReadComment.requestComment(getCommentIds()
                    .get(getCommentIds().size() - 1), null);
            CommentIOSession io = lastComment.getIOSession();
            try {

                if (!io.isValid())
                    return true;
            } finally {
                io.close();
            }
            return false;
        }

        public void incrementTotalComments() {
            totalComments++;
        }

        public void incrementTotalLikes() {
            totalLikes++;
        }

        public void decrementTotalLikes() {
            totalLikes--;
        }

        public boolean isSending() {
            return getEtag() == null && !hasFailedNetworkOperation();
        }

    }
}
