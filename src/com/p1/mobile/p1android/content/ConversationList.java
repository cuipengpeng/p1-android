package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * 
 * @author Anton
 *
 */
public class ConversationList extends Content{
    public static final String TAG = ConversationList.class.getSimpleName();
    public static final String TYPE = "conversation_list";
    
    public static final int PAGINATION_UNKNOWN = -1;
    
    public static final int INCOMPLETE_RESPONSES_ALLOWED = 2;
    
    // API information variables 
    private List<String> conversationIdList = new ArrayList<String>();
    
    
    private int paginationTotal = PAGINATION_UNKNOWN;
    private int paginationNextOffset = 0;
    private static final int paginationLimit = 100; // Can be anything

    private int notFullResponceCount = 0;

    protected ConversationList() {
        super("ConversationList");

        
        IOSession = new ConversationListIOSession();
        Log.d(TAG, "ConversationList created");
    }

    @Override
    public ConversationListIOSession getIOSession() {
        return (ConversationListIOSession) super.getIOSession();
    }

    public class ConversationListIOSession extends ContentIOSession {
        @Override
        public String getType(){
            return TYPE;
        }
    
        public List<String> getConversationIdList() {
            return conversationIdList;
        }
        
        public int getPaginationTotal() {
            return paginationTotal;
        }

        public void setPaginationTotal(int paginationTotal) {
            ConversationList.this.paginationTotal = paginationTotal;
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
        
        public void reportIncompleteNetworkResponse() {
            notFullResponceCount++;
        }

        public boolean hasMore() {
            return paginationNextOffset != paginationTotal
                    && notFullResponceCount < INCOMPLETE_RESPONSES_ALLOWED;
        }
        
    }

    

    
    
}
