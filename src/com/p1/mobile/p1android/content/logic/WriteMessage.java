package com.p1.mobile.p1android.content.logic;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.Conversation.ConversationIOSession;
import com.p1.mobile.p1android.content.ConversationList;
import com.p1.mobile.p1android.content.ConversationList.ConversationListIOSession;
import com.p1.mobile.p1android.content.Message;
import com.p1.mobile.p1android.content.Message.MessageIOSession;
import com.p1.mobile.p1android.content.background.RetryRunnable;
import com.p1.mobile.p1android.content.parsing.MessageParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 *         All results of writing to content is seen in ContentChanged.
 */
public class WriteMessage {
    public static final String TAG = WriteMessage.class.getSimpleName();

    /**
     * 
     * @param message
     * @param targetUserId
     *            same as conversationId
     */
    public static void sendMessage(String text, final String targetUserId) {
        final String fakeId = FakeIdGenerator.getNextFakeId();
        final Message message = ContentHandler.getInstance().getMessage(fakeId,
                null);
        ContentHandler.getInstance().getFakeIdTracker().track(fakeId, message);
        MessageIOSession io = message.getIOSession();
        try {
            io.setValue(text);
            io.setCreatedTime(new Date());
            io.setOwnerId(NetworkUtilities.getLoggedInUserId());
        } finally {
            io.close();
        }
        final Conversation conversation = ContentHandler.getInstance()
                .getConversation(targetUserId, null);
        ConversationIOSession conversationIO = conversation.getIOSession();
        try {
            conversationIO.getMessageIdList().add(fakeId);
            conversationIO.setNewestMessageId(fakeId);
            ContentHandler.getInstance().getFakeIdTracker()
                    .track(fakeId, conversation);
            conversationIO.setLatestTime(new Date());
            conversationIO.incrementUnfinishedUserModifications();
        } finally {
            conversationIO.close();
        }
        ConversationList conversationList = ContentHandler.getInstance()
                .getConversationList(null);
        ConversationListIOSession conversationListIO = conversationList
                .getIOSession();
        try {
            List<String> conversationIds = conversationListIO
                    .getConversationIdList();
            conversationIds.remove(targetUserId);
            conversationIds.add(0, targetUserId);

        } finally {
            conversationListIO.close();
        }

        conversation.notifyListeners();
        conversationList.notifyListeners();


        ContentHandler.getInstance().getNetworkHandler()
                .post(new RetryRunnable() {
            @Override
            public void run() {
                String messageRequest = ReadContentUtil.netFactory
                        .createPostMessageRequest(targetUserId);

                try {
                    Network network = NetworkUtilities.getNetwork();

                    JsonObject sentJson = MessageParser
                            .serializeMessage(message);
                    JsonObject object = network.makePostRequest(messageRequest,
                            null, sentJson).getAsJsonObject();

                    JsonObject data = object.getAsJsonObject("data");
                    JsonArray messagesArray = data.getAsJsonArray("messages");

                    Iterator<JsonElement> iterator = messagesArray.iterator();
                    if (iterator.hasNext()) { // Will save the
                                              // single returned
                                              // message
                        JsonObject messageJson = iterator.next().getAsJsonObject();
                        String newMessageId = messageJson.get("id").getAsString();

                        if (fakeId != newMessageId) {
                            ContentHandler.getInstance().getFakeIdTracker()
                                    .update(fakeId, newMessageId);
                        }

                        MessageParser.parseMessage(messageJson, message);
                    }

                    MessageIOSession messageIO = message.getIOSession();
                    ConversationIOSession conversationIO = conversation
                            .getIOSession();
                    try {
                        conversationIO.setLatestTime(messageIO.getCreatedTime());
                        conversationIO.decrementUnfinishedUserModifications();
                    } finally {
                        conversationIO.close();
                        messageIO.close();
                    }
                    message.notifyListeners();
                    conversation.notifyListeners();

                    Log.d(TAG, "All listeners notified as result post message");
                } catch (Exception e) {
                    Log.e(TAG, "Failed posting message", e);
                    retry();
                }
            }

            protected void failedLastRetry() {
                super.failedLastRetry();
                MessageIOSession messageIO = message.getIOSession();
                ConversationIOSession conversationIO = conversation
                        .getIOSession();
                try {
                    messageIO.setHasFailedNetworkOperation(true);
                    conversationIO.decrementUnfinishedUserModifications();
                } finally {
                    conversationIO.close();
                    messageIO.close();
                }
                message.notifyListeners();

            }
        });

    }

}
