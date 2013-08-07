package com.p1.mobile.p1android.io.model.tags;

import org.json.JSONException;
import org.json.JSONObject;

public class PictureTag implements SimpleTag {

    private int _id;
    private float mLocationX;
    private float mLocationY;

    private TagEntity mTagEntity;

    /**
     * Used by OrmLite
     */
    public PictureTag() {
    }
    
    public PictureTag(TagEntity entity, float locationX, float locationY) {
        this(locationX, locationY);
        mTagEntity = entity;
    }

    public PictureTag(float locationX, float locationY) {
        mLocationX = locationX;
        mLocationY = locationY;
    }

    public PictureTag(JSONObject object) throws JSONException {
        this((float) object.getDouble("x"), (float) object.getDouble("y"));
    }

    public PictureTag(float x, float y, float bitmapWidth, float bitmapHeight) {
        this(x / bitmapWidth, y / bitmapHeight);
    }

    public PictureTag(float x, float y, int bitmapWidth, int bitmapHeight) {
        this(x, y, (float) bitmapWidth, (float) bitmapHeight);
    }

    public final float getLocationX() {
        return mLocationX;
    }

    public void setLocationX(float locationX) {
        mLocationX = locationX;
    }

    public final float getLocationY() {
        return mLocationY;
    }

    public void setLocationY(float locationY) {
        mLocationY = locationY;
    }

    /**
     * Only used by OrmLite
     * 
     * @return
     * @throws JSONException
     */
    public JSONObject toJsonObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("x", mLocationX);
        object.put("y", mLocationX);
        return object;
    }

    @Override
    public String getTitle() {
        if (mTagEntity == null) {
            return "";
        }
        return mTagEntity.getTagTitle();
    }

    @Override
    public void setEntity(TagEntity entity) {
        mTagEntity = entity;
    }

    @Override
    public TagEntity getEntity() {
        return mTagEntity;
    }
}
