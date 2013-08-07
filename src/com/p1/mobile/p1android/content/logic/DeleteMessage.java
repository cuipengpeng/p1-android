package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.p1.mobile.p1android.content.Message;
import com.p1.mobile.p1android.content.Message.MessageIOSession;

public class DeleteMessage {
    public static final String TAG = DeleteMessage.class.getSimpleName();

    public static void deleteMessage(Message message) {
        MessageIOSession io = message.getIOSession();
        try {
            io.setValid(false);
        } finally {
            io.close();
        }
        message.notifyListeners();
        Log.w(TAG, "Delete is not yet fully implemented");
    }

    public static void reportMessage(Message message) {
        Log.w(TAG, "Reporting is not yet fully implemented");
    }

}
