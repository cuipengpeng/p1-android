package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.parsing.UserParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 */
public class WriteUser {
    public static final String TAG = WriteUser.class.getSimpleName();

    public static void changeEnglishGivenName(User user, String givenName) {
        UserIOSession io = user.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setEnUsGivenName(givenName);
                updateFullnames(io);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        user.notifyListeners();

        sendUser(user);
    }

    public static void changeEnglishSurname(User user, String surname) {
        UserIOSession io = user.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setEnUsSurname(surname);
                updateFullnames(io);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        user.notifyListeners();

        sendUser(user);
    }

    public static void changeLanguage(User user, String language) {
        UserIOSession io = user.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setPreferredLanguage(language);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        user.notifyListeners();

        sendUser(user);
    }

    public static void changeCity(User user, String city) {
        UserIOSession io = user.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setCity(city);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        user.notifyListeners();

        sendUser(user);
    }

    public static void changeCareerCompany(User user, String company) {
        UserIOSession io = user.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setCareerCompany(company);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        user.notifyListeners();

        sendUser(user);
    }

    public static void changeCareerPosition(User user, String position) {
        UserIOSession io = user.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setCareerPosition(position);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        user.notifyListeners();

        sendUser(user);
    }

    public static void changeEducation(User user, String education) {
        UserIOSession io = user.getIOSession();
        try {
            if (io.getId().equals(NetworkUtilities.getLoggedInUserId())) {
                io.setEducation(education);
                io.incrementUnfinishedUserModifications();
            }
        } finally {
            io.close();
        }
        user.notifyListeners();

        sendUser(user);
    }

    private static void sendUser(final User user) {

        ContentHandler.getInstance().getNetworkHandler().post(new Runnable() {
            @Override
            public void run() {
                String userRequest;
                UserIOSession io = user.getIOSession();
                try {
                    userRequest = ReadContentUtil.netFactory
                            .createGetUserRequest(io.getId());
                } finally {
                    io.close();
                }

                try {
                    Network network = NetworkUtilities.getNetwork();

                    JsonObject jsonResponse = network.makePatchRequest(
                            userRequest, null, UserParser.serializeUser(user))
                            .getAsJsonObject();
                    Log.d(TAG, "User response: " + jsonResponse);

                    // no need to save the user as no unknown information is
                    // returned

                    Log.d(TAG, "User modification successful");
                } catch (Exception e) {
                    Log.e(TAG, "Failed modifying user", e);
                } finally {
                    io = user.getIOSession();
                    try {
                        io.decrementUnfinishedUserModifications();
                    } finally {
                        io.close();
                    }
                }
            }
        });
    }

    private static void updateFullnames(UserIOSession io) {
        io.setEnUsFullname(io.getEnUsGivenName() + " " + io.getEnUsSurname());
    }

}
