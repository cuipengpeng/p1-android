package com.p1.mobile.p1android.content.logic;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.parsing.UserParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadUser {
    public static final String TAG = ReadUser.class.getSimpleName();
    
    public static final String USER_ID_ME = "me";

    public static User requestUser(String id, IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        User user = ContentHandler.getInstance().getUser(id, requester);

        boolean userIsValid = false;
        UserIOSession io = user.getIOSession();
        try {
            userIsValid = io.isValid();

        } finally {
            io.close();
        }
        if (!userIsValid) {
            fetchUser(user);
        }

        return user;

    }
    
    public static User requestLoggedInUser(IContentRequester requester) {
        String userId = NetworkUtilities.getLoggedInUserId();
        if (userId == null) {
            userId = USER_ID_ME;
        }
        return requestUser(userId, requester);
    }

    /**
     * Fetches a single user over the network. UI never needs to call this.
     * 
     * @param user
     */
    public static void fetchUser(final User user) {

        boolean noActiveRequest;
        UserIOSession io = user.getIOSession();
        try {
            noActiveRequest = io.getLastAPIRequest() == 0;
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
                            String userId = null;
                            UserIOSession io = user
                                    .getIOSession();
                            try {
                                userId = io.getId();
                            } finally {
                                io.close();
                            }
                            String userRequest = ReadContentUtil.netFactory
                                    .createGetUserRequest(userId);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        userRequest, null).getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");
                                JsonArray usersArray = data
                                        .getAsJsonArray("users");

                                Iterator<JsonElement> iterator = usersArray
                                        .iterator();
                                if (iterator.hasNext()) { // Will save the
                                                          // single returned
                                                          // user
                                    JsonObject userJson = iterator.next()
                                            .getAsJsonObject();
                                    String newUserId = userJson.get("id")
                                            .getAsString();
                                    if (userId != newUserId) {
                                        ContentHandler
                                                .getInstance()
                                                .changeUserId(userId, newUserId);
                                    }
                                    UserParser.parseToUser(userJson, user);
                                    user.notifyListeners();
                                }

                                Log.d(TAG,
                                        "All listeners notified as result of requestUser");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        "Failed getting user", e);
                            } finally {
                                io = user.getIOSession();
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
