/**
 * WithClauseBuilder.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.net;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * @author Viktor Nyblom
 * 
 */

// TODO Reimplement properly
public class WithClause {
    private List<String> withStrings;
    private int OFFSET = 0;
    private int PAGINATION = 100;

    private WithClause(Builder builder) {
        this.withStrings = builder.withStrings;
        this.OFFSET = builder.offset;
        this.PAGINATION = builder.pagination;
    }

    public static class Builder {

        private static final String PICTURES = "pictures";
        private static final String TAGS = "tags";
        private static final String COMMENTS = "comments";
        private static final String LIKES = "likes";
        private static final String USERS = "users";

        private List<String> withStrings = new ArrayList<String>();
        
        private int offset=0;
        
        private int pagination=100;

        public Builder pictures(boolean pictures) {
            if (pictures)
                withStrings.add(PICTURES);
            else
            	withStrings.remove(PICTURES);
            return this;
        }

        public Builder tags(boolean tags) {
            if (tags)
                withStrings.add(TAGS);
            else
            	withStrings.remove(TAGS);
            return this;
        }

        public Builder comments(boolean comments) {
            if (comments)
                withStrings.add(COMMENTS);
            else
            	withStrings.remove(COMMENTS);
            return this;
        }

        public Builder likes(boolean likes) {
            if (likes)
                withStrings.add(LIKES);
            else
            	withStrings.remove(LIKES);
            return this;
        }

        public Builder users(boolean users) {
            if (users)
                withStrings.add(USERS);
            else
            	withStrings.remove(USERS);
            return this;
        }

        public WithClause build() {
            return new WithClause(this);
        }

        public Builder pagination(int pagination) {
            this.pagination = pagination;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }
    }

    public String toString() {
        if (withStrings.isEmpty() && PAGINATION ==100 && OFFSET == 0)
            return "";
        StringBuilder builder = new StringBuilder();
        if(!withStrings.isEmpty() || PAGINATION != 100 || OFFSET!=0){
        	builder.append("?");
        }
        if (!withStrings.isEmpty()) {
            builder.append("with=");

            ListIterator<String> it = withStrings.listIterator();
            while (it.hasNext()) {
                builder.append(it.next());
                if (it.hasNext()) {
                    builder.append(";");
                }
            }
            if (PAGINATION != 100 || OFFSET!=0)
                builder.append("&");
        }
        
        if (PAGINATION != 100){
        	builder.append("limit="+PAGINATION);
        	if(OFFSET!=0)
        		builder.append("&");
        }
        if (OFFSET != 0)
            builder.append("offset=" + OFFSET);

        return builder.toString();
    }
}
