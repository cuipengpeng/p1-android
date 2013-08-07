package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.FollowList;
import com.p1.mobile.p1android.content.FollowList.FollowListIOSession;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 *         All results of writing to content is seen in ContentChanged. Affected
 *         content is the FollowList of the logged in user
 */
public class WriteFollow {
    public static final String TAG = WriteFollow.class.getSimpleName();

    public static void toggleFollow(final String targetUserId) {
        final FollowList followingList = ContentHandler.getInstance()
                .getFollowing(NetworkUtilities.getLoggedInUserId(), null);
        final boolean followingAfterToggle;
        FollowListIOSession io = followingList.getIOSession();
        try {
            if (io.getUserIdList().contains(targetUserId)) {
                followingAfterToggle = false;
                io.removeUser(targetUserId);
            } else {
                followingAfterToggle = true;
                io.addUser(targetUserId);
            }
        } finally {
            io.close();
        }

        followingList.notifyListeners();

        ContentHandler.getInstance().getNetworkHandler().post(new Runnable() {
            @Override
            public void run() {
                String relationshipRequest = ReadContentUtil.netFactory
                        .createRelationshipRequest(targetUserId);

                try {
                    Network network = NetworkUtilities.getNetwork();

                    JsonObject sentJson = null;
                    if (followingAfterToggle) {
                        network.makePutRequest(relationshipRequest,
                            null, sentJson).getAsJsonObject();
                    } else {
                        network.makeDeleteRequest(relationshipRequest, null);
                    }

                    Log.d(TAG, "Follow/unfollow successful");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to follow/unfollow", e);
                }
            }
        });

    }

}
