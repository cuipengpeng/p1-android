package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * 
 * @author Anton
 *
 * Used for "followers" and "following"
 * Should have the same Id as the User it represents
 */
public class FollowList extends Content{
    public static final String TAG = FollowList.class.getSimpleName();
    public static final String FOLLOWING = "following";
    public static final String FOLLOWERS = "followers";
    
    public static final int PAGINATION_UNKNOWN = -1;
    
    
    // API information variables 
    private String type;
    private List<String> userIdList = new ArrayList<String>();
    
    
    private int paginationTotal = PAGINATION_UNKNOWN;
    private int paginationNextOffset = 0;
    private static final int paginationLimit = 50; // Can be anything

    protected FollowList(String id, String type) {
        super(id);
        if(type.equals(FOLLOWING) || type.equals(FOLLOWERS)){
            this.type = type;
        } else{
            Log.e(TAG, "Invalid type");
        }
        
        IOSession = new FollowListIOSession();
        Log.d(TAG, "FollowList " + id + " created");
    }

    @Override
    public FollowListIOSession getIOSession() {
        return (FollowListIOSession) super.getIOSession();
    }

    public class FollowListIOSession extends ContentIOSession {
        @Override
        public String getType(){
            return type;
        }
    
        public List<String> getUserIdList() {
            return userIdList;
        }
        
        public boolean isFollowing(String userId){
            if(getType().equals(FOLLOWERS)){
                throw new IllegalStateException(
                        "Check for if user X is following someone is not applicable for the followers list");
            }
            return userIdList.contains(userId);
        }

        public void addUser(String userId) {
            userIdList.add(userId);
            paginationTotal++;
            paginationNextOffset++;
        }

        public void removeUser(String userId) {
            userIdList.remove(userId);
            paginationTotal--;
            paginationNextOffset--;
        }

        public int getPaginationTotal() {
            return paginationTotal;
        }

        public void setPaginationTotal(int paginationTotal) {
            FollowList.this.paginationTotal = paginationTotal;
        }

        public int getPaginationNextOffset() {
            return paginationNextOffset;
        }

        public void incrementOffset() {
            paginationNextOffset++;
        }

        public int getPaginationLimit() {
            return paginationLimit;
        }
        
        public boolean hasMore(){
            return paginationNextOffset != paginationTotal;
        }
        
    }

    

    
    
}
