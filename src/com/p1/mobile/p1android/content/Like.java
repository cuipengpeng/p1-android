package com.p1.mobile.p1android.content;

import android.util.Log;

/**
 * 
 * @author Anton
 * 
 *         Each Like has the id of the user that created it, and the parent
 *         object is always available through other means. This makes the Like
 *         object redundant.
 * 
 *         Request the User object directly instead of going through Like.
 */
@Deprecated
public class Like extends Content{
    public static final String TAG = Like.class.getSimpleName();
    
    public static final String TYPE = "like";

    // API information variables:
    private IdTypePair parent;
    
    
    
    protected Like(String id) {
        super(id);
        IOSession = new LikeIOSession();
        Log.d(TAG, "Like " + id + " created");
    }

    @Override
    public LikeIOSession getIOSession() {
        return (LikeIOSession) super.getIOSession();
    }

    public class LikeIOSession extends ContentIOSession {
        
        @Override
        public String getType(){
            return TYPE;
        }
        
        public String getOwnerId() {
            return getId();
        }

        public IdTypePair getParent() {
            return parent;
        }

        public void setParent(IdTypePair parent) {
            Like.this.parent = parent;
        }
    }

    

}
