package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.UserPicturesList;
import com.p1.mobile.p1android.content.UserPicturesList.RangePagination;
import com.p1.mobile.p1android.content.UserPicturesList.UserPicturesListIOSession;
import com.p1.mobile.p1android.content.parsing.UserPicturesListParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadUserPictures {
    public static final String TAG = ReadUserPictures.class.getSimpleName();

    /**
     * This method does not automatically start filling the UserPicturesList, as
     * there is no telling which part should be filled.
     * 
     * @param ownerId
     * @param requester
     * @return
     */
    public static UserPicturesList requestUserPicturesList(String ownerId,
            IContentRequester requester) {
        if (ownerId == null)
            throw new NullPointerException("Id must be non-null");
        UserPicturesList userPicturesList = ContentHandler.getInstance()
                .getUserPicturesList(ownerId, requester);

        // fillUserPicturesList(userPicturesList); // TODO paginated filling

        return userPicturesList;
    }

    /**
     * This method will make a network request to fill items near the supplied
     * origin picture id.
     * 
     * @param ownerId
     *            String id of the user that is the owner of the pictures
     * @param requester
     * @return
     */
    public static UserPicturesList requestUserPicturesList(String ownerId,
            IContentRequester requester, String originPictureId) {
        if (ownerId == null)
            throw new NullPointerException("Id must be non-null");
        UserPicturesList userPicturesList = ContentHandler.getInstance()
                .getUserPicturesList(ownerId, requester);

        ReadUserPictures.fillUserPicturesListInitial(userPicturesList,
                originPictureId);

        return userPicturesList;
    }

    private static void fillUserPicturesList(
            final UserPicturesList userPicturesList,
            final RangePagination pagination) {

        boolean noActiveRequest;

        UserPicturesListIOSession io = userPicturesList.getIOSession();
        try {
            noActiveRequest = io.getLastAPIRequest() == 0;
            Log.d(TAG, "No activive request " + noActiveRequest);
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
                            String listId = null;
                            UserPicturesListIOSession io = userPicturesList
                                    .getIOSession();
                            try {
                                listId = io.getId();
                            } finally {
                                io.close();
                            }
                            String userPicturesRequest = ReadContentUtil.netFactory
                                    .createGetUserPicturesListRequest(listId,
                                            pagination);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        userPicturesRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");
                                JsonArray picturesArray = data
                                        .getAsJsonArray("pictures");
                                ReadContentUtil
                                        .saveExtraPictures(picturesArray);
                                UserPicturesListParser
                                        .appendToUserPicturesList(object,
                                                userPicturesList, pagination);
                                Log.d(TAG,
                                        "Successful fetch of range requestUserPicturesList");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        "Failed getting user pictures list with range pagination",
                                        e);
                            } finally {
                                io = userPicturesList.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                            }
                            userPicturesList.notifyListeners();
                        }
                    });
        }
    }

    /**
     * Fills the UserPictureList with older pictures
     * 
     * @param userPicturesList
     * @param originId
     */
    public static void fillUserPicturesListPositive(
            final UserPicturesList userPicturesList, String originId) {
        RangePagination pagination;
        UserPicturesListIOSession io = userPicturesList.getIOSession();
        try {
            if (io.isAKnownId(originId)) {
                fillUserPicturesList(userPicturesList);
            } else {
                pagination = io.getPaginationPositive(originId);
                fillUserPicturesList(userPicturesList, pagination);
            }

        } finally {
            io.close();
        }

    }

    /**
     * Fills UserPicturesList with newer pictures
     * 
     * @param userPicturesList
     * @param originId
     */
    public static void fillUserPicturesListNegative(
            final UserPicturesList userPicturesList, String originId) {
        UserPicturesListIOSession io = userPicturesList.getIOSession();
        try {
            if (io.isAKnownId(originId)) { // paginated from start -> can't fill
                                           // to negative
                return;
            }
            RangePagination pagination = io.getPaginationNegative(originId);
            fillUserPicturesList(userPicturesList, pagination);
        } finally {
            io.close();
        }
    }

    public static void fillUserPicturesListInitial(
            final UserPicturesList userPicturesList, String originId) {
        UserPicturesListIOSession io = userPicturesList.getIOSession();
        try {
            if (io.requiresFillingNear(originId)) {
                RangePagination pagination = io.getPaginationInitial(originId);
                fillUserPicturesList(userPicturesList, pagination);
            }
        } finally {
            io.close();
        }
    }

    public static void fillUserPicturesList(
            final UserPicturesList userPicturesList) {

        boolean shouldMakeNetworkCall;

        UserPicturesListIOSession io = userPicturesList.getIOSession();
        try {
            shouldMakeNetworkCall = io.getLastAPIRequest() == 0 && io.hasMore();
            if (shouldMakeNetworkCall) {
                io.refreshLastAPIRequest();
            }
        } finally {
            io.close();
        }

        if (shouldMakeNetworkCall) {
            ContentHandler.getInstance().getNetworkHandler()
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            int paginationOffset, paginationLimit;
                            String listId = null;
                            UserPicturesListIOSession io = userPicturesList
                                    .getIOSession();
                            try {
                                listId = io.getId();
                                paginationOffset = io.getPaginationNextOffset();
                                paginationLimit = io.getPaginationLimit();
                            } finally {
                                io.close();
                            }
                            String userPicturesRequest = ReadContentUtil.netFactory
                                    .createGetUserPicturesListRequest(listId,
                                            paginationOffset, paginationLimit);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        userPicturesRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");
                                JsonArray picturesArray = data
                                        .getAsJsonArray("pictures");
                                ReadContentUtil
                                        .saveExtraPictures(picturesArray);
                                UserPicturesListParser
                                        .appendToUserPicturesList(object,
                                                userPicturesList);
                                Log.d(TAG,
                                        "Successfully fetched requestUserPicturesList");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        "Failed getting user pictures list with normal pagination",
                                        e);
                            } finally {
                                io = userPicturesList.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                            }
                            userPicturesList.notifyListeners();
                        }
                    });
        }
    }

}
