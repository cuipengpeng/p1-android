package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * 
 * @author Anton
 *
 */
public class NotificationList extends Content{
    public static final String TAG = NotificationList.class.getSimpleName();
    public static final String TYPE = "notification_list";
    
    public static final int PAGINATION_UNKNOWN = -1;
    
    
    // API information variables 
    private List<String> notificationIdList = new ArrayList<String>();
    
    
    private int paginationTotal = PAGINATION_UNKNOWN;
    private int paginationNextOffset = 0;
    private static final int paginationLimit = 50; // Can be anything

    protected NotificationList() {
        super("NotificationList");

        
        IOSession = new NotificationListIOSession();
        Log.d(TAG, "NotificationList created");
    }

    @Override
    public NotificationListIOSession getIOSession() {
        return (NotificationListIOSession) super.getIOSession();
    }

    public class NotificationListIOSession extends ContentIOSession {
        @Override
        public String getType(){
            return TYPE;
        }
    
        public List<String> getNotificationIdList() {
            return notificationIdList;
        }
        
        public String getNewestNotificationId() {
            if (notificationIdList.isEmpty()) {
                return null;
            }
            return notificationIdList.get(0);
        }

        public int getPaginationTotal() {
            return paginationTotal;
        }

        public void setPaginationTotal(int paginationTotal) {
            NotificationList.this.paginationTotal = paginationTotal;
        }

        public int getPaginationNextOffset() {
            return paginationNextOffset;
        }

        public void incrementOffset() {
            paginationNextOffset++;
        }

        public void resetPagination() {
            paginationNextOffset = 0;
        }

        public int getPaginationLimit() {
            return paginationLimit;
        }
        
        public boolean hasMore(){
            return paginationNextOffset != paginationTotal;
        }
        
    }

    

    
    
}
