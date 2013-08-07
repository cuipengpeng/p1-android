package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Profile;
import com.p1.mobile.p1android.content.Profile.BloodType;
import com.p1.mobile.p1android.content.Profile.MaritalStatus;
import com.p1.mobile.p1android.content.Profile.ProfileIOSession;
import com.p1.mobile.p1android.content.parsing.ProfileParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 */
public class WriteProfile {
    public static final String TAG = WriteProfile.class.getSimpleName();

    public static void changeBloodType(Profile profile, BloodType bloodType) {
        ProfileIOSession io = profile.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setBloodtype(bloodType);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        profile.notifyListeners();

        sendProfile(profile);
    }

    public static void changeMaritalStatus(Profile profile,
            MaritalStatus maritalStatus) {
        ProfileIOSession io = profile.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setMarital(maritalStatus);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        profile.notifyListeners();

        sendProfile(profile);
    }

    public static void changeDescription(Profile profile, String description) {
        ProfileIOSession io = profile.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setDescription(description);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        profile.notifyListeners();

        sendProfile(profile);
    }

    private static void sendProfile(final Profile profile) {

        ContentHandler.getInstance().getNetworkHandler().post(new Runnable() {
            @Override
            public void run() {
                String profileRequest;
                ProfileIOSession io = profile.getIOSession();
                try {
                    profileRequest = ReadContentUtil.netFactory
                            .createProfileRequest(io.getId());
                } finally {
                    io.close();
                }

                try {
                    Network network = NetworkUtilities.getNetwork();

                    JsonObject jsonResponse = network.makePatchRequest(
                            profileRequest, null,
                            ProfileParser.serializeProfile(profile))
                            .getAsJsonObject();
                    Log.d(TAG, "Profile response: " + jsonResponse);

                    // no need to save the profile as no unknown information is
                    // returned

                    Log.d(TAG, "Profile modification successful");
                } catch (Exception e) {
                    Log.e(TAG, "Failed modifying profile", e);
                } finally {
                    io = profile.getIOSession();
                    try {
                        io.decrementUnfinishedUserModifications();
                    } finally {
                        io.close();
                    }
                }
            }
        });
    }
}
