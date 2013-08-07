package com.p1.mobile.p1android.test.content;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.p1.mobile.p1android.content.DummyContentHandler;
import com.p1.mobile.p1android.content.UserPicturesList;
import com.p1.mobile.p1android.content.UserPicturesList.RangePagination;
import com.p1.mobile.p1android.content.UserPicturesList.UserPicturesListIOSession;

public class UserPictureListTest extends TestCase {
    
    /**
     * A sequential list of Ids, where the Id is the same as the Index. Do not modify.
     */
    public List<String> hugeListOfIds;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(hugeListOfIds == null){
            hugeListOfIds = new ArrayList<String>();
            for(int c=0; c<600; c++){
                hugeListOfIds.add(String.valueOf(c));
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testRetrieveFromFilledList() {
        UserPicturesList pList = DummyContentHandler.createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try{
            io.addPaginatedIds(hugeListOfIds);
            String originId = "200";
            String returnedId = io.getPictureId(originId, 50);
            assertEquals(returnedId, "250");
            
        }finally{
            io.close();
        }
    }

    public void testRetrieveOutsideFilledList() {
        UserPicturesList pList = DummyContentHandler
                .createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try {
            io.addPaginatedIds(hugeListOfIds);
            String originId = "500";
            String returnedId = io.getPictureId(originId, 150);
            assertNull(returnedId);

        } finally {
            io.close();
        }
    }
    
    public void testOriginId(){
        UserPicturesList pList = DummyContentHandler.createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try{
            String originId = "200";
            String returnedOriginId = io.getPictureId(originId, 0);
            assertEquals(originId, returnedOriginId);
            
        }finally{
            io.close();
        }
    }
    public void testUnfilledOriginId(){
        UserPicturesList pList = DummyContentHandler.createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try{
            String originId = "200";
            String returnedOriginId = io.getPictureId(originId, 1);
            assertTrue(returnedOriginId == null);
            
        }finally{
            io.close();
        }
    }
    
    public void testBasicPaginationRange(){
        UserPicturesList pList = DummyContentHandler.createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try{
            RangePagination pagination = io.getPaginationInitial("500");
            assertEquals(io.getPaginationLimit(), pagination.expectedSize());
            List<String> dummyNetList = hugeListOfIds.subList(500-pagination.negativeRange, 500+pagination.positiveRange+1);
            assertEquals(dummyNetList.size(), pagination.expectedSize());
            io.fillIds(dummyNetList, pagination);
            pagination = io.getPaginationPositive("500");
            assertEquals(io.getPaginationLimit(), pagination.expectedSize());
            pagination = io.getPaginationNegative("500");
            assertEquals(io.getPaginationLimit(), pagination.expectedSize());
            
            
            
        }finally{
            io.close();
        }
    }
    
    public void testPaginationRange(){
        UserPicturesList pList = DummyContentHandler.createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try{
            RangePagination pagination = io.getPaginationInitial("500");
            List<String> dummyNetList = hugeListOfIds.subList(500-pagination.negativeRange, 500+pagination.positiveRange+1);
            assertEquals(dummyNetList.size(), pagination.expectedSize());
            io.fillIds(dummyNetList, pagination);
            String originId = "500";
            String returnedOriginId = io.getPictureId(originId, -4);
            assertEquals("496", returnedOriginId);
  
        }finally{
            io.close();
        }
    }
    public void testFilling(){
        UserPicturesList pList = DummyContentHandler.createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try{
            RangePagination pagination = io.getPaginationInitial("500");
            List<String> dummyNetList = dummyNetResponse(pagination);
            assertEquals(dummyNetList.size(), pagination.expectedSize());
            io.fillIds(dummyNetList, pagination);
            pagination = io.getPaginationPositive("500");
            dummyNetList = dummyNetResponse(pagination);
            io.fillIds(dummyNetList, pagination);
            String originId = "500";
            String returnedOriginId = io.getPictureId(originId, 20);
            assertEquals("520", returnedOriginId);
  
        }finally{
            io.close();
        }
    }
    public void testMalformedFilling(){
        UserPicturesList pList = DummyContentHandler.createUserPicturesList("DummyId");
        UserPicturesListIOSession io = pList.getIOSession();
        try{
            RangePagination pagination = io.getPaginationInitial("20");
            List<String> dummyNetList = dummyNetResponse(pagination);
            assertEquals(dummyNetList.size(), pagination.expectedSize());
            io.fillIds(dummyNetList, pagination);
            pagination = io.getPaginationNegative("20");
            dummyNetList = dummyNetResponse(pagination);
            assertTrue(dummyNetList.size() != pagination.expectedSize());
            io.fillIds(dummyNetList, pagination);
            String originId = "20";
            String returnedOriginId = io.getPictureId(originId, -19);
            assertEquals("1", returnedOriginId);
  
        }finally{
            io.close();
        }
    }
    
    public List<String> dummyNetResponse(RangePagination pagination){
        int originIndex = Integer.parseInt(pagination.originId);
        int start = originIndex-pagination.negativeRange;
        if(start < 0)
            start = 0;
        int end = originIndex+pagination.positiveRange+1;
        if(end > hugeListOfIds.size())
            end = hugeListOfIds.size();
        
        ArrayList<String> returnedList = new ArrayList<String>(hugeListOfIds.subList(start, end));
        return returnedList;
    }

}
