package com.p1.mobile.p1android.ui.fragment;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.listeners.CameraFragmentListener;
import com.p1.mobile.p1android.ui.listeners.CameraOrientationListener;
import com.p1.mobile.p1android.ui.listeners.CameraOrientationListener.CameraOrientationChangeCallback;
import com.p1.mobile.p1android.ui.listeners.CancelShareListener;
import com.p1.mobile.p1android.ui.widget.TriStateToggleButton;
import com.p1.mobile.p1android.util.Utils;

public class CameraPreviewFragment extends Fragment implements
        SurfaceHolder.Callback, Camera.PictureCallback,
        CameraOrientationChangeCallback {
    static final String TAG = CameraPreviewFragment.class.getSimpleName();

    private static final int MAX_HEIGHT = 2000;
    private static final int MAX_WIDTH = 1800;

    private int mHeight;
    private int mWidth;

    private Camera mCamera;
    private boolean mHasFlash;
    private String mFlashMode;
    private boolean mHasSecondaryCamera;
    private boolean mHasAutoFocus;
    private int mCameraId;
    private SurfaceHolder mSurfaceHolder;
    private CameraFragmentListener mListener;
    private CancelShareListener mCancelListener;
    private CameraOrientationListener mOrientationListener;
    private int mDisplayOrientation;
    private int mLayoutOrientation;

    private ImageButton mTakePictureButton;

    public static CameraPreviewFragment newInstance() {
        return new CameraPreviewFragment();
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof CameraFragmentListener)) {
            throw new IllegalArgumentException(
                    "Activity has to implement CameraFragmentListener");
        }

        mListener = (CameraFragmentListener) activity;

        if (!(activity instanceof CancelShareListener)) {
            throw new IllegalArgumentException(
                    "Activity has to implement CancelShareListener");
        }

        mCancelListener = (CancelShareListener) activity;

        mHasFlash = Utils.hasFlash(activity);

        mHasSecondaryCamera = (Camera.getNumberOfCameras() >= 2);

        mOrientationListener = new CameraOrientationListener(activity);
        mOrientationListener.setCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_fragment_layout,
                container, false);
        final SurfaceView surfaceView = (SurfaceView) view
                .findViewById(R.id.cameraSurfaceView);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        mTakePictureButton = (ImageButton) view
                .findViewById(R.id.cameraTakePictureButton);
        mTakePictureButton.setOnClickListener(new OnTakePictureClickListener());

        final ImageButton showGalleriesButton = (ImageButton) view
                .findViewById(R.id.cameraShowGalleryButton);
        showGalleriesButton
                .setOnClickListener(new OnShowGalleriesClickListener());

        final ImageButton cancelShareButton = (ImageButton) view
                .findViewById(R.id.cameraCancelButton);
        cancelShareButton.setOnClickListener(new OnCancelClickListener());

        final ImageButton flashButton = (ImageButton) view
                .findViewById(R.id.cameraFlashButton);
        if (mHasFlash) {
            flashButton.setOnClickListener(new OnFlashClickListener());
        } else {
            flashButton.setVisibility(View.GONE);
        }

        final ImageButton switchBackFrontButton = (ImageButton) view
                .findViewById(R.id.cameraFrontBackSwitchButton);
        if (mHasSecondaryCamera) {
            switchBackFrontButton
                    .setOnClickListener(new OnSwitchCameraClickListener());
        } else {
            switchBackFrontButton.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mOrientationListener.enable();

        try {
            Log.d(TAG, "Try Open camera " + mCameraId);
            mCamera = Camera.open(mCameraId);
            mSurfaceHolder.addCallback(this);
        } catch (Exception exception) {
            Log.e(TAG, "Can't open camera", exception);

            mListener.onCameraError();
            return;
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        stopCameraPreview();

        mCamera.release();

        mOrientationListener.disable();
    }

    private synchronized void startCameraPreview() {
        determineDisplayOrientation();

        setHasAutoFocus();

        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size previewSize = getBestPreviewSize(mHeight, mWidth,
                parameters);
        Camera.Size pictureSize = getBestPictureSize(mHeight, mWidth,
                parameters);

        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            Log.d(TAG, "previewSize.width " + previewSize.width
                    + " previewSize.height " + previewSize.height);
        }

        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            Log.d(TAG, "pictureSize.width " + pictureSize.width
                    + " pictureSize.height " + pictureSize.height);
        }
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);

            mCamera.startPreview();
        } catch (IOException exception) {
            Log.e(TAG, "Can't start camera preview due to IOException",
                    exception);

            mListener.onCameraError();
        }
    }

    private synchronized void stopCameraPreview() {
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mOrientationListener.disable();
            mSurfaceHolder.removeCallback(this);
        } catch (Exception exception) {
            Log.i(TAG, "Exception during stopping camera preview");
        }
    }

    private void takePicture() {
        mOrientationListener.rememberOrientation();

        Parameters params = mCamera.getParameters();
        params.setFlashMode(mFlashMode);
        if (mHasAutoFocus) {
            params.setFlashMode(Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(params);

        mCamera.autoFocus(myAutoFocusCallback);
    }

    private void cancelShare() {
        mCancelListener.onCancelShare();
    }

    private void showGalleries() {
        mListener.onShowGalleries();
    }

    private void toggleFlash(int state) {
        switch (state) {
        case 0:
            mFlashMode = Parameters.FLASH_MODE_AUTO;
            break;
        case 1:
            mFlashMode = Parameters.FLASH_MODE_ON;
            break;
        case 2:
            mFlashMode = Parameters.FLASH_MODE_OFF;
            break;
        }
    }

    @TargetApi(9)
    private void switchCamera() {
        Log.d(TAG, "switchCamera " + mCameraId);
        mTakePictureButton.setEnabled(false);
        stopCameraPreview();

        if (mCameraId == CameraInfo.CAMERA_FACING_BACK) {
            mCameraId = CameraInfo.CAMERA_FACING_FRONT;
            Log.d(TAG, "switchCamera back " + mCameraId);
        } else {
            mCameraId = CameraInfo.CAMERA_FACING_BACK;
            Log.d(TAG, "switchCamera front " + mCameraId);
        }
        mCamera = Camera.open(mCameraId);
        mSurfaceHolder.addCallback(this);

        mOrientationListener.enable();
        startCameraPreview();
        mTakePictureButton.setEnabled(true);
    }

    private Camera.Size determineBestSize(int width, int height,
            List<Camera.Size> sizes, boolean forPreview) {
        Camera.Size result = null;
        boolean firstCheck = false;
        Log.d(TAG, "Screen size height " + height + " width" + width);
        for (Camera.Size size : sizes) {
            boolean screenSizeCheck = forPreview ? (width >= size.width && height >= size.height)
                    : true;

            Log.d(TAG, "size height" + size.height + " width " + size.width);
            if ((size.width <= MAX_WIDTH && size.height <= MAX_HEIGHT)
                    && screenSizeCheck) {
                if (result == null) {
                    result = size;
                    firstCheck = true;
                }
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;
                double newRatio = (double) size.width / (double) size.height;

                boolean ratioCheck = true;

                ratioCheck = (newRatio >= 1.3 && newRatio <= 1.4);
                boolean areaCheck = firstCheck ? true : newArea > resultArea;
                Log.d(TAG, "Ratiocheck " + ratioCheck);
                if (areaCheck && ratioCheck) {
                    result = size;
                    firstCheck = false;
                }

            }
        }
        Log.d(TAG, "Sent height width " + height + " " + width);
        Log.d(TAG, "Best hight width  " + result.height + " " + result.width);

        return (result);
    }

    private Camera.Size getBestPreviewSize(int width, int height,
            Camera.Parameters parameters) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();

        return determineBestSize(width, height, sizes, true);
    }

    private Camera.Size getBestPictureSize(int width, int height,
            Camera.Parameters parameters) {
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();

        return determineBestSize(width, height, sizes, false);
    }

    private void setHasAutoFocus() {
        List<String> listFocusModes = mCamera.getParameters()
                .getSupportedFocusModes();
        mHasAutoFocus = false;

        for (String focusMode : listFocusModes) {
            Log.e(TAG, "focus mode " + focusMode);
            if (focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                mHasAutoFocus = true;
                break;
            }
        }
    }

    @SuppressLint("NewApi")
    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);

        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;

        switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break;

        case Surface.ROTATION_90:
            degrees = 90;
            break;

        case Surface.ROTATION_180:
            degrees = 180;
            break;

        case Surface.ROTATION_270:
            degrees = 270;
            break;
        }

        int displayOrientation;

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            Log.d(TAG, "First pass " + displayOrientation);
            displayOrientation = (360 - displayOrientation) % 360;
            Log.d(TAG, "Second pass " + displayOrientation);
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mDisplayOrientation = displayOrientation;
        mLayoutOrientation = degrees;

        Log.d(TAG, "rotaion " + rotation + " displayOrientation "
                + displayOrientation + " layout orientation "
                + mLayoutOrientation);
        mCamera.setDisplayOrientation(displayOrientation);

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                options);

        Log.d(TAG, "out h " + options.outHeight + " w " + options.outWidth);
        int rotation = (mDisplayOrientation
                + mOrientationListener.getRememberedOrientation() + mLayoutOrientation) % 360;

        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (360 - rotation) % 360;
        }
        Log.d(TAG, " " + rotation);
        if (rotation != 0) {
            Bitmap oldBitmap = bitmap;

            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);

            oldBitmap.recycle();
        }

        mListener.onPictureTaken(bitmap);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        mHeight = height;
        mWidth = width;
        toggleFlash(0);
        startCameraPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // NOOP
    }

    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean isFocused, Camera camera) {
            Log.e(TAG, "is focused " + isFocused);

            mCamera.takePicture(null, null, CameraPreviewFragment.this);

            mTakePictureButton.setEnabled(true);
        }
    };

    ShutterCallback myShutterCallback = new ShutterCallback() {

        @Override
        public void onShutter() {
            // TODO Play sound or do something fancy

        }
    };

    // ////////////////////
    // Listeners
    // ////////////////////

    private class OnTakePictureClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            mTakePictureButton.setEnabled(false);
            takePicture();
        }

    }

    private class OnShowGalleriesClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            showGalleries();
        }

    }

    private class OnCancelClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            cancelShare();
        }

    }

    private class OnFlashClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            int state = ((TriStateToggleButton) v).getState();
            toggleFlash(state);
        }

    }

    private class OnSwitchCameraClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switchCamera();
        }

    }

    @Override
    public void onCameraRotate(int degrees) {
        determineDisplayOrientation();
    }

}
