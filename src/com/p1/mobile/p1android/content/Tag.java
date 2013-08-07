package com.p1.mobile.p1android.content;


import android.util.Log;

public class Tag extends Content {

    private static final String TYPE = "tag";

    private IdTypePair tagged;
    private IdTypePair parent;
    private String owner;

    private DoublePoint coords;

    protected Tag(String id) {
        super(id);
        IOSession = new TagIOSession();
        Log.d(TAG, "Tag " + id + " created");
    }

    @Override
    public TagIOSession getIOSession() {
        return (TagIOSession) super.getIOSession();
    }

    public class TagIOSession extends ContentIOSession {

        @Override
        public String getType() {
            return TYPE;
        }

        public IdTypePair getTagged() {
            return tagged;
        }

        public void setTagged(IdTypePair tagged) {
            Tag.this.tagged = tagged;
        }

        public IdTypePair getParent() {
            return parent;
        }

        public void setParent(IdTypePair parent) {
            Tag.this.parent = parent;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            Tag.this.owner = owner;
        }

        public DoublePoint getCoords() {
            return coords;
        }

        public void setCoords(DoublePoint coords) {
            Tag.this.coords = coords;
        }
    }

}
