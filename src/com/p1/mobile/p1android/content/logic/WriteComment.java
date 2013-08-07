package com.p1.mobile.p1android.content.logic;

import java.util.Date;
import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Comment;
import com.p1.mobile.p1android.content.Comment.CommentIOSession;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Content.ContentIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IdTypePair;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.background.RetryRunnable;
import com.p1.mobile.p1android.content.parsing.CommentParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 *         All results of writing to content is seen in ContentChanged.
 */
public class WriteComment {
    public static final String TAG = WriteComment.class.getSimpleName();

    public static void sendShareComment(String text, String shareId) {
        Share share = ContentHandler.getInstance().getShare(shareId, null);
        sendComment(text, share);
    }

    public static void sendPictureComment(String text, String pictureId) {
        Picture picture = ContentHandler.getInstance().getPicture(pictureId,
                null);
        sendComment(text, picture);
    }

    private static void sendComment(String text, Share share) {
        ShareIOSession shareIo = share.getIOSession();
        try {
            if (shareIo.isSinglePictureShare()) {
                Log.w(TAG,
                        "Redirecting comment from singlepictureshare to picture");
                sendComment(text, shareIo.getSinglePicture());
                share.notifyListeners();
                return;
            }
        } finally {
            shareIo.close();
        }

        String fakeId = FakeIdGenerator.getNextFakeId();
        Comment comment = ContentHandler.getInstance().getComment(fakeId, null);
        ContentHandler.getInstance().getFakeIdTracker().track(fakeId, comment);
        CommentIOSession commentIo = comment.getIOSession();
        shareIo = share.getIOSession();
        try {
            commentIo.setValue(text);
            commentIo.setCreatedTime(new Date());
            commentIo.setOwnerId(NetworkUtilities.getLoggedInUserId());
            commentIo.setParent(new IdTypePair(shareIo.getId(),
                    IdTypePair.Type.SHARE));
            commentIo.setValid(true);
            shareIo.getCommentIds().add(0, fakeId);
            ContentHandler.getInstance().getFakeIdTracker()
                    .track(fakeId, share);
            shareIo.incrementTotalComments();
            shareIo.incrementUnfinishedUserModifications();
        } finally {
            commentIo.close();
            shareIo.close();
        }
        
        share.notifyListeners();

        sendComment(share, comment);

    }

    public static void sendComment(String text, Picture picture) {
        String fakeId = FakeIdGenerator.getNextFakeId();
        Comment comment = ContentHandler.getInstance().getComment(fakeId, null);
        ContentHandler.getInstance().getFakeIdTracker().track(fakeId, comment);
        CommentIOSession commentIo = comment.getIOSession();
        PictureIOSession pictureIo = picture.getIOSession();
        try {
            commentIo.setValue(text);
            commentIo.setCreatedTime(new Date());
            commentIo.setOwnerId(NetworkUtilities.getLoggedInUserId());
            commentIo.setParent(new IdTypePair(pictureIo.getId(),
                    IdTypePair.Type.PICTURE));
            commentIo.setValid(true);
            pictureIo.getCommentIds().add(0, fakeId);
            ContentHandler.getInstance().getFakeIdTracker()
                    .track(fakeId, picture);
            pictureIo.incrementTotalComments();
            pictureIo.incrementUnfinishedUserModifications();
        } finally {
            commentIo.close();
            pictureIo.close();
        }

        picture.notifyListeners();

        sendComment(picture, comment);

    }

    private static void sendComment(final Content commentedContent,
            final Comment comment) {

        ContentHandler.getInstance().getNetworkHandler()
                .post(new RetryRunnable() {
            @Override
            public void run() {
                String messageRequest;
                String fakeId;
                ContentIOSession commentedIO = commentedContent
                        .getIOSession();
                CommentIOSession commentIO = comment
                        .getIOSession();
                try {
                    if(FakeIdGenerator.isFakeId(commentedIO.getId())){
                        retry();
                        return;
                    }
                    fakeId = commentIO.getId();
                    messageRequest = ReadContentUtil.netFactory
                            .createCommentRequest(commentedIO.getType(), commentedIO.getId());
                } finally {
                    commentedIO.close();
                    commentIO.close();
                }

                try {
                    Network network = NetworkUtilities.getNetwork();

                    JsonObject jsonResponse = network.makePostRequest(
                            messageRequest,
                            null, CommentParser.serializeComment(comment))
                            .getAsJsonObject();
                    Log.d(TAG, "Comment response: " + jsonResponse);

                    JsonObject data = jsonResponse.getAsJsonObject("data");
                    JsonArray commentsArray = data.getAsJsonArray("comments");
                    
                    Iterator<JsonElement> iterator = commentsArray.iterator();
                    if (iterator.hasNext()) { // Will save the
                                              // single returned
                                              // comment
                        JsonObject commentJson = iterator.next().getAsJsonObject();
                        String newCommentId = commentJson.get("id").getAsString();

                        if (fakeId != newCommentId) {
                            ContentHandler.getInstance().getFakeIdTracker()
                                    .update(fakeId, newCommentId);
                        }

                        CommentParser.parseToComment(commentJson, comment);
                    }

                    Log.d(TAG, "Comment successful");
                } catch (Exception e) {
                    Log.e(TAG, "Failed commenting", e);
                    retry();
                } finally {
                    commentedIO = commentedContent.getIOSession();
                    try {
                        commentedIO.decrementUnfinishedUserModifications();
                    } finally {
                        commentedIO.close();
                    }
                }

            }
        });
    }

}
