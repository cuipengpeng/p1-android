package com.p1.mobile.p1android.content.logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.net.ApiCalls;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.listeners.BitmapLoaderListener;
import com.p1.mobile.p1android.util.BitmapLoaderTask;
import com.p1.mobile.p1android.util.BitmapUtils;

public class WritePicture {
    public static final String TAG = WritePicture.class.getSimpleName();

    public interface WritePictureSuccessListener {
        public void successfulPictureChange();

        public void failedPictureChange();
    }

    /**
     * Be sure to use Application Context as the calling ativity might be
     * destryed
     * 
     * @param context
     * @param imageUri
     */
    public static void setProfilePicture(Context context, String imageUri) {
        setPicture(context, imageUri, ApiCalls.PROFILE_PICTURE_URI);
    }

    /**
     * Be sure to use Application Context as the calling ativity might be
     * destryed
     * 
     * @param context
     * @param imageUri
     */
    public static void setCoverPicture(Context context, String imageUri) {
        setPicture(context, imageUri, ApiCalls.COVER_PICTURE_URI);
    }

    private static void setPicture(Context context, String imageUri,
            final String destinationUrl) {

        BitmapLoaderListener listener = new BitmapLoaderListener() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                ContentHandler
                        .getInstance()
                        .getNetworkHandler()
                        .post(new AsynchronousPostCall(
                                createHttpEntity(bitmap), destinationUrl));
            }
        };

        new BitmapLoaderTask(context, listener).execute(imageUri);

    }

    private static class AsynchronousPostCall implements Runnable {
        private HttpEntity httpEntity;
        private String destinationUrl;

        public AsynchronousPostCall(HttpEntity httpEntity, String destinationUrl) {
            this.httpEntity = httpEntity;
            this.destinationUrl = destinationUrl;
        }

        @Override
        public void run() {
            try {
                Network network = NetworkUtilities.getNetwork();
                JsonObject json = network.makePostImageRequest(destinationUrl,
                        null, httpEntity).getAsJsonObject();
                JsonObject data = json.getAsJsonObject("data");

                User loggedInUser = ReadUser.requestLoggedInUser(null);
                ReadUser.fetchUser(loggedInUser); // Fetches the new user with
                                                  // updated pictures

                Log.d(TAG, "Post picture call successful");
            } catch (Exception e) {
                Log.e(TAG, "Failed to send picture", e);
                e.printStackTrace();
            }
        }

    }

    public static HttpEntity createHttpEntity(Bitmap image) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, BitmapUtils.JPEG_COMPRESSION_LEVEL,
                bos);
        ByteArrayInputStream bs = new ByteArrayInputStream(bos.toByteArray());

        InputStreamEntity httpEntity = new InputStreamEntity(bs, -1);

        return httpEntity;
    }

}
