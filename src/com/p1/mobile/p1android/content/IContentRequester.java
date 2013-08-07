package com.p1.mobile.p1android.content;

/**
 * 
 * @author Anton
 * Anything that requests content for displaying to the user should implement the IContentRequester.
 * Before an IContentRequester is destroyed it should call ContentHandler.removeRequester(this)
 */
public interface IContentRequester {
    public void contentChanged(Content content);

    public interface IChildContentRequester {
        public void removeChildRequestors();
    }

    public interface IhasTimers {
        public void removetimer();
    }

}
