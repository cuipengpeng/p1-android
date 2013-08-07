package com.p1.mobile.p1android.ui.listeners;


public interface NavigationListener {
    
    public void navigateToFeed();
    
    public void navigateToBrowse();
    
    public void navigateToCamera();
    
    public void navigateToFollowers();
    
    public void navigateToChat();
    
    public void navigateToNotifications();

    public void showNavigationBar(boolean show, boolean anim);

    public void showNavigationBar(boolean show);
}
