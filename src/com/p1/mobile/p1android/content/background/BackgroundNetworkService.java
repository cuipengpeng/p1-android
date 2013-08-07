package com.p1.mobile.p1android.content.background;

import java.util.Queue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.logic.ReadAccount;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 *         Handles background network fetching such as polling and retrying
 *         failed requests.
 */
public class BackgroundNetworkService extends Service {
    public static final String TAG = BackgroundNetworkService.class
            .getSimpleName();
    private static final long SECOND = 1000l;
    private static final long MINUTE = 60 * SECOND;
    private static final long BASE_ACCOUNT_REQUEST_DELAY = 1 * SECOND;
    private static final long MAX_ACCOUNT_REQUEST_DELAY = 30 * MINUTE;
    
    private static final long CONVERSATION_DEFAULT_DELAY = 15 * SECOND;

    private static final long BUNDLED_POLL_DELAY = 15 * SECOND;

    private static final long POLL_DELAY_GAIN_PERCENT = 40;

    public static final String START_CODE = "background_code";
    public static final int CODE_SUCCESSFUL_REQUEST = 0;
    /**
     * An action that makes it likely for other users to generate messages and
     * notifications for you
     */
    public static final int CODE_INCREASE_POLLING = 1;
    public static final int CODE_APPLICATION_IN_BACKGROUND = 2;
    public static final int CODE_APPLICATION_IN_FOREGROUND = 3;
    public static final int CODE_ENTER_CONVERSATIONS = 4;
    public static final int CODE_ENTER_MESSAGES = 5;
    public static final int CODE_LEAVE_CONVERSATIONS = 6;
    public static final int CODE_LEAVE_MESSAGES = 7;

    public static boolean POLLING_MESSAGES_ENABLED = false;

    public boolean insideConversations = false;
    public boolean insideMessages = false;

    private long timeOfLastAccountRequest;

    private long timeToNextAccountRequest = BASE_ACCOUNT_REQUEST_DELAY;

    private Account account;
    private PollerThread pollerThread;

    public void onCreate() {
        account = ContentHandler.getInstance().getAccount(null);

        pollerThread = new PollerThread();
        pollerThread.start();

        prefetchInformation();
        Log.d(TAG, "BackgroundNetworkService created");
    }

    @Override
    public void onDestroy() {
        pollerThread.running = false;
        Log.d(TAG, "BackgroundNetworkService destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int code = intent.getIntExtra(START_CODE, CODE_SUCCESSFUL_REQUEST);
        Log.d(TAG, "Start command code: " + code);
        switch (code) {
        case CODE_SUCCESSFUL_REQUEST:
            reportSuccessfulNetworkRequest();
            break;
        case CODE_INCREASE_POLLING:
            reportSignificantAction();
            break;
        case CODE_ENTER_CONVERSATIONS:
            insideConversations = true;
            break;
        case CODE_ENTER_MESSAGES:
            insideMessages = true;
            break;
        case CODE_LEAVE_CONVERSATIONS:
            insideConversations = false;
            break;
        case CODE_LEAVE_MESSAGES:
            insideMessages = false;
            break;
        }



        return START_NOT_STICKY;
    }

    /**
     * Will start making pending and unimportant requests in combination with
     * the successful one as bundled requests consume much less battery compared
     * to constant polling.
     */
    public void reportSuccessfulNetworkRequest() {
        // The delay is set lower to bundle account requests with other
        // requests.
        if ((BUNDLED_POLL_DELAY * getNetworkTypeMultiplier()) < timeSinceLastAccountUpdate()) {
            Log.d(TAG,
                    "Polling for account in combination with successful requests. The delay was "
                    + timeToNextAccountRequest);
            fetchAccount();
        }
        retryFailedNetworkRequests();
    }

    private void retryFailedNetworkRequests() {
        Queue<Runnable> failedRequests = ContentHandler.getInstance().getFailedNetworkRequests();
        Log.d(TAG, "Retrying " + failedRequests.size() + " requests");
        while (!failedRequests.isEmpty()) {
            Runnable retry = failedRequests.poll();
            ContentHandler.getInstance().getLowPriorityNetworkHandler()
                    .post(retry);
        }
    }

    /**
     * Report anything that will cause others to be likely to generate actions.
     * Examples are posting shares and sending/receiving messages.
     */
    public void reportSignificantAction() {
        timeToNextAccountRequest = BASE_ACCOUNT_REQUEST_DELAY;
    }

    public void reportFailedNetworkRequest(Runnable request) {

    }

    /**
     * makes all calls preparing views with basic information before it is
     * requested.
     */
    public void prefetchInformation() {
        ReadAccount.fetchAccount();
    }

    private long timeSinceLastAccountUpdate() {
        if (Math.abs(System.currentTimeMillis() - timeOfLastAccountRequest) < SECOND) {
            return SECOND;
        }
        AccountIOSession io = account.getIOSession();
        try {
            if (POLLING_MESSAGES_ENABLED)
                Log.d(TAG,
                        "Time since update is "
                                + (System.currentTimeMillis() - io
                                        .getLastUpdate()));
            return System.currentTimeMillis() - io.getLastUpdate();
        } finally {
            io.close();
        }
    }

    private long getDelayToNextAccountRequest() {

        if (insideConversations || insideMessages) {
            return Math.min(CONVERSATION_DEFAULT_DELAY,
                    timeToNextAccountRequest) * getNetworkTypeMultiplier();
        }
        return timeToNextAccountRequest * getNetworkTypeMultiplier();
    }

    private class PollerThread extends Thread {

        public volatile boolean running = true;

        @Override
        public void run() {
            while (running) {
                if (getDelayToNextAccountRequest() < timeSinceLastAccountUpdate()) {
                    if (POLLING_MESSAGES_ENABLED)
                        Log.v(TAG, "Polling for account. The delay was "
                            + timeToNextAccountRequest);
                    fetchAccount();
                }

                try {
                    // Sleep for 1/POLL_CHECK_FRACTION of the time between
                    // requests
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fetchAccount() {
        if (NetworkUtilities.getLoggedInUserId() != null) {
            ReadAccount.fetchAccount();
            updateAccountRequestDelay();
            timeOfLastAccountRequest = System.currentTimeMillis();
        } else {
            Log.w(TAG, "no user was found when polling for account");
            updateAccountRequestDelay();
            timeOfLastAccountRequest = System.currentTimeMillis();
        }

    }

    private void updateAccountRequestDelay() {
        timeToNextAccountRequest += timeToNextAccountRequest
                * POLL_DELAY_GAIN_PERCENT / 100;
        if (timeToNextAccountRequest > MAX_ACCOUNT_REQUEST_DELAY) {
            timeToNextAccountRequest = MAX_ACCOUNT_REQUEST_DELAY;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int getNetworkTypeMultiplier() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        if (activeNetworkInfo != null
                && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return 1;
        } else {
            return 2;
        }

    }

}
