package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Comment;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.LikeableIOSession;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.parsing.LikeParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadLike {
    public static final String TAG = ReadLike.class.getSimpleName();

    public static final int LIKE_PAGINATION_LIMIT = 30;

    public static void fillLikes(Comment comment) {
        internalFillLikes(comment);
    }

    public static void fillLikes(Picture picture) {
        internalFillLikes(picture);
    }

    public static void fillLikes(Share share) {
        ShareIOSession io = share.getIOSession();
        try {
            if (io.isSinglePictureShare()) {
                fillLikes(io.getSinglePicture());
                return;
            }
        } finally {
            io.close();
        }
        internalFillLikes(share);
    }

    private static void internalFillLikes(final Content likeableContent) {
        boolean shouldMakeNetworkCall = false;
        LikeableIOSession io = (LikeableIOSession) likeableContent
                .getIOSession();
        try {
            if (io.getLastAPIRequest() == 0 && io.hasMoreLikes()) {
                shouldMakeNetworkCall = true; // Fetch new information
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
                            int paginationOffset = 0;
                            boolean clearOldLikes = false;
                            String likesRequest;
                            LikeableIOSession io = (LikeableIOSession) likeableContent
                                    .getIOSession();
                            try {
                                paginationOffset = io.getLikeUserIds().size();
                                if (paginationOffset < LIKE_PAGINATION_LIMIT) {
                                    // has not fetched the first batch of
                                    // comments
                                    paginationOffset = 0;
                                    clearOldLikes = true;
                                }
                                likesRequest = ReadContentUtil.netFactory
                                        .createGetLikesRequest(io.getType(),
                                                io.getId(), paginationOffset,
                                                LIKE_PAGINATION_LIMIT);

                            } finally {
                                io.close();
                            }

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject jsonResponse = network
                                        .makeGetRequest(likesRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = jsonResponse
                                        .getAsJsonObject("data");
                                JsonArray usersArray = data
                                        .getAsJsonArray("users");
                                ReadContentUtil.saveExtraUsers(usersArray);

                                LikeParser.appendToLikeable(jsonResponse,
                                        likeableContent, clearOldLikes);

                                likeableContent.notifyListeners();

                                Log.d(TAG,
                                        "All listeners notified as result of fillLikeable");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed getting likes", e);
                            } finally {
                                io = (LikeableIOSession) likeableContent
                                        .getIOSession();
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
