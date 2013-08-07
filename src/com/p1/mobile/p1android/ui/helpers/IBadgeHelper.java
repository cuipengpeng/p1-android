/**
 * IBadgeHelper.java
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
public interface IBadgeHelper {
    void incrementBadge(BadgeView badge);

    void decrementBadge(BadgeView badge);

    void setBadge(BadgeView badge, int count);
}
