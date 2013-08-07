package com.p1.mobile.p1android.content;

import android.util.Log;

public class Message extends Content {
    public static final String TAG = Message.class.getSimpleName();
    public static final String TYPE = "message";

    private String ownerId;
    private String value;

    protected Message(String id) {
        super(id);
        IOSession = new MessageIOSession();
        Log.d(TAG, "Tag " + id + " created");
    }

    @Override
    public MessageIOSession getIOSession() {
        return (MessageIOSession) super.getIOSession();
    }

    public class MessageIOSession extends ContentIOSession {
        
        @Override
        public String getType(){
            return TYPE;
        }

        @Override
        public void replaceFakeId(String oldId, String newId) {
            if (getId().equals(oldId)) {
                setId(newId);
                ContentHandler.getInstance().changeMessageId(oldId, newId);
                Log.d(TAG, "Id replaced from " + oldId + " to " + newId);
            }
            if (ownerId.equals(oldId)) {
                ownerId = newId;
            }
        }

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            Message.this.ownerId = ownerId;
        }

        /**
         * @return the actual message
         */
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            Message.this.value = value;
        }

        public boolean isSending() {
            return !hasFailedNetworkOperation() && getEtag() == null;
        }
    }

}
