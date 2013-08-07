package com.p1.mobile.p1android.content;

import android.util.Log;

public class Relationship extends Content {
    public static final String TYPE = "relationship";
    
    private boolean following;
    private String ownerID;
    private String otherUserId;
    
    
    protected Relationship(String id) {
        super(id);
        IOSession = new RelationshipIOSession();
        Log.d(TAG, "Relationship " + id + " created");
    }

    @Override
    public RelationshipIOSession getIOSession() {
        return (RelationshipIOSession) super.getIOSession();
    }

    public class RelationshipIOSession extends ContentIOSession {

        @Override
        public String getType() {
            return TYPE;
        }

        public boolean isFollowing() {
            return following;
        }

        public void setFollowing(boolean following) {
            Relationship.this.following = following;
        }

        public String getOwnerID() {
            return ownerID;
        }

        public void setOwnerID(String ownerID) {
            Relationship.this.ownerID = ownerID;
        }

        public String getOtherUserId() {
            return otherUserId;
        }

        public void setOtherUserId(String otherUserId) {
            Relationship.this.otherUserId = otherUserId;
        }
    }

}
