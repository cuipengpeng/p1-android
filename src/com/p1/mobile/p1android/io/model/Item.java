/**
 * Item.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.io.model;

import com.p1.mobile.p1android.io.model.tags.TagEntity;

/**
 * @author Viktor Nyblom
 * 
 */
public class Item implements TagEntity{

    private String name;
    private int category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    @Override
    public String getTagTitle() {
        return getName();
    }

}
