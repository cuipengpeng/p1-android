package com.p1.mobile.p1android.content.logic;

import java.util.Date;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.Conversation.ConversationIOSession;
import com.p1.mobile.p1android.content.ConversationList;
import com.p1.mobile.p1android.content.ConversationList.ConversationListIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.parsing.ConversationListParser;
import com.p1.mobile.p1android.content.parsing.ConversationParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadConversation {
    public static final String TAG = ReadConversation.class.getSimpleName();

    /**
     * Currently returns the best memory representation available.
     * 
     * Does not automatically start filling the conversation as the messages are
     * not needed for all readers.
     * 
     */
    public static Conversation requestConversation(String id,
            IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        Conversation conversation = ContentHandler.getInstance()
                .getConversation(id, requester);

        return conversation;

    }

    /**
     * Fetches more messages for the given conversation
     * 
     * @param conversation
     */
    public static void fillConversation(final Conversation conversation) {
        boolean noActiveRequest;
        ConversationIOSession io = conversation.getIOSession();
        try {
            if (io.getUnfinishedUserModifications() > 0 || !io.hasMore()) {
                return;
            }
            noActiveRequest = io.getLastAPIRequest() == 0;
            if (noActiveRequest) {
                io.refreshLastAPIRequest();
                io.setHasFailedNetworkOperation(false);
            }
        } finally {
            io.close();
        }

        if (noActiveRequest) {
            ContentHandler.getInstance().getNetworkHandler()
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            String conversationId = null;
                            int paginationLimit;
                            Date oldestMessageTime;
                            ConversationIOSession io = conversation
                                    .getIOSession();
                            try {
                                conversationId = io.getId();
                                oldestMessageTime = io.getOldestMessageTime();
                                paginationLimit = io.getPaginationLimit();
                            } finally {
                                io.close();
                            }
                            String conversationsRequest = ReadContentUtil.netFactory
                                    .createGetConversationMessagesRequest(
                                            conversationId, oldestMessageTime,
                                            paginationLimit);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        conversationsRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");
                                
                                JsonArray messageArray = data
                                        .getAsJsonArray("messages");
                                ReadContentUtil.saveExtraMessages(messageArray);
                                ConversationParser
                                        .appendToConversation(object,
                                                conversation);

                                Log.d(TAG,
                                        "All listeners notified as result of requestConversationList");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        "Failed getting conversation list", e);
                                io = conversation.getIOSession();
                                try {
                                    io.setHasFailedNetworkOperation(true);
                                } finally {
                                    io.close();
                                }
                            } finally {
                                io = conversation
                                        .getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                                conversation.notifyListeners();
                            }
                        }
                    });
        }

    }

    /**
     * 
     * @param requester
     */
    public static ConversationList requestConversationList(
            IContentRequester requester) {
        ConversationList conversationList = ContentHandler.getInstance()
                .getConversationList(requester);

        ConversationListIOSession io = conversationList.getIOSession();
        try {
            if (!io.isValid()) {
                fillConversationList();
            }
        } finally {
            io.close();
        }

        return conversationList;
    }

    /**
     * Will retrieve more information for ConversationList and notify all
     * IContentRequesters of any successful changes. It is safe to call this
     * method quickly a large number of times.
     * 
     * Clears the old list
     * 
     * @param conversationList
     */
    public static void fillConversationList() {
        final ConversationList conversationList = ContentHandler.getInstance()
                .getConversationList(null);

        boolean noActiveRequest;
        ConversationListIOSession io = conversationList.getIOSession();
        try {
            noActiveRequest = io.getLastAPIRequest() == 0;
            if (noActiveRequest) {
                io.refreshLastAPIRequest();
            }
        } finally {
            io.close();
        }

        if (noActiveRequest) {
            ContentHandler.getInstance().getNetworkHandler()
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            int paginationOffset, paginationLimit;
                            ConversationListIOSession io = conversationList
                                    .getIOSession();
                            try {
                                paginationOffset = io.getPaginationNextOffset();
                                paginationLimit = io.getPaginationLimit();
                            } finally {
                                io.close();
                            }
                            String conversationsRequest = ReadContentUtil.netFactory
                                    .createGetConversationsRequest(
                                            paginationOffset, paginationLimit);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        conversationsRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");
                                JsonArray userArray = data
                                        .getAsJsonArray("users");
                                ReadContentUtil.saveExtraUsers(userArray);
                                JsonArray messageArray = data
                                        .getAsJsonArray("messages");
                                ReadContentUtil.saveExtraMessages(messageArray);
                                JsonArray conversationArray = data
                                        .getAsJsonArray("conversations");
                                ReadContentUtil
                                        .saveExtraConversations(conversationArray);

                                ConversationListParser
                                        .appendToConversationList(object,
                                                conversationList);

                                conversationList.notifyListeners();

                                Log.d(TAG,
                                        "All listeners notified as result of requestConversationList");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        "Failed getting conversation list", e);
                            }
                        }
                    });
        }
    }

}
