package com.p1.mobile.p1android.ui.listeners;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

/**
 * Orientation listener to remember the device's orientation when the user
 * presses the shutter button.
 * 
 * The orientation will be normalized to return it in steps of 90 degrees (0,
 * 90, 180, 270).
 * 
 * @author Viktor Nyblom
 */
public class CameraOrientationListener extends OrientationEventListener {
    static final String TAG = CameraOrientationListener.class.getSimpleName();
    private int currentNormalizedOrientation;
    private int rememberedNormalizedOrientation;
    private CameraOrientationChangeCallback mCallback;

    public interface CameraOrientationChangeCallback {
        public void onCameraRotate(int degrees);
    }

    public CameraOrientationListener(Context context) {
        super(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation != ORIENTATION_UNKNOWN) {
            int oldOrientation = currentNormalizedOrientation;
            currentNormalizedOrientation = normalize(orientation);

            if (oldOrientation != currentNormalizedOrientation) {
                mCallback.onCameraRotate(currentNormalizedOrientation);
            }
        }
    }

    public void setCallback(CameraOrientationChangeCallback callback) {
        mCallback = callback;
    }
    
    private int normalize(int degrees) {

        if (degrees > 315 || degrees <= 45) {
            return 0;
        }

        if (degrees > 45 && degrees <= 135) {
            return 90;
        }

        if (degrees > 135 && degrees <= 225) {
            return 180;
        }

        if (degrees > 225 && degrees <= 315) {
            return 270;
        }

        throw new RuntimeException("Unable to normalize image...");
    }

    public void rememberOrientation() {
        Log.d(TAG, "rememberOrientation");
        rememberedNormalizedOrientation = currentNormalizedOrientation;
    }

    public int getRememberedOrientation() {
        Log.d(TAG, "getRememberedOrientation");
        return rememberedNormalizedOrientation;
    }
}