/**
 * User.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.io.model;

import android.text.TextUtils;

import com.p1.mobile.p1android.io.model.tags.TagEntity;

@Deprecated
/**
 * @author Viktor Nyblom
 * 
 */
public class User implements TagEntity{
    private static final String EN_US = "en-US";
    private static final String ZH_CN = "zh-CN";
    private static final String UNKNOWN = "unknown";

    /**
     * @param id
     * @param type
     */
    public User(int id) {
        this.id = id;
    }

    public User() {
    }


    private String gender;

    // en-US name
    private String enUsFullname;
    private String enUsGivenName;
    private String enUsSurname;

    // zh-CN name
    private String zhCnFullname;
    private String zhCnGivenName;
    private String zhCnSurname;

    // Preferred language
    private String preferred;

    // Thumb 100
    private String profileThumb100Url;

    // Thumb 50
    private String profileThumb50Url;

    // Thumb 30
    private String profileThumb30Url;

    // Cover
    private String coverFormat;
    private String coverUrl;
    private int coverHeight;
    private int coverWidth;

    private String path;
    private String birthdate;
    private int id;

    /**
     * @return
     */
    public String getPreferredFullName() {
        if (preferred == null) {
            return UNKNOWN;
        }
        if (preferred.equalsIgnoreCase(EN_US)) {
            return enUsFullname;
        } else if (preferred.equalsIgnoreCase(ZH_CN)) {
            if (TextUtils.isEmpty(zhCnFullname)) {
                return enUsFullname;
            }
            return zhCnFullname;

        } else {
            return UNKNOWN;
        }
    }

    public int getUserId() {
        return id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEnUsFullname() {
        return enUsFullname;
    }

    public void setEnUsFullname(String enUsFullname) {
        this.enUsFullname = enUsFullname;
    }

    public String getEnUsGivenName() {
        return enUsGivenName;
    }

    public void setEnUsGivenName(String enUsGivenName) {
        this.enUsGivenName = enUsGivenName;
    }

    public String getEnUsSurname() {
        return enUsSurname;
    }

    public void setEnUsSurname(String enUsSurname) {
        this.enUsSurname = enUsSurname;
    }

    public String getZhCnFullname() {
        return zhCnFullname;
    }

    public void setZhCnFullname(String zhCnFullname) {
        this.zhCnFullname = zhCnFullname;
    }

    public String getZhCnGivenName() {
        return zhCnGivenName;
    }

    public void setZhCnGivenName(String zhCnGivenName) {
        this.zhCnGivenName = zhCnGivenName;
    }

    public String getZhCnSurname() {
        return zhCnSurname;
    }

    public void setZhCnSurname(String zhCnSurname) {
        this.zhCnSurname = zhCnSurname;
    }

    public String getPreferred() {

        return preferred;
    }

    public void setPreferred(String preferred) {
        this.preferred = preferred;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getProfileThumb100Url() {
        return profileThumb100Url;
    }

    public void setProfileThumb100Url(String profileThumb100Url) {
        this.profileThumb100Url = profileThumb100Url;
    }

    public String getProfileThumb50Url() {
        return profileThumb50Url;
    }

    public void setProfileThumb50Url(String profileThumb50Url) {
        this.profileThumb50Url = profileThumb50Url;
    }

    public String getProfileThumb30Url() {
        return profileThumb30Url;
    }

    public void setProfileThumb30Url(String profileThumb30Url) {
        this.profileThumb30Url = profileThumb30Url;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCoverFormat() {
        return coverFormat;
    }

    public void setCoverFormat(String coverFormat) {
        this.coverFormat = coverFormat;
    }

    public int getCoverHeight() {
        return coverHeight;
    }

    public void setCoverHeight(int coverHeight) {
        this.coverHeight = coverHeight;
    }

    public int getCoverWidth() {
        return coverWidth;
    }

    public void setCoverWidth(int coverWidth) {
        this.coverWidth = coverWidth;
    }

    @Override
    public String getTagTitle() {
        return getPreferredFullName();
    }
}
