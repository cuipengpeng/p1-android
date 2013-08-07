package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.util.Utils;

public class Comment extends Content{
    public static final String TAG = Comment.class.getSimpleName();
    
    public static final String TYPE = "comment";
    
    // API information variables
    private String value;
    private String ownerId;
    private IdTypePair parent;
    private List<String> tagIdList = new ArrayList<String>();
    private List<String> likeIdList = new ArrayList<String>();
    private int totalLikes;
    private boolean hasLiked;

    protected Comment(String id) {
        super(id);
        IOSession = new CommentIOSession();
        Log.d(TAG, "Comment " + id + " created");
    }

    @Override
    public CommentIOSession getIOSession() {
        return (CommentIOSession) super.getIOSession();
    }

    public class CommentIOSession extends ContentIOSession implements
            LikeableIOSession {
        
        @Override
        public String getType(){
            return TYPE;
        }
        
        public void replaceFakeId(String oldId, String newId) {
            if (getId().equals(oldId)) {
                setId(newId);
                ContentHandler.getInstance().changeCommentId(oldId, newId);
                Log.d(TAG, "Id replaced from " + oldId + " to " + newId);
            }
            Utils.checkAndReplaceId(likeIdList, oldId, newId);
            Utils.checkAndReplaceId(tagIdList, oldId, newId);
        }

        public String getValue() {
            return value;
        }
    
        public void setValue(String value) {
            Comment.this.value = value;
        }
    
        public String getOwnerId() {
            return ownerId;
        }
    
        public void setOwnerId(String ownerId) {
            Comment.this.ownerId = ownerId;
        }
    
        public IdTypePair getParent() {
            return parent;
        }
    
        public void setParent(IdTypePair parent) {
            Comment.this.parent = parent;
        }
    
        public List<String> getTagIdList() {
            return tagIdList;
        }
        
        public int getTotalLikes() {
            return totalLikes;
        }

        public void setTotalLikes(int totalLikes) {
            Comment.this.totalLikes = totalLikes;
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

        /**
         * Each like has the id of the User who created the like. There is no
         * need to fetch the Like object, just fetch the User object directly.
         * 
         * @return
         */
        public List<String> getLikeUserIds() {
            return likeIdList;
        }

        public boolean hasLiked() {
            return hasLiked;
        }

        public void setHasLiked(boolean hasLiked) {
            Comment.this.hasLiked = hasLiked;
        }

        public void incrementTotalLikes() {
            totalLikes++;
        }

        public void decrementTotalLikes() {
            totalLikes--;
        }
    }
    
}
