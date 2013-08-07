package com.p1.mobile.p1android.test.content.parsing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.parsing.UserParser;

import junit.framework.TestCase;

public class UserParserTest extends TestCase {

    private User testUser1;
    private User testUser2;

    private static final String USER_ID = "ID";
    private static final String NO_CHINESE_NAME__NUll_EDUCATION_JSON = "{\"id\":864,\"gender\":\"male\",\"preferred_name\":\"en-US\",\"name\":{\"en-US\":{\"fullname\":\"Viktor Nyblom\",\"givenname\":\"Viktor\",\"surname\":\"Nyblom\"}},\"path\":\"viktornyblom\",\"education\":null,\"career\":{\"company\":\"P1\",\"position\":\"Android Developer\"},\"city\":\"Beijing, China\",\"profile_picture\":{\"thumb100\":{\"url\":\"http:\\/\\/master.unstable.p.com\\/photos\\/img\\/1\\/86\\/44\\/thumb100_18644.jpg\",\"width\":100,\"height\":100},\"thumb50\":{\"url\":\"http:\\/\\/master.unstable.p.com\\/photos\\/img\\/1\\/86\\/44\\/thumb50_18644.jpg\",\"width\":50,\"height\":50},\"thumb30\":{\"url\":\"http:\\/\\/master.unstable.p.com\\/photos\\/img\\/1\\/86\\/44\\/thumb30_18644.jpg\",\"width\":30,\"height\":30}},\"cover_picture\":{\"cover\":{\"url\":\"http:\\/\\/master.unstable.p.com\\/photos\\/img\\/1\\/39\\/31\\/cover_13931.jpg\",\"width\":980,\"height\":400},\"cover_122\":{\"url\":\"http:\\/\\/master.unstable.p.com\\/photos\\/img\\/1\\/39\\/31\\/cover_122_13931.jpg\",\"width\":300,\"height\":122}},\"birthdate\":\"1986-04-18\",\"type\":\"user\",\"etag\":\"9cd0a57f3181fa3fc0f398d485a51c0e\"}";
    private static final String NO_CHINESE_NAME__NO_CARREER_JSON = "{\"id\":101,\"gender\":\"male\",\"preferred_name\":\"en-US\",\"name\":{\"en-US\":{\"fullname\":\"Patrick Trillsam\",\"givenname\":\"Patrick\",\"surname\":\"Trillsam\"}},\"path\":\"icepat\",\"education\":null,\"career\":[],\"city\":\"Beijing, China\",\"profile_picture\":{\"thumb100\":{\"url\":\"http:\\/\\/master.unstable.p.com\\/photos\\/img\\/0\\/29\\/41\\/thumb100_2941.jpg\",\"width\":100,\"height\":100},\"thumb50\":{\"url\":\"http:\\/\\/master.unstable.p.com\\/photos\\/img\\/0\\/29\\/41\\/thumb50_2941.jpg\",\"width\":50,\"height\":50},\"thumb30\":{\"url\":\"http:\\/\\/master.unstable.p.com\\/photos\\/img\\/0\\/29\\/41\\/thumb30_2941.jpg\",\"width\":30,\"height\":30}},\"cover_picture\":{\"cover\":{\"url\":\"http:\\/\\/api.master.unstable.p.com\\/v2\\/static\\/cover_default.png\",\"width\":655,\"height\":400},\"cover_122\":{\"url\":\"http:\\/\\/api.master.unstable.p.com\\/v2\\/static\\/cover_default_mini.png\",\"width\":300,\"height\":122}},\"birthdate\":\"1984-05-27\",\"type\":\"user\",\"etag\":\"\"}";

    private static final String ACTUAL_ID_1 = "864";
    private static final String ACTUAL_ID_2 = "101";
    private static final String ACTUAL_GENDER_1 = "male";
    private static final String ACTUAL_POSITION_1 = "Android Developer";
    private static final String ACTUAL_COMPANY_1 = "P1";
    private static final String ACTUAL_ETAG_1 = "9cd0a57f3181fa3fc0f398d485a51c0e";
    private static final String EMPTY = "";
    private static final String ACTUAL_SURNAME_NAME_1 = "Nyblom";
    private static final String ACTUAL_GIVEN_NAME_1 = "Viktor";
    private static final String ACTUAL_FULL_NAME_1 = "Viktor Nyblom";
    private static final String ACTUAL_SURNAME_NAME_2 = "Trillsam";
    private static final String ACTUAL_GIVEN_NAME_2 = "Patrick";
    private static final String ACTUAL_FULL_NAME_2 = "Patrick Trillsam";
    
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setUpUsers();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        testUser1 = null;
        testUser2 = null;
        ContentHandler.getInstance().tearDown();
    }

    public void testParseUser_no_chinese_null_education() {
        JsonElement element = new JsonParser()
                .parse(NO_CHINESE_NAME__NUll_EDUCATION_JSON);
        JsonObject testObject = element.getAsJsonObject();
        UserParser.parseToUser(testObject, testUser1);

        assertNotNull(testUser1);

        assertEquals(ACTUAL_ID_1, testUser1.getIOSession().getId());
        assertEquals(ACTUAL_GENDER_1, testUser1.getIOSession().getGender());
        assertEquals(ACTUAL_SURNAME_NAME_1, testUser1.getIOSession().getEnUsSurname());
        assertEquals(ACTUAL_GIVEN_NAME_1, testUser1.getIOSession().getEnUsGivenName());
        assertEquals(ACTUAL_FULL_NAME_1, testUser1.getIOSession().getEnUsFullname());
        assertNotNull(testUser1.getIOSession().getEducation());
        assertEquals(EMPTY, testUser1.getIOSession().getEducation());
        assertEquals(ACTUAL_POSITION_1, testUser1.getIOSession()
                .getCareerPosition());
        assertEquals(ACTUAL_COMPANY_1, testUser1.getIOSession()
                .getCareerCompany());
        assertEquals(ACTUAL_ETAG_1, testUser1.getIOSession().getEtag());

    }

    public void testParseUser_no_chinese_no_company() {
        JsonElement element = new JsonParser()
                .parse(NO_CHINESE_NAME__NO_CARREER_JSON);
        JsonObject testObject = element.getAsJsonObject();
        UserParser.parseToUser(testObject, testUser2);

        assertNotNull(testUser2);
        assertEquals(EMPTY, testUser2.getIOSession().getEducation());
        assertEquals(ACTUAL_SURNAME_NAME_2, testUser2.getIOSession().getEnUsSurname());
        assertEquals(ACTUAL_GIVEN_NAME_2, testUser2.getIOSession().getEnUsGivenName());
        assertEquals(ACTUAL_FULL_NAME_2, testUser2.getIOSession().getEnUsFullname());
    }

    private void setUpUsers() {
        testUser1 = ContentHandler.getInstance().getUser(ACTUAL_ID_1, null);
        testUser2 = ContentHandler.getInstance().getUser(ACTUAL_ID_2, null);
    }

}
