package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.FollowList;
import com.p1.mobile.p1android.content.FollowList.FollowListIOSession;

public abstract class FollowListParser {
    public static final String TAG = FollowListParser.class.getSimpleName();
    
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String USERS = "users";
    public static final String PAGINATION = "pagination";
    public static final String PAGINATION_OFFSET = "offset";
    public static final String PAGINATION_TOTAL = "total";
    
    /**
     * 
     * @param json response json containing a list of json Users and pagination
     * @param followList
     * @return adds information to the follow list
     */
    public static boolean appendToFollowList(JsonObject json, FollowList followList){
        FollowListIOSession io = followList.getIOSession();
        try{
            JsonObject pagination = json.getAsJsonObject(PAGINATION);
            int responseOffset = pagination.getAsJsonPrimitive(PAGINATION_OFFSET).getAsInt();
            if(responseOffset != io.getPaginationNextOffset()){
                Log.e(TAG, "Pagination offset is off! Returned offset is "+responseOffset+", should be "+io.getPaginationNextOffset());
            }
            
            JsonArray jArr = json.getAsJsonObject(DATA).getAsJsonArray(USERS);
            Iterator<JsonElement> iterator = jArr.iterator();
            while(iterator.hasNext()){
                io.getUserIdList().add(iterator.next().getAsJsonObject().get(ID).getAsString());
                io.incrementOffset();
            }
            io.setPaginationTotal(json.getAsJsonObject(PAGINATION).getAsJsonPrimitive(PAGINATION_TOTAL).getAsInt());
            
            io.setValid(true);
        }
        finally{
            io.close();
        }
        return true;
    }
}
