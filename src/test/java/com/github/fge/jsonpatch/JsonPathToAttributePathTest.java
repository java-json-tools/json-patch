/*
 * Copyright (c) 2016, Alexander Patrikalakis (amcp@me.com)
 * Copyright (c) 2015, Daisuke Miyamoto (dai.0304@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package com.github.fge.jsonpatch;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.fge.jackson.jsonpointer.JsonPointer;

public class JsonPathToAttributePathTest {
	
	JsonPathToAttributePath sut = new JsonPathToAttributePath();
	
	@Test
	public void test() throws Exception {
		// setup
		JsonPointer pointer = new JsonPointer("/a/b/c");
		String expected = "a.b.c";
		// exercise
		String actual = sut.apply(pointer);
		// verify
		Assert.assertEquals(actual, expected);
	}
	
	@Test
	public void testFoo() throws Exception {
		// setup
		JsonPointer pointer = new JsonPointer("/foo");
		String expected = "foo";
		// exercise
		String actual = sut.apply(pointer);
		// verify
		Assert.assertEquals(actual, expected);
	}
	
	@Test
	public void testFoo0() throws Exception {
		// setup
		JsonPointer pointer = new JsonPointer("/foo/0");
		String expected = "foo[0]";
		// exercise
		String actual = sut.apply(pointer);
		// verify
		Assert.assertEquals(actual, expected);
	}
	
	@Test
	public void testEmpty() throws Exception {
		// setup
		JsonPointer pointer = new JsonPointer("/");
		String expected = "";
		// exercise
		String actual = sut.apply(pointer);
		// verify
		Assert.assertEquals(actual, expected);
	}
	
	@Test
	public void testSlash() throws Exception {
		// setup
		JsonPointer pointer = new JsonPointer("/a~1b");
		String expected = "a/b";
		// exercise
		String actual = sut.apply(pointer);
		// verify
		Assert.assertEquals(actual, expected);
	}
}
