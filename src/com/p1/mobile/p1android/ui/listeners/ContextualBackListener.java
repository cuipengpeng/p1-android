package com.p1.mobile.p1android.ui.listeners;

/**
 * Interface supplies method for listening to fragments calling for a Back
 * action
 * 
 * @author Viktor Nyblom
 * 
 */
public interface ContextualBackListener {
    /**
     * Take a step back in the back stack
     */
    public void onContextualBack();
}
