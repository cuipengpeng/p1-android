package com.p1.mobile.p1android.test.net;

import com.p1.mobile.p1android.net.WithClause;

import junit.framework.TestCase;

public class WithClauseTest extends TestCase {

	WithClause.Builder withClause;
	
	protected void setUp() throws Exception {
		super.setUp();
		withClause = new WithClause.Builder();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testWithClauseBuilderWithPicture(){
		assertEquals("?with=pictures",withClause.pictures(true).build().toString());
	}
	
	public void testWithClauseBuilderWithPictureAndLimit(){
		assertEquals("?with=pictures&limit=5",withClause.pictures(true).pagination(5).build().toString());
	}
	
	public void testWithClauseBuilderWithPictureAndWith(){
		assertEquals("?with=pictures;likes&limit=5",withClause.pictures(true).likes(true).pagination(5).build().toString());
	}
	
	public void testWithClauseEmpty(){
		assertEquals("",withClause.pictures(true).pictures(false).build().toString());
	}
	
	public void testWithClauseLimitOnly(){
		assertEquals("?limit=5",withClause.pagination(5).build().toString());
	}
	
	public void testWithClauseOffsetOnly(){
		assertEquals("?offset=5",withClause.offset(5).build().toString());
	}
	
	public void testWithClauseLimitAndOffsetOnly(){
		assertEquals("?limit=5&offset=5",withClause.offset(5).pagination(5).build().toString());
	}
	
	
	public void testWithEmpty(){
		assertEquals("",withClause.comments(false).build().toString());
	}
}
