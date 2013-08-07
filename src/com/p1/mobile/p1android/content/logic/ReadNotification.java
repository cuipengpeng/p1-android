package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.NotificationList;
import com.p1.mobile.p1android.content.NotificationList.NotificationListIOSession;
import com.p1.mobile.p1android.content.NotificationStory;
import com.p1.mobile.p1android.content.parsing.NotificationListParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadNotification {
    public static final String TAG = ReadNotification.class.getSimpleName();

    /**
     * Currently returns the best memory representation available.
     * 
     */
    public static NotificationStory requestNotification(String id,
            IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        NotificationStory notification = ContentHandler.getInstance()
                .getNotification(id, requester);
        // TODO start asynchronous updates

        return notification;

    }

    /**
     * 
     * @param requester
     */
    public static NotificationList requestNotificationList(
            IContentRequester requester) {
        NotificationList notificationList = ContentHandler.getInstance()
                .getNotificationList(requester);

        fillNotificationList();

        return notificationList;
    }

    public static void fillNotificationList() {
        fillNotificationList(false);
    }

    public static void fillNotificationList(
            final boolean fetchNewestNotifications) {
        final NotificationList notificationList = ContentHandler.getInstance()
                .getNotificationList(null);

        boolean noActiveRequest;
        NotificationListIOSession io = notificationList.getIOSession();
        try {
            noActiveRequest = io.getLastAPIRequest() == 0;
            if (noActiveRequest) {
                io.refreshLastAPIRequest();
                if (fetchNewestNotifications) {
                    io.resetPagination();
                }
            }
        } finally {
            io.close();
        }

        if (noActiveRequest) {
            ContentHandler.getInstance().getLowPriorityNetworkHandler()
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            int paginationOffset, paginationLimit;
                            NotificationListIOSession io = notificationList
                                    .getIOSession();
                            try {
                                paginationOffset = io.getPaginationNextOffset();

                                paginationLimit = io.getPaginationLimit();
                            } finally {
                                io.close();
                            }
                            String notificationsRequest = ReadContentUtil.netFactory
                                    .createGetNotificationsRequest(
                                            paginationOffset, paginationLimit);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        notificationsRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");

                                JsonArray picturesArray = data
                                        .getAsJsonArray("pictures");
                                ReadContentUtil
                                        .saveExtraPictures(picturesArray);
                                JsonArray usersArray = data
                                        .getAsJsonArray("users");
                                ReadContentUtil.saveExtraUsers(usersArray);
                                JsonArray sharesArray = data
                                        .getAsJsonArray("shares");
                                ReadContentUtil.saveExtraShares(sharesArray);
                                JsonArray commentsArray = data
                                        .getAsJsonArray("comments");
                                ReadContentUtil
                                        .saveExtraComments(commentsArray);
                                JsonArray notificationArray = data
                                        .getAsJsonArray("stories");
                                ReadContentUtil
                                        .saveExtraNotifications(notificationArray);
                                NotificationListParser
                                        .appendToNotificationList(object,
                                        notificationList);

                                notificationList.notifyListeners();

                                Log.d(TAG,
                                        "All listeners notified as result of fillNotificationList");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        "Failed getting notification list", e);
                            } finally{
                                io = notificationList.getIOSession();
                                try{
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                            }
                        }
                    });
        }

    }

}
