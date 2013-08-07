package com.p1.mobile.p1android.net.withclause;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import android.text.TextUtils;

import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.UserPicturesList.RangePagination;
import com.p1.mobile.p1android.content.parsing.GenericParser;

public class WithClauseBuilder {

    private static final String WITH_PICTURES = "pictures";
    private static final String WITH_TAGS = "tags";
    private static final String WITH_VENUES = "venues";
    private static final String WITH_COMMENTS = "comments";
    private static final String WITH_SHARES = "shares";
    private static final String WITH_LIKES = "likes";
    private static final String WITH_USERS = "users";
    private static final String WITH_MESSAGES = "messages";
    private static final String ORDER = "order=";
    private static final String LIMIT = "limit=";
    private static final String OFFSET = "offset=";
    private static final String FILTER = "filter=";
    private static final String UNTIL = "until=";
    private static final String GENDER = "filter="; // filter is used for gender
                                                    // filtering

    private static final String SEARCH_START = "search=";
    private static final String SEARCH_END = "";

    /*
     * Descending order
     */
    public static final String ORDER_DESC = "desc";

    private static final String SEPARATOR = "&";
    private static final String START = "?";

    private Param withParam;
    private Param orderParam;
    private Param paginationParam;
    private Param limitParam;
    private Param offsetParam;
    private Param filterParam;
    private Param untilParam;
    private Param genderParam;
    private Param searchParam;
    private RangeParam rangeParam;
    private LatLongParam latLongParam;

    Set<Param> params = new LinkedHashSet<Param>();

    public WithClauseBuilder() {
        withParam = new WithParam();
        orderParam = new DefaultParam();
        paginationParam = new DefaultParam();
        limitParam = new DefaultParam();
        offsetParam = new DefaultParam();
        genderParam = new DefaultParam();
        rangeParam = new RangeParam();
        untilParam = new DefaultParam();
        latLongParam = new LatLongParam();
        searchParam = new DefaultParam();
    }

    public WithClauseBuilder addWithPicturesParam() {
        withParam.addParam(WITH_PICTURES);
        params.add(withParam);
        return this;
    }

    public WithClauseBuilder addWithSharesParam() {
        withParam.addParam(WITH_SHARES);
        params.add(withParam);
        return this;
    }

    public WithClauseBuilder addWithCommentsParam() {
        withParam.addParam(WITH_COMMENTS);
        params.add(withParam);
        return this;
    }

    public WithClauseBuilder addWithTagsParam() {
        withParam.addParam(WITH_TAGS);
        params.add(withParam);
        return this;
    }

    public WithClauseBuilder addWithVenuesParam() {
        withParam.addParam(WITH_VENUES);
        params.add(withParam);
        return this;
    }

    public WithClauseBuilder addWithLikesParam() {
        withParam.addParam(WITH_LIKES);
        params.add(withParam);
        return this;
    }

    public WithClauseBuilder addWithUsersParam() {
        withParam.addParam(WITH_USERS);
        params.add(withParam);
        return this;
    }

    public WithClauseBuilder addWithMessagesParam() {
        withParam.addParam(WITH_MESSAGES);
        params.add(withParam);
        return this;
    }

    public WithClauseBuilder addOrderParam(String order) {
        orderParam.addParam(ORDER + order);
        params.add(orderParam);
        return this;
    }

    public WithClauseBuilder addLimitParam(int limit) {
        limitParam.addParam(LIMIT + String.valueOf(limit));
        params.add(limitParam);
        return this;
    }

    public WithClauseBuilder addOffsetParam(int offset) {
        offsetParam.addParam(OFFSET + String.valueOf(offset));
        params.add(offsetParam);
        return this;
    }

    public WithClauseBuilder addGenderParam(String gender) {
        genderParam.addParam(GENDER + String.valueOf(gender));
        params.add(genderParam);
        return this;
    }

    public WithClauseBuilder addSearchParam(String searchString) {
        if (TextUtils.isEmpty(searchString.trim())) {
            return this;
        }
        try {
            searchString = URLEncoder.encode(searchString.trim(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        searchParam.addParam(SEARCH_START + searchString + SEARCH_END);
        params.add(searchParam);
        return this;
    }

    public WithClauseBuilder addPositionParam(double latitude, double longitude) {
        latLongParam.setValues(latitude, longitude);
        params.add(latLongParam);
        return this;
    }

    public WithClauseBuilder addBrowseFilter(BrowseFilter browseFilter) {
        if (browseFilter.getFilterBy() != BrowseFilter.BY_RECENT) { // If not
                                                                    // API
                                                                    // default
            addOrderParam(browseFilter.getFilterBy());
        }
        if (browseFilter.getGender() != BrowseFilter.GENDER_ALL) { // If not API
                                                                   // default
            addGenderParam(browseFilter.getGender());
        }

        return this;
    }

    public WithClauseBuilder addRangeParam(RangePagination range) {
        rangeParam.setRange(range);
        params.add(rangeParam);

        return this;
    }

    public WithClauseBuilder addUntilParam(Date until) {
        if (until != null) {
            untilParam.addParam(UNTIL + GenericParser.formatAPITime(until));
            params.add(untilParam);
        }

        return this;
    }

    public String toString() {
        if (!params.isEmpty()) {
            Iterator<Param> it = params.iterator();
            StringBuilder builder = new StringBuilder();

            builder.append(START);

            while (it.hasNext()) {
                builder.append(it.next().getParamString());
                if (it.hasNext()) {
                    builder.append(SEPARATOR);
                }
            }

            return builder.toString();
        }

        return "";
    }

}
