/**
 * TestSuite.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

/**
 * @author Viktor Nyblom
 *
 */
public class P1TestSuite extends TestSuite {
	public static Test suite(){
		return new TestSuiteBuilder(P1TestSuite.class).includeAllPackagesUnderHere().build();
	}

}
