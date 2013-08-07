package com.p1.mobile.p1android.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class BatchReferences {

    // Hash maps contains <old_id, new_id>
    private HashMap<String, String> shareIdChanges = new HashMap<String, String>();
    private HashMap<String, String> pictureIdChanges = new HashMap<String, String>();
    private HashMap<String, String> tagIdChanges = new HashMap<String, String>();

    public HashMap<String, String> getShareIdChanges() {
        return shareIdChanges;
    }

    public HashMap<String, String> getPictureIdChanges() {
        return pictureIdChanges;
    }

    public HashMap<String, String> getTagIdChanges() {
        return tagIdChanges;
    }

    public Set<Entry<String, String>> getAllIdChanges(){
        Set<Entry<String, String>> returnedSet = new HashSet<Entry<String, String>>();
        returnedSet.addAll(shareIdChanges.entrySet());
        returnedSet.addAll(pictureIdChanges.entrySet());
        returnedSet.addAll(tagIdChanges.entrySet());
        return returnedSet;
    }

}
