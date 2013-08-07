package com.p1.mobile.p1android.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.p1.mobile.p1android.util.BitmapUtils;
import com.p1.mobile.p1android.util.PerformanceMeasure;

public class BatchUtil {
    public static final String TAG = BatchUtil.class.getSimpleName();

    public static final String DEFAULT_BOUNDRY = "MyBoundry";

    public static MultipartEntity createMultipartEntity() {
        return new MultipartEntity(HttpMultipartMode.STRICT, DEFAULT_BOUNDRY,
                Charset.defaultCharset());
    }

    @Deprecated
    public static void addImage(MultipartEntity multipartEntry,
            String imageUri, String imageId) {
        File imageFile;
        try {
            imageFile = new File(new URI(imageUri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        multipartEntry.addPart(imageId, new FileBody(imageFile, "image/jpeg"));
        Log.d(TAG,
                "Multipart entry after image addition: "
                        + multipartEntry.toString());
    }

    public static void addBitmap(MultipartEntity multipartEntity, Bitmap image,
            String imageId) {
        int measureId = PerformanceMeasure.startMeasure();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, BitmapUtils.JPEG_COMPRESSION_LEVEL,
                bos);
        ByteArrayInputStream bs = new ByteArrayInputStream(bos.toByteArray());

        InputStreamBody body = new InputStreamBody(bs, "image/jpeg", imageId);
        multipartEntity.addPart(imageId, body);
        PerformanceMeasure.endMeasure(measureId, "Jpeg compression time");

        Log.d(TAG,
                "Multipart entry after bitmap addition: "
                        + multipartEntity.toString());
    }

    public static void addJson(MultipartEntity multipartEntry, String json) {

        try {
            StringBody sb = new StringBody(json, "application/json",
                    Charset.defaultCharset());
            multipartEntry.addPart("json", sb);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG,
                "Multipart entry after json addition: "
                        + multipartEntry.toString());
    }

    public static void addJpeg(MultipartEntity multipartEntity,
            byte[] compressedJpeg, String imageId) {
        ByteArrayInputStream bs = new ByteArrayInputStream(compressedJpeg);

        InputStreamBody body = new InputStreamBody(bs, "image/jpeg", imageId);
        multipartEntity.addPart(imageId, body);
    }

}
