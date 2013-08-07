package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Feed;
import com.p1.mobile.p1android.content.Feed.FeedIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.background.RetryRunnable;
import com.p1.mobile.p1android.content.parsing.FeedParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadFeed {
    public static final String TAG = ReadFeed.class.getSimpleName();

    public static Feed requestFeed(IContentRequester requester) {
        Log.d(TAG, "Requested feed");
        Feed feed = ContentHandler.getInstance().getFeed(requester);

        fillFeed(feed);

        return feed;
    }

    public static Feed requestRefreshedFeed(IContentRequester requester) {
        Log.d(TAG, "Refreshed feed");
        Feed feed = ContentHandler.getInstance().getFeed(requester);

        FeedIOSession io = feed.getIOSession();
        try {
            io.resetOnNextUpdate();
        } finally {
            io.close();
        }
        fillFeed(feed);

        return feed;
    }

    /**
     * Will retrieve more feed information using pagination, and notify all
     * IContentRequesters of any successful changes. It is safe to call this
     * method quickly a large number of times.
     * 
     * @param feed
     */
    public static void fillFeed(final Feed feed) {
        boolean shouldMakeNetworkCall = false;
        FeedIOSession io = feed.getIOSession();
        try {
            if (io.getLastAPIRequest() == 0 && io.hasMore()) {
                shouldMakeNetworkCall = true; // Fetch new information
                io.setHasFailedNetworkOperation(false);
            }
        } finally {
            io.close();
        }

        if (shouldMakeNetworkCall) {
            ContentHandler.getInstance().getNetworkHandler()
                    .post(new RetryRunnable() {
                        @Override
                        public void run() {
                            int paginationOffset, paginationLimit;
                            FeedIOSession io = feed.getIOSession();
                            try {
                                if (io.getLastAPIRequest() != 0) {
                                    return;
                                }
                                io.refreshLastAPIRequest();
                                paginationOffset = io.getPaginationNextOffset();
                                paginationLimit = io.getPaginationLimit();

                            } finally {
                                io.close();
                            }

                            String feedRequest;

                            feedRequest = ReadContentUtil.netFactory
                                    .createGetFeedRequest(paginationOffset,
                                            paginationLimit);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject jsonResponse = network
                                        .makeGetRequest(feedRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = jsonResponse
                                        .getAsJsonObject("data");
                                JsonArray pictureArray = data
                                        .getAsJsonArray("pictures");
                                ReadContentUtil.saveExtraPictures(pictureArray);
                                JsonArray venueArray = data
                                        .getAsJsonArray("venues");
                                ReadContentUtil.saveExtraVenues(venueArray);
                                JsonArray usersArray = data
                                        .getAsJsonArray("users");
                                ReadContentUtil.saveExtraUsers(usersArray);
                                JsonArray commentsArray = data
                                        .getAsJsonArray("comments");
                                ReadContentUtil
                                        .saveExtraComments(commentsArray);
                                JsonArray sharesArray = data
                                        .getAsJsonArray("shares");
                                ReadContentUtil.saveExtraShares(sharesArray);

                                FeedParser.appendToFeed(
                                        jsonResponse, feed);

                                Log.d(TAG,
                                        "All listeners notified as result of requestFeed");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed getting feed", e);
                                // retry(); Disabling retries as we notify
                                // listeners of network failure
                                io = feed.getIOSession();
                                try {
                                    io.setHasFailedNetworkOperation(true);
                                } finally {
                                    io.close();
                                }
                            } finally {
                                io = feed.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                                feed.notifyListeners();
                            }
                        }
                    });
        }
    }

}
