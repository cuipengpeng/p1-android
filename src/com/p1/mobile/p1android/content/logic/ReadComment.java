package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Comment;
import com.p1.mobile.p1android.content.CommentableIOSession;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.parsing.CommentParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadComment {
    public static final String TAG = ReadComment.class.getSimpleName();

    public static final int COMMENT_PAGINATION_LIMIT = 30;

    /**
     * Currently returns the best memory representation available.
     */
    public static Comment requestComment(String id, IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        Comment comment = ContentHandler.getInstance()
                .getComment(id, requester);

        return comment;

    }

    public static void fillComments(Picture picture) {
        internalFillComments(picture);
    }

    public static void fillComments(Share share) {
        ShareIOSession io = share.getIOSession();
        try {
            if (io.isSinglePictureShare()) {
                fillComments(io.getSinglePicture());
                return;
            }
        } finally {
            io.close();
        }
        internalFillComments(share);
    }

    private static void internalFillComments(final Content commentableContent) {
        boolean shouldMakeNetworkCall = false;
        CommentableIOSession io = (CommentableIOSession) commentableContent
                .getIOSession();
        try {
            if (io.getLastAPIRequest() == 0 && io.hasMoreComments()) {
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
                            boolean clearOldComments = false;
                            String commentRequest;
                            CommentableIOSession io = (CommentableIOSession) commentableContent
                                    .getIOSession();
                            try {
                                paginationOffset = io.getCommentIds().size();
                                if (paginationOffset < COMMENT_PAGINATION_LIMIT) {
                                    // has not fetched the first batch of
                                    // comments
                                    paginationOffset = 0;
                                    clearOldComments = true;
                                }
                                commentRequest = ReadContentUtil.netFactory
                                        .createGetCommentsRequest(io.getType(),
                                                io.getId(), paginationOffset,
                                                COMMENT_PAGINATION_LIMIT);

                            } finally {
                                io.close();
                            }

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject jsonResponse = network
                                        .makeGetRequest(commentRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = jsonResponse
                                        .getAsJsonObject("data");
                                JsonArray usersArray = data
                                        .getAsJsonArray("users");
                                ReadContentUtil.saveExtraUsers(usersArray);
                                JsonArray commentsArray = data
                                        .getAsJsonArray("comments");
                                ReadContentUtil
                                        .saveExtraComments(commentsArray);

                                CommentParser.appendToCommentable(jsonResponse,
                                        commentableContent, clearOldComments);

                                commentableContent.notifyListeners();

                                Log.d(TAG,
                                        "All listeners notified as result of fillCommentable");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed getting comments", e);
                            } finally {
                                io = (CommentableIOSession) commentableContent
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
