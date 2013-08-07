package com.p1.mobile.p1android.content.background;

import android.util.Log;

import com.p1.mobile.p1android.content.ContentHandler;

public abstract class RetryRunnable implements Runnable {
    private static final String TAG = RetryRunnable.class.getSimpleName();
    private static final int MAX_RETRIES = 2;
    private int retryCount = 0;

    protected void retry() {
        retryCount++;
        if (retryCount > MAX_RETRIES) {
            failedLastRetry();
            return;
        }
        Log.d(TAG, "Retry attempt " + retryCount);
        ContentHandler.getInstance().getFailedNetworkRequests().add(this);

    }

    protected void failedLastRetry() {
        Log.w(TAG, "Final retry failed");
    }

}
