package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Profile;
import com.p1.mobile.p1android.content.Profile.ProfileIOSession;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadProfile {
    public static final String TAG = ReadProfile.class.getSimpleName();

    public static final String USER_ID_ME = "me";

    public static Profile requestProfile(String id, IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        Profile profile = ContentHandler.getInstance()
                .getProfile(id, requester);

        boolean profileIsValid = false;
        ProfileIOSession io = profile.getIOSession();
        try {
            profileIsValid = io.isValid();
        } finally {
            io.close();
        }

        if (!profileIsValid) {
            fetchProfile(profile);
        }

        return profile;
    }

    public static Profile requestLoggedInProfile(IContentRequester requester) {
        String userId = NetworkUtilities.getLoggedInUserId();
        if (userId == null) {
            userId = USER_ID_ME;
        }
        return requestProfile(userId, requester);
    }

    private static void fetchProfile(final Profile profile) {
        boolean noActiveRequest;
        ProfileIOSession io = profile.getIOSession();
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
                            String userId = null;
                            ProfileIOSession io = profile.getIOSession();
                            try {
                                userId = io.getId();
                            } finally {
                                io.close();
                            }
                            String profileRequest = ReadContentUtil.netFactory
                                    .createGetProfileRequest(userId);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        profileRequest, null).getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");

                                JsonArray profilesArray = data
                                        .getAsJsonArray("profiles");
                                ReadContentUtil
                                        .saveExtraProfiles(profilesArray);

                                if (data.has("users")) {
                                    JsonArray usersArray = data
                                            .getAsJsonArray("users");
                                    ReadContentUtil.saveExtraUsers(usersArray);
                                }

                                profile.notifyListeners();
                                Log.d(TAG,
                                        "All listeners notified as result of requestProfile");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed getting profile", e);
                            } finally {
                                io = profile.getIOSession();
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
}
