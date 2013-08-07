package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class NotificationStory extends Content {
    public static final String TYPE = "story";

    /**
     * StoryType defines what happened. General rule of thumb is that it's the
     * object type that was created. Example: StoryType:COMMENT is sent if
     * someone commented on something.
     * 
     */
    public enum StoryType {
        LIKE, COMMENT, TAG, RELATIONSHIP;

        public static StoryType getEnum(String value) {
            if (value == null)
                throw new IllegalArgumentException();
            for (StoryType v : values())
                if (value.equalsIgnoreCase(v.name()))
                    return v;
            Log.e(NotificationStory.TAG, "Unable to map " + value
                    + " to StoryType enum");
            throw new IllegalArgumentException();
        }
    };

    /**
     * Relevance defines why you are getting this notification. Example:
     * Relevance:TAGGED is sent if someone likes a photo you're tagged in.
     */
    public enum Relevance {
        OWNER, TAGGED, MENTIONED, COMMENTED, FOLLOWED;

        public static Relevance getEnum(String value) {
            if (value == null)
                throw new IllegalArgumentException();
            for (Relevance v : values())
                if (value.equalsIgnoreCase(v.name()))
                    return v;
            Log.e(TAG, "Unable to map " + value + " to Relevance enum");
            throw new IllegalArgumentException();
        }
    };

    /**
     * Reference to a new Like, Comment, Message etc.
     */
    private IdTypePair createdObject;

    /**
     * A like on a comment will have a Comment as a linkedObject
     */
    private IdTypePair linkedObject;

    /**
     * Reference to the important object of the interaction. Clicking the
     * notification should lead to a view displaying this object.
     */
    private IdTypePair topLevelObject;

    private Relevance relevance;
    private StoryType storyType;
    private boolean read = false;

    /**
     * Kept for future compitability with the API. Will mostly only contain the
     * user who made an action creating the notification.
     */
    private List<String> relatedUserIds = new ArrayList<String>();

    protected NotificationStory(String id) {
        super(id);
        IOSession = new NotificationIOSession();
        Log.d(TAG, "Tag " + id + " created");
    }

    @Override
    public String toString() {
        return "NotificationStory [createdObject=" + createdObject
                + ", linkedObject=" + linkedObject + ", topLevelObject="
                + topLevelObject + ", relevance=" + relevance + ", storyType="
                + storyType + ", read=" + read + ", relatedUserIds="
                + relatedUserIds + "]";
    }

    @Override
    public NotificationIOSession getIOSession() {
        return (NotificationIOSession) super.getIOSession();
    }

    public class NotificationIOSession extends ContentIOSession {

        @Override
        public String getType() {
            return TYPE;
        }

        /**
         * Reference to a new Like, Comment, Message etc.
         */
        public IdTypePair getCreatedObject() {
            return createdObject;
        }

        public void setCreatedObject(IdTypePair createdObject) {
            NotificationStory.this.createdObject = createdObject;
        }

        /**
         * A like on a comment will have a Comment as a linkedObject
         */
        public IdTypePair getLinkedObject() {
            return linkedObject;
        }

        public void setLinkedObject(IdTypePair linkedObject) {
            NotificationStory.this.linkedObject = linkedObject;
        }

        /**
         * Reference to the important object of the interaction. Clicking the
         * notification should lead to a view displaying this object.
         */
        public IdTypePair getTopLevelObject() {
            return topLevelObject;
        }

        public void setTopLevelObject(IdTypePair topLevelObject) {
            NotificationStory.this.topLevelObject = topLevelObject;
        }

        public Relevance getRelevance() {
            return relevance;
        }

        public void setRelevance(Relevance relevance) {
            NotificationStory.this.relevance = relevance;
        }

        public StoryType getStoryType() {
            return storyType;
        }

        public void setStoryType(StoryType storyType) {
            NotificationStory.this.storyType = storyType;
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            NotificationStory.this.read = read;
        }

        public List<String> getRelatedUserIds() {
            return relatedUserIds;
        }

        /**
         * @return id of the user who did something that triggered the
         *         notification
         */
        public String getSourceUserId() {
            return relatedUserIds.get(0);
        }

    }
}
