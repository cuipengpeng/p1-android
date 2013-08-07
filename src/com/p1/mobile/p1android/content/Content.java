package com.p1.mobile.p1android.content;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * 
 * At any given time, only one Content object of a given IdType exists in
 * memory. This behavior is maintained using the ContentHandler class.
 * 
 * Content is designed to be thread-safe. Reads and writes are done using
 * getIOSession() followed by IOSession.close()
 * 
 * @author Anton
 * 
 */
public abstract class Content {
    public static final String TAG = Content.class.getSimpleName();

    public static final String TYPE = "undefined";

    private Lock lock = new ReentrantLock();
    private Collection<IContentRequester> listeners = new HashSet<IContentRequester>(
            2);
    /** The session used for all read and write to the Content */
    protected ContentIOSession IOSession;

    /** Last time we got reliable information from the API */
    private long lastUpdateMillis;
    /**
     * Last time the user altered the content. Recent user modifications should
     * prevent further contradictory updates
     */
    private long lastUserModification;
    /**
     * Time when last api request was sent. If no api request is active, this
     * variable may be 0
     */
    private long lastAPIRequest;

    /** Amount of modifications that have priority over other modifications. */
    private int unfinishedUserModifications;

    private boolean hasFailedNetworkOperation = false;

    /**
     * Once Content has enough information for the app to display it's
     * information, the Content is valid.
     */
    private boolean isValid;

    // API information variables:
    /** json: id: 9 */
    private String id;
    /** json: created_time: "2011-09-21T16:42:04+0800" */
    private Date createdTime;
    /** json: etag: "6fd6af1777820151c16c69ee956a59ad" */
    private String etag;

    protected Content(String id) {
        this.id = id;
        isValid = false;
    }

    protected void addListeners(Content otherContent) {
        listeners.addAll(otherContent.listeners);
    }

    protected void clearListeners() {
        listeners.clear();
    }

    protected Collection<IContentRequester> getListeners() {
        return listeners;
    }

    protected void addListener(IContentRequester requester) {
        synchronized (listeners) {
            listeners.add(requester);
            Log.d(TAG, "Added listener. Now have " + listeners.size()
                    + " listeners (" + getClass().getSimpleName() + " " + id
                    + ")");
        }
    }

    protected void removeListener(IContentRequester requester) {
        synchronized (listeners) {
            listeners.remove(requester);
            Log.d(TAG, "Removed listener. Now have " + listeners.size()
                    + " listeners (" + getClass().getSimpleName() + " " + id
                    + ")");
        }
    }

    /**
     * Will add a task to notify all listeners in the UI Thread. May be called
     * from any thread
     */
    public void notifyListeners() {
        Handler handler = new Handler(Looper.getMainLooper()); // TODO reuse
                                                               // handler
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                synchronized (listeners) {
                    for (IContentRequester requester : listeners) {
                        requester.contentChanged(Content.this);
                    }
                }
            }
        };
        boolean success = handler.post(runnable);
        if (success) {
            // Log.d(TAG,
            // "Posted notification for all listeners from "+this.toString()+" to "+listeners.size()+" listeners.");
            Log.d(TAG, "Posted notification for all listeners from content id "
                    + id + " to " + listeners.size() + " listeners.");
        } else {
            Log.e(TAG, "Failed to post notification for all listeners");
        }
    }

    public String getId() {
        ContentIOSession io = getIOSession();
        try {
            return io.getId();
        } finally {
            io.close();
        }
    }

    /**
     * Returns a thread-safe session for reading and writing to the Content
     * object. Subclasses should override this function and call it using super.
     * 
     * @return
     */
    public ContentIOSession getIOSession() {
        if (IOSession == null) {
            Log.e(TAG, "IOSession is null, should never happen");
        }
        @SuppressWarnings("unused")
        boolean lockSuccessful = false;
        try {
            lockSuccessful = lock.tryLock(1, TimeUnit.SECONDS); // Timeout to
                                                                // discover
                                                                // deadlocks
        } catch (InterruptedException e) {
            lockSuccessful = false;
            e.printStackTrace();
            Log.e(TAG,
                    "ERROR: Failed to lock for getting IOSession. Check for content reads that forget to call io.Close()");
            return null;
        }

        return IOSession;
    }

    /**
     * 
     * You should never keep a reference to a ContentIOSession after you call
     * it's close() function. Aim for quickly making modifications and then
     * close() the ContentIOSession after use to allow other threads to use the
     * Content.
     * 
     * @author Anton
     */
    public abstract class ContentIOSession {
        public String getType() {
            if (TYPE.equals("undefined")) {
                Log.e(TAG, "A subclass of Content does not override TYPE");
            }
            return TYPE;
        }

        /**
         * Goes through all internal lists and replaces any occurrence of the
         * old id with the new one.
         * 
         * @param oldId
         * @param newId
         */
        public void replaceFakeId(String oldId, String newId) {

            // Should be overridden by subclasses
            throw new UnsupportedOperationException();
        }

        public String getId() {
            return id;
        }

        public void setId(String newId) {
            id = newId;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            Content.this.createdTime = createdTime;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag(String etag) {
            Content.this.etag = etag;
        }

        public long getLastUpdate() {
            return lastUpdateMillis;
        }

        public void refreshLastUpdate() {
            lastUpdateMillis = System.currentTimeMillis();
        }

        public long getLastUserModification() {
            return lastUserModification;
        }

        public void refreshLastUserModification() {
            lastUserModification = System.nanoTime();
        }

        public long getLastAPIRequest() {
            return lastAPIRequest;
        }

        public void refreshLastAPIRequest() {
            lastAPIRequest = System.nanoTime();
        }

        public void clearLastAPIRequest() {
            lastAPIRequest = 0;
        }

        public void incrementUnfinishedUserModifications() {
            unfinishedUserModifications++;
        }

        public void decrementUnfinishedUserModifications() {
            unfinishedUserModifications--;
        }

        public int getUnfinishedUserModifications() {
            return unfinishedUserModifications;
        }

        /**
         * 
         * @return true if it makes sense to read the content and display it to
         *         the user
         */
        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean isValid) {
            Content.this.isValid = isValid;
        }

        /**
         * Ideally, call this function inside the finally{} block of a
         * try-catch.
         */
        public void closeAfterModification() {
            refreshLastUpdate();
            close();
        }

        /**
         * Ideally, call this function inside the finally{} block of a
         * try-catch.
         */
        public void close() {
			try {
				lock.unlock();
            } catch (IllegalMonitorStateException e) {
                // Crude way of bypassing the lock as this error only occurs
                // when no unlock has been made
                Log.e(TAG,
                        "Failed to close IOSession for " + IOSession.getType()
                                + ":" + IOSession.getId()
                                + "\nCheck for unclosed IOSessions");
				e.printStackTrace();
			}
        }

        /**
         * Whenever this method returns true, the user should be notified that
         * his request went wrong.
         * 
         * @return true if any network request related to the object has failed
         *         (Fetching API information or sending information).
         */
        public boolean hasFailedNetworkOperation() {
            return hasFailedNetworkOperation;
        }

        public void setHasFailedNetworkOperation(
                boolean hasFailedNetworkOperation) {
            Content.this.hasFailedNetworkOperation = hasFailedNetworkOperation;
        }
    }

    public String toString() {
        // This function exemplifies how to use ContentIOSession
        String ret = null;
        ContentIOSession session = getIOSession();
        try {
            ret = "{" + session.getId() + ", " + session.getType() + "}";
        } finally {
            session.close();
        }
        return ret;

    }

}
