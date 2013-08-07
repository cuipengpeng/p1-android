package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.p1.mobile.p1android.content.Message.MessageIOSession;
import com.p1.mobile.p1android.content.logic.ReadMessage;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.util.Utils;

public class Conversation extends Content {
    public static final String TAG = Conversation.class.getSimpleName();

    public static final String TYPE = "conversation";
    public static final int PAGINATION_UNKNOWN = -1;

    public static final int INCOMPLETE_RESPONSES_ALLOWED = 1;

    // API information variables
    private Date latestTime;
    private Date oldestMessageTime = new Date();
    private String ownerId;
    private List<String> messageIdList = new ArrayList<String>();
    private String newestMessageId;
    private boolean read = true;

    private static final int paginationLimit = 30; // Can be anything

    private int notFullResponceCount = 0;

    protected Conversation(String id) {
        super(id);
        ownerId = NetworkUtilities.getLoggedInUserId();
        IOSession = new ConversationIOSession();
        if (ownerId != null) {
            IOSession.setValid(true); // Noone can access the IOSession yet, so
                                      // it's safe to bypass the lock.
        }
        Log.d(TAG, "Conversation " + id + " created");
    }

    @Override
    public ConversationIOSession getIOSession() {
        return (ConversationIOSession) super.getIOSession();
    }

    public class ConversationIOSession extends ContentIOSession {

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public void replaceFakeId(String oldId, String newId) {
            if (getId().equals(oldId)) {
                Log.e(TAG, "Can not change id of conversations");
            }
            if (newestMessageId != null && newestMessageId.equals(oldId)) {
                newestMessageId = newId;
                Log.d(TAG, "newest id replaced from " + oldId + " to " + newId);
            }
            Utils.checkAndReplaceId(messageIdList, oldId, newId);
        }

        public Date getLatestTime() {
            return latestTime;
        }

        public void setLatestTime(Date latestTime) {
            Conversation.this.latestTime = latestTime;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            Conversation.this.ownerId = ownerId;
        }

        public String getOtherUserId() {
            return getId();
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            Conversation.this.read = read;
        }

        /**
         * 
         * @return an Unordered list of messages
         */
        public List<String> getMessageIdList() {
            return messageIdList;
        }

        public String getNewestMessageId() {
            Log.d(TAG, "Newest message of " + getId() + " retrieved as "
                    + newestMessageId);
            return newestMessageId;
        }

        public void setNewestMessageId(String newId) {
            Log.d(TAG, "Newest message of " + getId() + " set to "
 + newId);
            newestMessageId = newId;
        }

        /**
         * Maintains unique ids
         * 
         * @param messageId
         * @return true if inserted successfully. False if the id is already
         *         contained in the conversation
         */
        public boolean safelyAddMessageId(String messageId) {
            if (messageIdList.contains(messageId)) {
                return false;
            }
            messageIdList.add(messageId);
            Message newMessage = ReadMessage.requestMessage(messageId, null);
            MessageIOSession io = newMessage.getIOSession();
            try {
                if (io.getCreatedTime() != null
                        && io.getCreatedTime().before(oldestMessageTime)) {
                    oldestMessageTime = io.getCreatedTime();
                }
            } finally {
                io.close();
            }
            return true;
        }

        public int getPaginationLimit() {
            return paginationLimit;
        }

        public void reportIncompleteNetworkResponse() {
            notFullResponceCount++;
        }

        public boolean hasMore() {
            return notFullResponceCount < INCOMPLETE_RESPONSES_ALLOWED;
        }

        public Date getOldestMessageTime() {
            return oldestMessageTime;
        }
    }
}
