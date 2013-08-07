package com.p1.mobile.p1android.content.logic;

import java.util.Iterator;

import android.util.Log;

import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.NotificationList;
import com.p1.mobile.p1android.content.NotificationList.NotificationListIOSession;
import com.p1.mobile.p1android.content.NotificationStory;
import com.p1.mobile.p1android.content.NotificationStory.NotificationIOSession;
import com.p1.mobile.p1android.content.parsing.JsonFactory;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 *         Does not call ContentChanged to avoid the views throwing away the
 *         unread tag too early.
 */
public class WriteNotification {
    public static final String TAG = WriteNotification.class.getSimpleName();

    @Deprecated
    public static void markAsRead(String notificationId) {
        NotificationStory notification = ContentHandler.getInstance().getNotification(notificationId, null);
        NotificationIOSession io = notification.getIOSession();
        try {
            if (!io.isRead()) {
                io.setRead(true);
                io.incrementUnfinishedUserModifications();
            }

        } finally {
            io.close();
        }

        // TODO asynchronous sending of notification

    }

    /**
     * This method will not call contentChanged of NotificationStories nor
     * NotificationList and has a small delay to when notifications are marked
     * as read. This is to let the notifications list show notifications as
     * unread even when the underlying data says they are read.
     */
    public static void markAllAsRead() {
        final NotificationList notificationList = ContentHandler.getInstance()
                .getNotificationList(null);
        Account account = ContentHandler.getInstance().getAccount(null);
        AccountIOSession accountIO = account.getIOSession();
        NotificationListIOSession notificationListIO = notificationList
                .getIOSession();
        boolean shouldMarkAsRead = false;
        try {
            if (notificationListIO.getLastAPIRequest() == 0
                    && accountIO.getUnreadNotifications() > 0) {
                notificationListIO.refreshLastAPIRequest();
                shouldMarkAsRead = true;
                accountIO.setUnreadNotifications(0);
                account.notifyListeners();
            }
        } finally {
            accountIO.close();
            notificationListIO.close();
        }
        if (shouldMarkAsRead) {
            ContentHandler.getInstance().getLowPriorityNetworkHandler()
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            String notificationsRequest;
                            NotificationListIOSession io = notificationList
                                    .getIOSession();
                            String newestNotificationId;
                            try {
                                notificationsRequest = ReadContentUtil.netFactory
                                        .createNotificationsRequest();
                                newestNotificationId = io
                                        .getNewestNotificationId();
                            } finally {
                                io.close();
                            }

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                network.makePatchRequest(
                                        notificationsRequest,
                                        null,
                                        JsonFactory
                                                .createReadJson(newestNotificationId));

                                Log.d(TAG, "Mark read successful");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed marking read", e);
                            } finally {
                                internalMarkAllAsRead(notificationList);

                                io = notificationList.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                            }
                        }
                    });
        }
    }

    private static void internalMarkAllAsRead(NotificationList notificationList) {
        NotificationListIOSession notificationListIO = notificationList.getIOSession();
        try {
            Iterator<String> iterator = notificationListIO
                    .getNotificationIdList().iterator();
            while (iterator.hasNext()) {
                String notificationId = iterator.next();
                NotificationStory notification = ContentHandler.getInstance()
                        .getNotification(notificationId, null);
                NotificationIOSession notificationIO = notification
                        .getIOSession();
                try {
                    if (notificationIO.isRead()) {
                        break;
                    } else {
                        notificationIO.setRead(true);
                    }
                } finally {
                    notificationIO.close();
                }

            }
        } finally {
            notificationListIO.close();
        }

    }


}
