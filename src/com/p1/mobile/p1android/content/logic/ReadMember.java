package com.p1.mobile.p1android.content.logic;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Member;
import com.p1.mobile.p1android.content.Member.MemberIOSession;
import com.p1.mobile.p1android.content.parsing.MemberParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadMember {
    public static final String TAG = ReadMember.class.getSimpleName();

    public static Member requestMember(String id, IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        Member member = ContentHandler.getInstance().getMember(id, requester);

        boolean memberIsValid = false;
        MemberIOSession io = member.getIOSession();
        try {
            memberIsValid = io.isValid();

        } finally {
            io.close();
        }
        if (!memberIsValid) {
            fetchMember(member);
        }
    
        return member;
    
    }

    private static void fetchMember(final Member member) {

        boolean noActiveRequest;
        MemberIOSession io = member.getIOSession();
        try {
            noActiveRequest = io.getLastAPIRequest() == 0;
            if (noActiveRequest) {
                io.refreshLastAPIRequest();
            }
        } finally {
            io.close();
        }

        if (noActiveRequest) {
            ContentHandler.getInstance().getLowPriorityNetworkHandler()
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            String memberId = null;
                            MemberIOSession io = member.getIOSession();
                            try {
                                memberId = io.getId();
                            } finally {
                                io.close();
                            }
                            String memberRequest = ReadContentUtil.netFactory
                                    .createGetSpecificMembersRequest(memberId);

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        memberRequest, null).getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");
                                JsonArray membersArray = data
                                        .getAsJsonArray("members");

                                Iterator<JsonElement> iterator = membersArray
                                        .iterator();
                                if (iterator.hasNext()) { // Will save the
                                                          // single returned
                                                          // member
                                    JsonObject memberJson = iterator.next()
                                            .getAsJsonObject();
                                    MemberParser.parseToMember(memberJson,
                                            member);
                                    member.notifyListeners();
                                }

                                Log.d(TAG,
                                        "All listeners notified as result of requestMember");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed getting member", e);
                            } finally {
                                io = member.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                            }
                        }
                    });
        }
    }

}
