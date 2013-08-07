package com.p1.mobile.p1android.content.logic;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.util.Log;

import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Content.ContentIOSession;

public class FakeIdTracker {
    public static final String TAG = FakeIdTracker.class.getSimpleName();

    private Hashtable<String, List<Content>> contentTracker = new Hashtable<String, List<Content>>();

    public void track(String fakeId, Content content) {
        if (fakeId != null) {

            if (!contentTracker.containsKey(fakeId)) {
                contentTracker.put(fakeId, new ArrayList<Content>());
            }
            contentTracker.get(fakeId).add(content);
        }
        Log.d(TAG, "Tracking fake id " + fakeId);
    }

    public void update(String fakeId, String newId) {
        Set<Entry<String, String>> idChanges = new HashSet<Entry<String, String>>();
        Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(fakeId, newId);
        idChanges.add(entry);
        update(idChanges);
        Log.d(TAG, "Updated single id " + fakeId + " to " + newId);
    }

    /**
     * Atomically updates all changed Ids
     * 
     * @param references
     */
    public void update(Set<Entry<String, String>> idChanges) {
        int openIOSessionCount = 0;
        Hashtable<String, List<ContentIOSession>> activeIOSessions = new Hashtable<String, List<ContentIOSession>>();
        //Open all IOSessions
        for(Entry<String, String> change : idChanges){
            String fakeId = change.getKey();
            String newId = change.getValue();
            ArrayList<ContentIOSession> openedIOSessions = new ArrayList<ContentIOSession>();
            for(Content affectedContent : contentTracker.get(fakeId)){
                openedIOSessions.add(affectedContent.getIOSession());
                openIOSessionCount++;
            }
            activeIOSessions.put(newId, openedIOSessions);
        }
        Log.d(TAG, "Replacing id's in " + openIOSessionCount + " objects");
        // Replace all Ids
        for (Entry<String, String> change : idChanges) {
            String fakeId = change.getKey();
            String newId = change.getValue();
            for (ContentIOSession openIOSession : activeIOSessions.get(newId)) {
                openIOSession.replaceFakeId(fakeId, newId);
            }
        }
        // Close all IOSessions
        for (List<ContentIOSession> openIOSessions : activeIOSessions.values()) {
            for (ContentIOSession io : openIOSessions) {
                io.close();
                openIOSessionCount--;
            }
        }
        if (openIOSessionCount == 0) {
            Log.d(TAG, "Successfully replaced all id's");
        } else {
            Log.e(TAG,
                    "Wrong amount of unclosed IOSessions after id replacement: "
                            + openIOSessionCount);
        }

    }


}
