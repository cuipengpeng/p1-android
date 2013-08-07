package com.p1.mobile.p1android.test.net;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.entity.mime.MultipartEntity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.test.AndroidTestCase;
import android.util.Log;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.net.BatchUtil;

public class BatchTest extends AndroidTestCase {
    public static final String TAG = BatchTest.class.getSimpleName();

    private static final String[] IMAGES_PROJECTION = { Images.Media._ID,
            Images.Media.MINI_THUMB_MAGIC, Images.Media.BUCKET_DISPLAY_NAME,
            Images.Media.BUCKET_ID };
    private static final Uri MEDIA_STORE_CONTENT_URI = Images.Media.EXTERNAL_CONTENT_URI;
    private static final String IMAGES_ORDER_BY = Images.Media.DATE_ADDED
            + " desc";

    public void testInitialMultipart() {
        MultipartEntity multipart = BatchUtil.createMultipartEntity();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            multipart.writeTo(outStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Log.d(TAG, "multipart is: " + outStream.toString());
        assertTrue(true);
    }

    public void testMultipartWithShare() {
        MultipartEntity multipart = BatchUtil.createMultipartEntity();
        String sentString = "{My invalid json:}";
        BatchUtil.addJson(multipart, sentString);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            multipart.writeTo(outStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "multipart share is: " + outStream.toString());
        assertTrue(outStream.toString().contains(sentString)
                && outStream.toString().contains(
                        "Content-Type: application/json"));
    }

    public void testMultipartWithImage() {
        MultipartEntity multipart = BatchUtil.createMultipartEntity();

        Uri imageUri = Uri.parse("android.resource://com.p1.mobile.p1android/"
                + R.drawable.emot_1);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext()
                    .getContentResolver(), imageUri);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            assertTrue(false);
        } catch (IOException e1) {
            e1.printStackTrace();
            assertTrue(false);
        }
        Log.d(TAG, "Static image uri is " + imageUri.toString());

        BatchUtil.addBitmap(multipart, bitmap, "-1");

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            multipart.writeTo(outStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "multipart share is: " + outStream.toString());
        assertTrue(outStream.toString().contains("Content-Type: image/jpeg")
                && outStream.toString().contains("filename=\"-1\""));
    }

}
