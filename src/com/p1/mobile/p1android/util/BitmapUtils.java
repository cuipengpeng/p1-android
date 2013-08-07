package com.p1.mobile.p1android.util;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

public class BitmapUtils {
    public static final int JPEG_COMPRESSION_LEVEL = 60;

    private static final int CACHE_DIVIDER = 8;
    public final static int MAX_IMAGE_DIMENSION = 1280 * 2; // TODO better
                                                            // decision
    // on maximum image
    // dimension
    public final static int TARGET_IMAGE_SAVE_SIZE = 1280;
    public final static int THUMBNAIL_SIZE = 180;
    private static final String TAG = BitmapUtils.class.getSimpleName();

    public static Bitmap drawViewOntoBitmap(View view) {
        Bitmap image = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(image);
        view.draw(canvas);
        return image;
    }

    /**
     * Found at
     * http://stackoverflow.com/questions/8327846/how-to-resize-a-bitmap
     * -eficiently-and-with-out-losing-quality-in-android
     * 
     * @param bitmap
     * @param newHeight
     * @param newWidth
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap bitmap, int newWidth,
            int newHeight) {
        int perfId = PerformanceMeasure.startMeasure();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, false);

        PerformanceMeasure.endMeasure(perfId, TAG + " getResizedBitmap");
        return resizedBitmap;
    }

    public static Bitmap getDefaultSizeBitmap(Bitmap bitmap) {
        Point targetSize = determineSaveSize(bitmap.getWidth(),
                bitmap.getHeight());
        return getResizedBitmap(bitmap, targetSize.x, targetSize.y);
    }

    /**
     * Crops the bitmap to 180*180
     * 
     * @param bitmap
     * @return
     */
    public static Bitmap getBitmapThumbnail(Bitmap bitmap) {
        int perfId = PerformanceMeasure.startMeasure();
        final int targetSize = THUMBNAIL_SIZE;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale;
        Bitmap resizedBitmap;
        Matrix matrix = new Matrix();
        if (width < height) {
            scale = ((float) targetSize) / width;
            matrix.postScale(scale, scale);
            int usedHeight = (int) (targetSize / scale);
            int heightMargin = (height - usedHeight) / 2;
            resizedBitmap = Bitmap.createBitmap(bitmap, 0, heightMargin, width,
                    usedHeight, matrix, false);
        } else {
            scale = ((float) targetSize) / height;
            matrix.postScale(scale, scale);
            int usedWidth = (int) (targetSize / scale);
            int widthMargin = (width - usedWidth) / 2;
            resizedBitmap = Bitmap.createBitmap(bitmap, widthMargin, 0,
                    usedWidth, height, matrix, false);
        }
        PerformanceMeasure.endMeasure(perfId, TAG + " getBitmapThumbnail");

        return resizedBitmap;
    }

    /**
     * Found at
     * http://stackoverflow.com/questions/3647993/android-bitmaps-loaded
     * -from-gallery-are-rotated-in-imageview
     * 
     * 
     * 
     * @param context
     * @param photoUri
     * @return
     * @throws IOException
     */
    public static Bitmap getCorrectlyOrientedImage(Context context,
            String pictureUri, int size) throws IOException {
        int perfId = PerformanceMeasure.startMeasure();

        Uri uri = Uri.parse(pictureUri);

        InputStream is = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int orientation = getOrientation(context, uri);
        Log.d(TAG, "orientation " + orientation);
        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(uri);

        int originalSize = (dbo.outHeight > dbo.outWidth) ? dbo.outHeight
                : dbo.outWidth;
        Log.d(TAG, "original size " + originalSize);
        if (size > 0)
            Log.d(TAG, "size " + originalSize / size);

        if (size >= 1) {
            Log.d(TAG, "samplesize " + originalSize / size);
            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = originalSize / size;
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            Log.d(TAG, "samplesize " + 4);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
                    srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
        }

        Log.d(TAG,
                "Bitmap size: " + srcBitmap.getWidth() + 'x'
                        + srcBitmap.getHeight());

        PerformanceMeasure.endMeasure(perfId, TAG
                + " getCorrectlyOrientedBitmap ");
        return srcBitmap;
    }

    public static Bitmap rotateBitmapFromExif(Context context, Bitmap bitmap,
            String imageUri) throws FileNotFoundException {
        int perfId = PerformanceMeasure.startMeasure();
        Uri uri = Uri.parse(imageUri);

        int orientation = getOrientation(context, uri);

        Bitmap returnBitmap = bitmap;
        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            returnBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            bitmap.recycle();
        }

        Log.d(TAG, "Bitmap size: " + returnBitmap.getWidth() + 'x'
                + returnBitmap.getHeight());
        PerformanceMeasure.endMeasure(perfId, TAG + " rotateBitmapFromExif ");
        return returnBitmap;

    }

    /**
     * Found at
     * http://stackoverflow.com/questions/3647993/android-bitmaps-loaded
     * -from-gallery-are-rotated-in-imageview
     * 
     * @param context
     * @param photoUri
     * @return
     */
    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        int perfId = PerformanceMeasure.startMeasure();

        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
                null, null, null);
        if (cursor == null || cursor.getCount() != 1) {
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        }

        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        PerformanceMeasure.endMeasure(perfId, TAG + " getOrientation ");

        return orientation;
    }

    public static int getImageCacheSizeInKB() {
        final int memoryTotal = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = memoryTotal / CACHE_DIVIDER;
        Log.d(TAG, "Memory Total " + memoryTotal + " cacheSize " + cacheSize);
        return cacheSize;
    }

    /**
     * Scales the size to maintain aspect ratio and keep largest side equal or
     * less than BitmapUtils.TARGET_IMAGE_SAVE_SIZE
     * 
     * @param imageWidth
     * @param imageHeight
     * @return Point defining proper output width and height.
     */
    public static Point determineSaveSize(int imageWidth, int imageHeight) {

        if (imageWidth <= TARGET_IMAGE_SAVE_SIZE
                && imageHeight <= TARGET_IMAGE_SAVE_SIZE) {
            Log.d(TAG, "Determined save size is " + imageWidth + "x"
                    + imageHeight);
            return new Point(imageWidth, imageHeight);
        }
        if (imageWidth > imageHeight) {
            imageHeight = imageHeight * TARGET_IMAGE_SAVE_SIZE / imageWidth;
            imageWidth = TARGET_IMAGE_SAVE_SIZE;
        } else {
            imageWidth = imageWidth * TARGET_IMAGE_SAVE_SIZE / imageHeight;
            imageHeight = TARGET_IMAGE_SAVE_SIZE;
        }

        Log.d(TAG, "Determined save size is " + imageWidth + "x" + imageHeight);
        return new Point(imageWidth, imageHeight);

    }

    public static byte[] compressToJpegByteArray(Bitmap bitmap) {
        int measureId = PerformanceMeasure.startMeasure();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, JPEG_COMPRESSION_LEVEL, bos);

        PerformanceMeasure.endMeasure(measureId,
                "Jpeg compression time resulting in " + bos.size() + " bytes.");

        return bos.toByteArray();
    }
}
