package com.p1.mobile.p1android.content.logic;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.entity.mime.MultipartEntity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.content.BatchReferences;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Feed;
import com.p1.mobile.p1android.content.Feed.FeedIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.background.BackgroundNetworkService;
import com.p1.mobile.p1android.content.background.RetryRunnable;
import com.p1.mobile.p1android.content.parsing.BatchReferenceParser;
import com.p1.mobile.p1android.content.parsing.PictureParser;
import com.p1.mobile.p1android.content.parsing.ShareParser;
import com.p1.mobile.p1android.net.BatchUtil;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.listeners.BitmapLoaderListener;
import com.p1.mobile.p1android.util.BitmapLoaderTask;
import com.p1.mobile.p1android.util.BitmapUtils;
import com.p1.mobile.p1android.util.ThumbnailLoaderTask;

/**
 * 
 * @author Anton
 * 
 */
public class WriteShare {
    public static final String TAG = WriteShare.class.getSimpleName();

    private static HashMap<Share, UploadSession> uploadSessions = new HashMap<Share, UploadSession>();
    private static Hashtable<Share, Runnable> failedUploads = new Hashtable<Share, Runnable>();

    public static Share initNewShare() {
        String shareId = FakeIdGenerator.getNextFakeId();
        Share newShare = ContentHandler.getInstance().getShare(shareId, null);
        ContentHandler.getInstance().getFakeIdTracker()
                .track(shareId, newShare);
        ShareIOSession io = newShare.getIOSession();
        try{
            io.setOwnerId(NetworkUtilities.getSafeLoggedInUserId());
        } finally {
            io.close();
        }
        uploadSessions.put(newShare, new UploadSession(newShare));
        return newShare;
    }

    /**
     * This method automatically makes the uploadSession listen to Picture
     * contentChanged updates to check when it should start uploading
     * 
     * @param share
     * @param imageUri
     * @return
     */
    private static Picture createBasicPicture(Share share, String imageUri) {
        final UploadSession uploadSession = uploadSessions.get(share);
        String pictureId = FakeIdGenerator.getNextFakeId();
        final Picture newPicture = ContentHandler.getInstance().getPicture(
                pictureId, uploadSession);
        ContentHandler.getInstance().getFakeIdTracker()
                .track(pictureId, newPicture);
        ShareIOSession shareIO = share.getIOSession();
        PictureIOSession pictureIO = newPicture.getIOSession();
        try {
            pictureIO.setOwnerId(NetworkUtilities.getSafeLoggedInUserId());

            pictureIO.setInternalImageUri(imageUri);

            shareIO.getPictureIds().add(pictureId);
            ContentHandler.getInstance().getFakeIdTracker()
                    .track(pictureId, share);

            shareIO.updateValidity();
        } finally {
            pictureIO.close();
            shareIO.close();
        }
        uploadSession.pictures.put(newPicture, null);
        return newPicture;
    }

    /**
     * Creates new Pictures and attaches it to the Share.
     * 
     * Also saves the pictures for sending.
     * 
     * @param share
     * @param imageUris
     */
    public static void addPictures(final Share share,
            final List<String> imageUris) {
        Log.d(TAG, "(1.multi) addPicture called");
        if (imageUris == null) {
            throw new IllegalArgumentException("ImageUri must not be null");
        }
        final UploadSession uploadSession = uploadSessions.get(share);
        final List<Picture> newPictures = new ArrayList<Picture>();
        for (String imageUri : imageUris) {
            final Picture newPicture = createBasicPicture(share, imageUri);
            newPictures.add(newPicture);

            new ThumbnailLoaderTask(P1Application.getP1ApplicationContext(), new BitmapLoaderListener(){

                        @Override
                        public void onBitmapLoaded(Bitmap bitmap) {
                            PictureIOSession pictureIO = newPicture
                                    .getIOSession();
                            try {
                                pictureIO.setTemporaryThumbnail(BitmapUtils
                                        .getBitmapThumbnail(bitmap));

                                pictureIO.setValid(true);
                            } finally {
                                pictureIO.close();
                            }
                            newPicture.notifyListeners();

                        }
                
                    }).execute(imageUri);
        }
        
        for (final Picture picture : newPictures) {
            final String imageUri;
            PictureIOSession io = picture.getIOSession();
            try {
                imageUri = io.getInternalImageUri();
            } finally {
                io.close();
            }
            new BitmapLoaderTask(
                P1Application.getP1ApplicationContext(),
                new BitmapLoaderListener() {

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap) {
                        Log.d(TAG, "(3) Starting to load picture");
                        if (bitmap == null)
                            Log.e(TAG, "Failed to find bitmap for " + imageUri);
                        byte[] compressedJpeg = BitmapUtils
                                .compressToJpegByteArray(bitmap);
                            uploadSession.pictures.put(picture, compressedJpeg);
                        uploadSession.compressedImageCount++;
                            PictureIOSession pictureIO = picture.getIOSession();
                        try {

                            pictureIO.setSize(new Point(bitmap.getWidth(),
                                    bitmap.getHeight()));

                            pictureIO.setValid(true);
                        } finally {
                            pictureIO.close();
                        }
                            picture.notifyListeners();

                    }
                    }).execute(imageUri);
        }



    }
    
    /**
     * Efficiently add a picture to a SinglePictureShare
     * 
     * @param share
     * @param bitmap
     *            a bitmap of proper size for sending through network.
     */
    public static void addSinglePicture(Share share, final Bitmap bitmap) {
        final UploadSession uploadSession = uploadSessions.get(share);
        final Picture newPicture = createBasicPicture(share, null);
        Log.d(TAG, "(1.single) addSinglePicture called");

        new Thread(new Runnable() {

            @Override
            public void run() {
                byte[] compressedJpeg = BitmapUtils
                        .compressToJpegByteArray(bitmap);
                uploadSession.pictures.put(newPicture, compressedJpeg);
                uploadSession.compressedImageCount++;
                Log.d(TAG, "Single picture compressed");
                newPicture.notifyListeners();
            }
        }).start();

        ShareIOSession shareIO = share.getIOSession();
        PictureIOSession pictureIO = newPicture.getIOSession();
        try{
            pictureIO.setSize(new Point(bitmap.getWidth(), bitmap.getHeight()));
            pictureIO.setTemporaryFullImage(bitmap);
            pictureIO.setValid(true);
            moveCaptionToSinglePicture(shareIO, pictureIO);
        } finally {
            shareIO.close();
            pictureIO.close();
        }
        newPicture.notifyListeners();
    }

    private static void moveCaptionToSinglePicture(ShareIOSession shareIO,
            PictureIOSession pictureIO) {
        if (shareIO.isSinglePictureShare()) {
            pictureIO.setCaption(shareIO.getCaption());
            shareIO.setCaption("");
        }
    }

    /**
     * Adds an existing venue to the Share
     * 
     * @param share
     * @param venueId
     */
    public static void addVenue(Share share, String venueId) {
        ShareIOSession io = share.getIOSession();
        try {
            io.setVenueId(venueId);
        } finally {
            io.close();
        }
    }

    public static void addCaption(Share share, String caption) {
        Log.d(TAG, "(2) addCaption called");
        ShareIOSession shareIO = share.getIOSession();
        try {
            if (shareIO.isSinglePictureShare()) {
                PictureIOSession pictureIO = shareIO.getSinglePicture()
                        .getIOSession();
                try {
                    pictureIO.setCaption(caption);
                } finally {
                    pictureIO.close();
                }
            } else {
                shareIO.setCaption(caption);
                shareIO.updateValidity();
            }
        } finally {
            shareIO.close();
        }
    }

    /**
     * Clears all unsent information
     * 
     * @param share
     */
    public static void abort(Share share) {
        finish(uploadSessions.get(share));
        // TODO make fakeIdTracker stop tracking all associated objects.
    }

    /**
     * Places the share on top of the Feed and sends it to the API
     * 
     * @param share
     * @return true if the share is valid for sending
     */
    public static void send(Share share) {
        Log.d(TAG, "(4) Requested to send");
        UploadSession uploadSession = uploadSessions.get(share);
        uploadSession.startSending();
    }

    private static void addShareToFeed(Share share) {
        Feed feed = ContentHandler.getInstance().getFeed(null);

        ShareIOSession shareIo = share.getIOSession();
        FeedIOSession feedIO = feed.getIOSession();
        try {
            if (!shareIo.isValid()) {
                throw new InvalidParameterException(
                        "Not enough information is set");
            }
            shareIo.setCreatedTime(new Date());

            feedIO.addFakeShareId(shareIo.getId());
            ContentHandler.getInstance().getFakeIdTracker()
                    .track(shareIo.getId(), feed);
            feedIO.incrementUnfinishedUserModifications();
            feedIO.setValid(true);

        } finally {
            shareIo.close();
            feedIO.close();
        }
        feed.notifyListeners();
    }

    /**
     * removes all references of the uploadSession
     * 
     * @param uploadSession
     */
    private static void finish(UploadSession uploadSession) {
        ContentHandler.getInstance().removeRequester(uploadSession);
        uploadSessions.remove(uploadSession.share);
    }

    private static class AsynchronousBatchCall extends RetryRunnable {
        UploadSession uploadSession;

        public AsynchronousBatchCall(UploadSession uploadSession) {
            this.uploadSession = uploadSession;
        }

        @Override
        public void run() {
            String batchRequest = ReadContentUtil.netFactory
                    .createBatchRequest();
            try{
                Network network = NetworkUtilities.getNetwork();
                JsonObject json = network.makeBatchRequest(batchRequest, null,
                        uploadSession.getMultipartEntity()).getAsJsonObject();
                JsonObject data = json.getAsJsonObject("data");
                BatchReferences batchReferences = BatchReferenceParser
                        .parseBatchReferences(data.get("batch_references"));
                ContentHandler.getInstance().getFakeIdTracker()
                        .update(batchReferences.getAllIdChanges());

                JsonArray pictureArray = data.getAsJsonArray("pictures");
                ReadContentUtil.saveExtraPictures(pictureArray);
                JsonArray sharesArray = data.getAsJsonArray("shares");
                ReadContentUtil.saveExtraShares(sharesArray);

                for (Picture picture : uploadSession.pictures.keySet()) {
                    picture.notifyListeners();
                }
                uploadSession.share.notifyListeners();

                finish(uploadSession);
                Log.d(TAG, "Batch call successful");
            } catch (Exception e) {
                Log.e(TAG, "Failed to send batch", e);
                increasePolling();
                retry();
            }
        }

        @Override
        protected void failedLastRetry() {
            super.failedLastRetry();
            ShareIOSession io = uploadSession.share.getIOSession();
            try {
                io.setHasFailedNetworkOperation(true);
            } finally {
                io.close();
            }
            uploadSession.share.notifyListeners();
            failedUploads.put(uploadSession.share, this);

        }
    }

    private static class UploadSession implements IContentRequester {
        public Share share;
        public HashMap<Picture, byte[]> pictures = new HashMap<Picture, byte[]>();
        public int compressedImageCount = 0;

        public boolean tryingToSend = false;
        public boolean hasBeenAddedToFeed = false;

        public UploadSession(Share share) {
            this.share = share;
        }

        @Override
        /**
         * Called when any relevant Picture becomes valid or when the user tries to send.
         */
        public void contentChanged(Content content) {
            synchronized (this) {
                if (isReadyToPlaceInFeed() && !hasBeenAddedToFeed) {
                    addShareToFeed(share);
                    hasBeenAddedToFeed = true;
                }
                if (tryingToSend && isReadyToSend()) {
                    Log.d(TAG, "Starting to send batch");
                    tryingToSend = false;
                    ContentHandler.getInstance().getNetworkHandler()
                            .post(new AsynchronousBatchCall(this));
                }
            }
        }

        public void startSending() {

            tryingToSend = true;
            contentChanged(null);
        }

        public boolean isReadyToPlaceInFeed() {
            if (pictures.size() == 0) {
                return false;
            }
            for (Picture picture : pictures.keySet()) {
                PictureIOSession io = picture.getIOSession();
                try {
                    if (!io.isValid()) {
                        Log.d(TAG,
                                "Share is not ready to plave in feed, a Picture is invalid");
                        return false;
                    }
                } finally {
                    io.close();
                }
            }
            ShareIOSession io = share.getIOSession();
            try {
                if (!io.isValid()) {
                    Log.d(TAG,
                            "Share is not ready to place in feed, Share is invalid");
                    return false;
                }
            } finally {
                io.close();
            }
            Log.d(TAG, "Share is ready to be placed in feed!");
            return true;
        }

        public boolean isReadyToSend() {
            if (pictures.size() == 0) {
                return false;
            }
            if (compressedImageCount < pictures.size()) {
                Log.d(TAG, "Share is not ready to be sent, only "
                        + compressedImageCount + " of " + pictures.size()
                        + " pictures are compressed");
                return false;
            }
            ShareIOSession io = share.getIOSession();
            try {
                if (!io.isValid()) {
                    Log.d(TAG,
                            "Share is not ready to be sent, Share is invalid");
                    return false;
                }
            } finally {
                io.close();
            }
            Log.d(TAG, "Share is ready to be sent!");
            return true;
        }

        public MultipartEntity getMultipartEntity(){
            MultipartEntity entity = BatchUtil.createMultipartEntity();
            for (Entry<Picture, byte[]> picturePair : pictures.entrySet()) {
                BatchUtil.addJson(entity,
                        PictureParser.serializePicture(picturePair.getKey())
                                .toString());
                BatchUtil.addJpeg(entity, picturePair.getValue(),
                        picturePair.getKey().getId());
            }
            BatchUtil.addJson(entity, ShareParser.serializeShare(share)
                    .toString());
            
            return entity;
        }

    }

    /**
     * Will mark the share as not failed and try sending it again.
     * 
     * @param share
     */
    public static void retrySendingShare(Share share) {
        Runnable runnable = failedUploads.remove(share);
        if (runnable != null) {
            ShareIOSession io = share.getIOSession();
            try {
                io.setHasFailedNetworkOperation(false);
            } finally {
                io.close();
            }
            share.notifyListeners();
            ContentHandler.getInstance().getNetworkHandler().post(runnable);
        }
    }

    private static void increasePolling() {
        Intent successfulNetworkIntent = new Intent(
                P1Application.getP1ApplicationContext(),
                BackgroundNetworkService.class);
        successfulNetworkIntent.putExtra(BackgroundNetworkService.START_CODE,
                BackgroundNetworkService.CODE_INCREASE_POLLING);
        P1Application.getP1ApplicationContext().startService(
                successfulNetworkIntent);
    }


}
