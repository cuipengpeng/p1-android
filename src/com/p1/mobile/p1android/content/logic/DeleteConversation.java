package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.Conversation.ConversationIOSession;

public class DeleteConversation {
    public static final String TAG = DeleteConversation.class.getSimpleName();

    public static void deleteConversation(Conversation conversation) {
        ConversationIOSession io = conversation.getIOSession();
        try {
            io.setValid(false);
        } finally {
            io.close();
        }
        conversation.notifyListeners();
        Log.w(TAG, "Delete is not yet fully implemented");
    }

    public static void reportComment(Conversation conversation) {
        Log.w(TAG, "Reporting is not yet fully implemented");
    }

}
