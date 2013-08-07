package com.p1.mobile.p1android.content.logic;

import java.security.InvalidParameterException;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Content.ContentIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.LikeableIOSession;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.background.RetryRunnable;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 */
public class WriteLike {
    public static final String TAG = WriteLike.class.getSimpleName();

    /**
     * Toggles like for Picture, Share or Comment, and notifies all listeners of
     * changes.
     * 
     * @param likeableContent
     */
    public static void toggleLike(Content likeableContent) {
        if (likeableContent == null) {
            throw new InvalidParameterException(
                    "likeableContent can not be null");
        }
        Content notifyThisInstead = null;
        if (likeableContent instanceof Share) {
            ShareIOSession shareIO = (ShareIOSession) likeableContent
                    .getIOSession();
            try {
                if (shareIO.isSinglePictureShare()) { // The Picture is
                                                      // relevant, not the Share
                    notifyThisInstead = likeableContent;
                    likeableContent = shareIO.getSinglePicture();
                }
            } finally {
                shareIO.close();
            }
        }

        ContentIOSession io = likeableContent
                .getIOSession();
        try {
            LikeableIOSession likeableIO = (LikeableIOSession) io;
            if (likeableIO.isValid()) {
                if (likeableIO.hasLiked()) {
                    likeableIO.setHasLiked(false);
                    likeableIO.decrementTotalLikes();
                    likeableIO.getLikeUserIds().remove(
                            NetworkUtilities.getLoggedInUserId());
                    likeableIO.incrementUnfinishedUserModifications();
                } else {
                    likeableIO.setHasLiked(true);
                    likeableIO.incrementTotalLikes();
                    likeableIO.getLikeUserIds().add(0,
                            NetworkUtilities.getLoggedInUserId());
                    likeableIO.incrementUnfinishedUserModifications();
                }
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "Tried to like content that can not be liked");
            return;
        } finally {
            io.close();
        }
        if (notifyThisInstead != null) { // Also notifies the Picture
            notifyThisInstead.notifyListeners();
        } else {
            likeableContent.notifyListeners();
        }

        sendLike(likeableContent);
    }

    private static void sendLike(final Content likeableContent) {

        ContentHandler.getInstance().getNetworkHandler()
                .post(new RetryRunnable() {
            @Override
            public void run() {
                String messageRequest;
                boolean liked;
                LikeableIOSession io = (LikeableIOSession) likeableContent
                        .getIOSession();
                try {
                    if (FakeIdGenerator.isFakeId(io.getId())) {
                        retry();
                        return;
                    }
                    liked = io.hasLiked();
                    messageRequest = ReadContentUtil.netFactory
                            .createLikeRequest(io.getType(), io.getId());
                } finally {
                    io.close();
                }

                try {
                    Network network = NetworkUtilities.getNetwork();

                    if (liked) {
                        JsonObject object = network.makePutRequest(
                                messageRequest, null, null).getAsJsonObject();
                        Log.d(TAG, "Like response: " + object);
                    } else {
                        network.makeDeleteRequest(messageRequest, null);
                        Log.d(TAG, "Unlike");
                    }

                    io = (LikeableIOSession) likeableContent.getIOSession();
                    try {
                        io.decrementUnfinishedUserModifications();
                    } finally {
                        io.close();
                    }
                    if (liked) {
                        Log.d(TAG, "Like successful");
                    } else {
                        Log.d(TAG, "Unlike successful");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed liking", e);
                    retry();
                }
            }
            
            @Override
            protected void failedLastRetry(){
                ContentIOSession io = likeableContent.getIOSession();
                try {
                    io.decrementUnfinishedUserModifications();
                } finally {
                    io.close();
                }
            }
        });
    }
}
