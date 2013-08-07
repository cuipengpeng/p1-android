package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.FollowList;
import com.p1.mobile.p1android.content.FollowList.FollowListIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.background.RetryRunnable;
import com.p1.mobile.p1android.content.parsing.FollowListParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadFollow {
    public static final String TAG = ReadFollow.class.getSimpleName();

    /**
     * Get list of people that are following User id
     */
    public static FollowList requestFollowersList(String id,
            IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        FollowList followList = ContentHandler.getInstance().getFollowers(id,
                requester);

        fillFollowList(followList);

        return followList;
    }

    /**
     * 
     * @param requester
     * @return the followers list of the currently logged in user
     */
    public static FollowList requestLoggedInFollowersList(
            IContentRequester requester) {
        return requestFollowersList(NetworkUtilities.getLoggedInUserId(),
                requester);
    }

    /**
     * Get list of people that User id is following
     */
    public static FollowList requestFollowingList(String id,
            IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        FollowList followList = ContentHandler.getInstance().getFollowing(id,
                requester);

        ReadFollow.fillFollowList(followList);

        return followList;
    }

    /**
     * 
     * @param requester
     * @return the following list of the currently logged in user
     */
    public static FollowList requestLoggedInFollowingList(
            IContentRequester requester) {
        return requestFollowingList(NetworkUtilities.getLoggedInUserId(),
                requester);
    }

    /**
     * Will retrieve all information of a specific FollowList using pagination,
     * and notify all IContentRequesters of any successful changes. It is safe
     * to call this method quickly a large number of times.
     * 
     * This method is used for both followers and following
     * 
     * @param followList
     */
    public static void fillFollowList(final FollowList followList) {

        boolean shouldMakeRequest;
        FollowListIOSession io = followList.getIOSession();
        try {
            boolean noActiveRequest = io.getLastAPIRequest() == 0;
            shouldMakeRequest = noActiveRequest && io.hasMore();
            if (shouldMakeRequest) {
                io.refreshLastAPIRequest();
                io.setHasFailedNetworkOperation(false);
            }
        } finally {
            io.close();
        }

        if (shouldMakeRequest) {
            ContentHandler.getInstance().getNetworkHandler()
                    .post(new RetryRunnable() {
                        @Override
                        public void run() {
                            String listId = null;
                            int paginationOffset, paginationLimit;
                            FollowListIOSession io = followList.getIOSession();
                            String contentType;
                            try {
                                listId = io.getId();
                                contentType = io.getType();
                                paginationOffset = io.getPaginationNextOffset();
                                paginationLimit = io.getPaginationLimit();
                            } finally {
                                io.close();
                            }
                            String followRequest;
                            if (contentType.equals(FollowList.FOLLOWERS)) {
                                followRequest = ReadContentUtil.netFactory
                                        .createGetFollowersRequest(listId,
                                                paginationOffset,
                                                paginationLimit);
                            } else {
                                followRequest = ReadContentUtil.netFactory
                                        .createGetFollowingRequest(listId,
                                                paginationOffset,
                                                paginationLimit);
                            }

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        followRequest, null).getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");
                                JsonArray userArray = data
                                        .getAsJsonArray("users");
                                ReadContentUtil.saveExtraUsers(userArray);
                                FollowListParser.appendToFollowList(object,
                                        followList);

                                followList.notifyListeners();

                                io = followList.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                    fillFollowList(followList); // Keep filling
                                } finally {
                                    io.close();
                                }

                                Log.d(TAG,
                                        "All listeners notified as result of requestFollowList");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        "Failed getting follow list", e);
                                io = followList.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                                retry();
                            }
                        }

                        @Override
                        protected void failedLastRetry() {
                            super.failedLastRetry();
                            FollowListIOSession io = followList.getIOSession();
                            try {
                                io.setHasFailedNetworkOperation(true);
                            } finally {
                                io.close();
                            }
                            followList.notifyListeners();
                        }
                    });
        }
    }

}
