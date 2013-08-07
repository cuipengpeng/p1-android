package com.p1.mobile.p1android.content;

import java.util.Calendar;
import java.util.Date;

import android.util.Log;

import com.p1.mobile.p1android.util.ChineseRegexUtil;

public class User extends Content {
    public static final String TAG = User.class.getSimpleName();
    public static final String EN_US = "en-US";
    public static final String ZH_CN = "zh-CN";
    private static final String UNKNOWN = "unknown";
    public static final String MALE = "male";
    public static final String FEMALE = "female";

    public static final String TYPE = "user";

    // API information variables:
    private String gender;
    private String enUsFullname;
    private String enUsGivenName;
    private String enUsSurname;

    private String preferredLanguage;

    private String city;

    private String careerCompany;
    private String careerPosition;

    private String education;
    private String profileThumb100Url;

    private String profileThumb50Url;

    private String profileThumb30Url;

    private String coverUrl;
    private int coverHeight;
    private int coverWidth;

    private String path;
    private Date birthdate;

    protected User(String id) {
        super(id);
        IOSession = new UserIOSession();
        Log.d(TAG, "User " + id + " created");
    }

    @Override
    public UserIOSession getIOSession() {
        return (UserIOSession) super.getIOSession();
    }

    public class UserIOSession extends ContentIOSession {

        @Override
        public String getType(){
            return TYPE;
        }
        
        public String getPreferredFullName() {
//            if (preferredLanguage == null) {
//                return UNKNOWN;
//            }
//            if (preferredLanguage.equalsIgnoreCase(EN_US)) {
//                return enUsFullname;
//            } else if (preferredLanguage.equalsIgnoreCase(ZH_CN)) {
//                if (TextUtils.isEmpty(zhCnFullname)) {
//                    return enUsFullname;
//                }
//                return zhCnFullname.replace(" ", "");
//            } else {
//                return UNKNOWN;
//            }
            
            /** New logic to get user default name
             * Do not use preferedlanguage any more
             * If it is a Chinese name remove space between surname and givenname*/
            if (preferredLanguage == null) {
                return UNKNOWN;
            }
            if (ChineseRegexUtil.isContainsChinese(this.getEnUsGivenName()) &&
                    ChineseRegexUtil.isContainsChinese(this.getEnUsSurname())) {
                String nameString = this.getEnUsSurname() +  this.getEnUsGivenName();
                return nameString;
            }
            else {
            	return this.getEnUsGivenName() + " " + this.getEnUsSurname();
                //return enUsFullname;
            }
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            User.this.gender = gender;
        }

        public String getEnUsFullname() {
            return enUsFullname;
        }

        public void setEnUsFullname(String enUsFullname) {
            User.this.enUsFullname = enUsFullname;
        }

        public String getEnUsGivenName() {
            return enUsGivenName;
        }

        public void setEnUsGivenName(String enUsGivenName) {
            User.this.enUsGivenName = enUsGivenName;
        }

        public String getEnUsSurname() {
            return enUsSurname;
        }

        public void setEnUsSurname(String enUsSurname) {
            User.this.enUsSurname = enUsSurname;
        }

        public String getPreferredLanguage() {
            return preferredLanguage;
        }

        public void setPreferredLanguage(String preferredLanguage) {
            if (preferredLanguage.equals(EN_US)
                    || preferredLanguage.equals(ZH_CN)) {
                User.this.preferredLanguage = preferredLanguage;
            }else{
                Log.e(TAG, "Tried to set invalid language: "
                        + preferredLanguage);
            }
        }

        public String getProfileThumb100Url() {
            return profileThumb100Url;
        }

        public void setProfileThumb100Url(String profileThumb100Url) {
            User.this.profileThumb100Url = profileThumb100Url;
        }

        public String getProfileThumb50Url() {
            return profileThumb50Url;
        }

        public void setProfileThumb50Url(String profileThumb50Url) {
            User.this.profileThumb50Url = profileThumb50Url;
        }

        public String getProfileThumb30Url() {
            return profileThumb30Url;
        }

        public void setProfileThumb30Url(String profileThumb30Url) {
            User.this.profileThumb30Url = profileThumb30Url;
        }

        public String getCoverUrl() {
            return coverUrl;
        }

        public void setCoverUrl(String coverUrl) {
            User.this.coverUrl = coverUrl;
        }

        public int getCoverHeight() {
            return coverHeight;
        }

        public void setCoverHeight(int coverHeight) {
            User.this.coverHeight = coverHeight;
        }

        public int getCoverWidth() {
            return coverWidth;
        }

        public void setCoverWidth(int coverWidth) {
            User.this.coverWidth = coverWidth;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            User.this.path = path;
        }

        public Date getBirthdate() {
            return birthdate;
        }
        
        /**
         * @return the Users age in years
         */
        public int getAge(){
            Calendar dob = Calendar.getInstance();
            dob.setTime(birthdate);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
              age--;
            } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
              age--;
            }
            return age;
        }

        public void setBirthdate(Date birthdate) {
            User.this.birthdate = birthdate;
        }

        public String getEducation() {
            return education;
        }

        public void setEducation(String education) {
            User.this.education = education;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            User.this.city = city;
        }

        public String getCareerCompany() {
            return careerCompany;
        }

        public void setCareerCompany(String careerCompany) {
            User.this.careerCompany = careerCompany;
        }

        public String getCareerPosition() {
            return careerPosition;
        }

        public void setCareerPosition(String careerPosition) {
            User.this.careerPosition = careerPosition;
        }
    }

}