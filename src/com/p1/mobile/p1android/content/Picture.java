package com.p1.mobile.p1android.content;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.p1.mobile.p1android.content.Comment.CommentIOSession;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadComment;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.util.Utils;

public class Picture extends Content {
    public static final String TAG = Picture.class.getSimpleName();
    public static final String TYPE = "picture";
    
    public static enum ImageFormat{IMAGE_SQUARE_314, IMAGE_SQUARE_180, IMAGE_SQUARE_154, IMAGE_980x145, IMAGE_WIDTH_720, IMAGE_WIDTH_640, IMAGE_WIDTH_480};

    public static ImageFormat[] supportedImageFormats = {
            ImageFormat.IMAGE_SQUARE_180, ImageFormat.IMAGE_SQUARE_154,
            ImageFormat.IMAGE_WIDTH_720, ImageFormat.IMAGE_WIDTH_480 };

    /** Used for Pictures created by the user */
    private String internalImageUri = null;

    private String ownerId;
    private String caption = "";
    private Point size;
    private List<String> comments = new ArrayList<String>();
    private List<String> likes = new ArrayList<String>();
    private int totalLikes;
    private int totalComments;
    private boolean hasLiked;
    private List<String> tags = new ArrayList<String>();

    private Bitmap temporaryThumbnail;
    private Bitmap temporaryFullImage;

    private String[] imageUrls = new String[ImageFormat.values().length];
    

    protected Picture(String id) {
        super(id);
        IOSession = new PictureIOSession();
        Log.d(TAG, "Picture " + id + " created");
    }
    
    

    @Override
    public PictureIOSession getIOSession() {
        return (PictureIOSession) super.getIOSession();
    }

    public class PictureIOSession extends ContentIOSession implements
            LikeableIOSession, CommentableIOSession {

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public void replaceFakeId(String oldId, String newId) {
            if (getId().equals(oldId)) {
                setId(newId);
                ContentHandler.getInstance().changePictureId(oldId, newId);
                Log.d(TAG, "Id replaced from " + oldId + " to " + newId);
            }
            Utils.checkAndReplaceId(comments, oldId, newId);
            Utils.checkAndReplaceId(likes, oldId, newId);
            Utils.checkAndReplaceId(tags, oldId, newId);
            Log.d(TAG, "Replace fake id " + oldId + " to " + newId);
        }

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            Picture.this.ownerId = ownerId;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            Picture.this.caption = caption;
        }

        public Point getSize() {
            return size;
        }

        /**
         * @return the aspect ratio of the original image.
         */
        public float getAspectRatio() {
            if (size == null) {
                Log.e(TAG, "Size for aspect ratio has not been set!");
                return 1f;
            }
            return size.y / (float) size.x;
        }

        public void setSize(Point size) {
            Picture.this.size = size;
        }

        public List<String> getCommentIds() {
            return comments;
        }

        /**
         * Each like has the id of the User who created the like. There is no
         * need to fetch the Like object, just fetch the User object directly.
         * 
         * @return
         */
        public List<String> getLikeUserIds() {
            return likes;
        }
        
        public boolean hasLiked() {
            return hasLiked;
        }

        public void setHasLiked(boolean hasLiked) {
            Picture.this.hasLiked = hasLiked;
        }

        public List<String> getTags() {
            return tags;
        }
        
        public String getImageUrl(ImageFormat id){
            if (!supportsImageFormat(id)) {
                throw new InvalidParameterException(
                        "Picture does not support format " + id.name());
            }
            if (imageUrls[id.ordinal()] != null)
                return imageUrls[id.ordinal()];
            Log.w(TAG, "Image url of format " + id.toString()
                    + " not set, returning full internal image instead");
            return getInternalImageUri();
        }
        public void setImageUrl(ImageFormat id, String url){
            if (supportsImageFormat(id)) {
                imageUrls[id.ordinal()] = url;
                if (id.equals(ImageFormat.IMAGE_SQUARE_180)) {
                    temporaryThumbnail = null;
                }
                if (id.equals(ImageFormat.IMAGE_WIDTH_720)) {
                    temporaryFullImage = null;
                }
            }

        }

        public int getTotalLikes() {
            return totalLikes;
        }

        public void setTotalLikes(int totalLikes) {
            Picture.this.totalLikes = totalLikes;
        }

        public int getTotalComments() {
            return totalComments;
        }

        public void setTotalComments(int totalComments) {
            Picture.this.totalComments = totalComments;
        }

        public String getInternalImageUri() {
            return internalImageUri;
        }

        public void setInternalImageUri(String internalImageUri) {
            Picture.this.internalImageUri = internalImageUri;
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

        /**
         * Is only set while a multipictureshare has not sent it's information
         * 
         * @return a 180x180 thumbnail or null
         */
        public Bitmap getTemporaryThumbnail() {
            return temporaryThumbnail;
        }

        public void setTemporaryThumbnail(Bitmap thumbnail) {
            Picture.this.temporaryThumbnail = thumbnail;
        }

        /**
         * Is only set while a singlePictureShare has not sent it's information
         * 
         * @return a large image with correct aspect ratio or null
         */
        public Bitmap getTemporaryFullImage() {
            return temporaryFullImage;
        }

        public void setTemporaryFullImage(Bitmap temporaryFullImage) {
            Picture.this.temporaryFullImage = temporaryFullImage;
        }
    }

    public static boolean supportsImageFormat(ImageFormat format) {
        for (ImageFormat f : supportedImageFormats) {
            if (f.equals(format)) {
                return true;
            }
        }

        return false;
    }

}
