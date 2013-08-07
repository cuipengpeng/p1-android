package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.Conversation.ConversationIOSession;
import com.p1.mobile.p1android.content.parsing.JsonFactory;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 *         All results of writing to content is seen in ContentChanged.
 */
public class WriteConversation {
    public static final String TAG = WriteConversation.class.getSimpleName();

    /**
     * If the conversation already is read, this method does nothing.
     * 
     * @param conversation
     */
    public static void markAsRead(Conversation conversation) {
        ConversationIOSession conversationIO = conversation.getIOSession();
        Account account = ContentHandler.getInstance()
                .getAccount(null);
        AccountIOSession accountIO = account.getIOSession();
        try {
            if (conversationIO.isRead()) {
                return;
            }
            conversationIO.setRead(true);
            accountIO.decrementUnreadMessages();
        } finally {
            conversationIO.close();
            accountIO.close();
        }

        conversation.notifyListeners();
        account.notifyListeners();

        sendMarkRead(conversation);

    }

    private static void sendMarkRead(final Conversation readConversation) {

        ContentHandler.getInstance().getLowPriorityNetworkHandler()
                .post(new Runnable() {
            @Override
            public void run() {
                String conversationRequest;
                ConversationIOSession io = readConversation
                        .getIOSession();
                String newestMessageId;
                try {
                    conversationRequest = ReadContentUtil.netFactory
                            .createConversationRequest(io.getId());
                    newestMessageId = io.getNewestMessageId();
                } finally {
                    io.close();
                }

                try {
                    Network network = NetworkUtilities.getNetwork();

                    network.makePatchRequest(conversationRequest, null,
                            JsonFactory.createReadJson(newestMessageId));

                    Log.d(TAG, "Mark read successful");
                } catch (Exception e) {
                    Log.e(TAG, "Failed marking read", e);
                }
            }
        });
    }

}
