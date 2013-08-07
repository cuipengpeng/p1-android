/**
 * BadgeHelper.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.helpers;

import com.readystatesoftware.viewbadger.BadgeView;

/**
 * @author Viktor Nyblom
 * 
 */
public class BadgeHelper implements IBadgeHelper {
    @SuppressWarnings("unused")
    private static final String TAG = BadgeHelper.class.getSimpleName();

    public void incrementBadge(BadgeView badge) {
        badge.increment(1);
        if (!badge.isShown()) {
            badge.show();
        }
    }

    public void decrementBadge(BadgeView badge) {
        badge.decrement(1);
        if (badge.isShown() && getBadgeCount(badge) < 1) {
            badge.hide();
        }
    }

    public int getBadgeCount(BadgeView badge) {
        String text = badge.getText().toString();
        int count;
        try {
            count = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            count = 0;
        }
        return count;
    }

    @Override
    public void setBadge(BadgeView badge, int count) {
        if (count == 0) {
            badge.hide();
        } else {
            if (!badge.isShown()) {
                badge.show();
            }
        }
        badge.setText(String.valueOf(count));
    }

}