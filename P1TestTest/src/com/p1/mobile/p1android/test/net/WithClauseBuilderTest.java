package com.p1.mobile.p1android.test.net;

import android.util.Log;

import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.DummyContentHandler;
import com.p1.mobile.p1android.content.UserPicturesList;
import com.p1.mobile.p1android.content.UserPicturesList.RangePagination;
import com.p1.mobile.p1android.content.UserPicturesList.UserPicturesListIOSession;
import com.p1.mobile.p1android.net.withclause.WithClauseBuilder;

import junit.framework.TestCase;

public class WithClauseBuilderTest extends TestCase {

    WithClauseBuilder builder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        builder = new WithClauseBuilder();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWithClauseBuilderWithPicture() {
        assertEquals("?with=pictures", builder.addWithPicturesParam()
                .toString());
    }

    public void testWithClauseBuilderWithPictureAndLimit() {
        assertEquals("?with=pictures&limit=5", builder.addWithPicturesParam()
                .addLimitParam(5).toString());
    }

    public void testWithClauseBuilderWithPictureAndWithLiksesAndWithOffset() {
        assertEquals("?with=pictures;likes&offset=5", builder
                .addWithPicturesParam().addWithLikesParam().addOffsetParam(5)
                .toString());
    }

    public void testWithClauseLimitOnly() {
        assertEquals("?limit=5", builder.addLimitParam(5).toString());
    }

    public void testWithClauseOffsetOnly() {
        assertEquals("?offset=5", builder.addOffsetParam(5).toString());
    }

    public void testWithClauseLimitAndOffsetOnly() {
        assertEquals("?limit=5&offset=5", builder.addLimitParam(5)
                .addOffsetParam(5).toString());
    }
    
    public void testWithClauseFilter() {
        BrowseFilter filter = new BrowseFilter();
        filter.setGender(BrowseFilter.GENDER_FEMALE);
        filter.setFilterBy(BrowseFilter.BY_RANDOM);
        Log.d("Test", builder.addBrowseFilter(filter).toString());
        assertTrue("?filter=female&order=rand".equals(builder.addBrowseFilter(filter).toString()) ||
                "?order=rand&filter=female".equals(builder.addBrowseFilter(filter).toString()));
    }
    
    public void testWithClauseRange() {
        UserPicturesList pList = DummyContentHandler.createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try{
            RangePagination pagination = io.getPaginationInitial("500");
            assertEquals("?range=500,"+pagination.positiveRange+","+pagination.negativeRange, builder.addRangeParam(pagination).toString());
        }finally{
            io.close();
        }
    }

    public void testWithEmpty() {
        assertEquals("", builder.toString());
    }
}
