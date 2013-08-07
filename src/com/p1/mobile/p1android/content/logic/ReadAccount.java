package com.p1.mobile.p1android.content.logic;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.parsing.AccountParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadAccount {
    public static final String TAG = ReadAccount.class.getSimpleName();

    public static final String USER_ID_ME = "me";

    public static Account requestAccount(IContentRequester requester) {
        Account account = ContentHandler.getInstance().getAccount(requester);

        boolean accountIsValid = false;
        AccountIOSession io = account.getIOSession();
        try {
            accountIsValid = io.isValid();
        } finally {
            io.close();
        }

        if (!accountIsValid) {
            fetchAccount();
        }

        return account;
    }

    public static void fetchAccount() {
        final Account account = ContentHandler.getInstance().getAccount(null);
        boolean noActiveRequest;
        AccountIOSession io = account.getIOSession();
        try {
            noActiveRequest = (io.getLastAPIRequest() == 0 && NetworkUtilities
                    .getLoggedInUserId() != null);
            if (noActiveRequest) {
                io.refreshLastAPIRequest();
            }
        } finally {
            io.close();
        }

        if (noActiveRequest) {
            ContentHandler.getInstance().getLowPriorityNetworkHandler()
                    .post(new Runnable() {

                        @Override
                        public void run() {
                            boolean accountChanged = false;
                            String accountRequest = ReadContentUtil.netFactory
                                    .createAccountRequest();

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        accountRequest, null).getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");

                                JsonArray accountsArray = data
                                        .getAsJsonArray("accounts");
                                Iterator<JsonElement> iterator = accountsArray
                                        .iterator();
                                if (iterator.hasNext()) { // Will save the
                                                          // single returned
                                                          // account
                                    JsonObject accountJson = iterator.next()
                                            .getAsJsonObject();
                                    accountChanged = AccountParser
                                            .parseToAccount(accountJson,
                                            account);

                                }

                                account.notifyListeners();
                                Log.d(TAG,
                                        "All listeners notified as result of requestAccount");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed getting account", e);
                            } finally {
                                AccountIOSession io = account.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                    if (accountChanged
                                            && io.getUnreadNotifications() > 0) {
                                        ReadNotification
                                                .fillNotificationList(true);
                                        Log.d(TAG,
                                                "Fetching notifications as a result of account request");
                                    }
                                    if (accountChanged
                                            && io.getUnreadMessages() > 0) {
                                        ReadConversation.fillConversationList();
                                        Log.d(TAG,
                                                "Fetching conversations as a result of account request");
                                    }
                                } finally {
                                    io.close();
                                }
                            }

                        }

                    });
        }
    }
}
