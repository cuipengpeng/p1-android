package com.p1.mobile.p1android.content;

/**
 * 
 * @author Anton
 * 
 */
public class BrowseFilter {
    
    public static final String BY_RECENT = "recent".intern();
    public static final String BY_POPULAR = "pop".intern();
    public static final String BY_RANDOM = "rand".intern();
    
    public static final String GENDER_ALL = "all".intern();
    public static final String GENDER_MALE = "male".intern();
    public static final String GENDER_FEMALE = "female".intern();
    
    private String filterBy = BY_POPULAR;
    private String gender = GENDER_ALL;
    
    public BrowseFilter(){
        
    }
    
    public String getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(String filterBy) {
        filterBy = filterBy.intern();
        if(filterBy == BY_RECENT || filterBy == BY_POPULAR || filterBy == BY_RANDOM){
            this.filterBy = filterBy;
        }
        else{
            throw new IllegalArgumentException();
        }
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        gender = gender.intern();
        if(gender == GENDER_ALL || gender == GENDER_MALE || gender == GENDER_FEMALE){
            this.gender = gender;
        }
        else{
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof BrowseFilter))
            return false;
        BrowseFilter other = (BrowseFilter)object;
        if(this.filterBy.equals(other.filterBy) && this.gender.equals(other.gender)){
            return true;
        }
        
        return false;
        
    }

}
