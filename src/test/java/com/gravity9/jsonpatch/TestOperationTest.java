/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
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

package com.gravity9.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;

public final class TestOperationTest extends JsonPatchOperationTest {

	private static final TestDomain DOMAIN_TO_PATCH = new TestDomain(2022.4321f);
	private static final String testOperation = "[{\"op\":\"test\",\"path\":\"/myValue\",\"value\":2022.4321}]";

	public TestOperationTest()
		throws IOException {
		super("test");
	}

	@Test
	void testPatchValueIsDoubleDomainValueIsFloat() throws Exception {
		JsonPatch jsonPatch = new ObjectMapper().readValue(testOperation, JsonPatch.class);

		JsonNode jsonNode = new ObjectMapper().valueToTree(DOMAIN_TO_PATCH);

		jsonPatch.apply(jsonNode);
	}

	@SuppressWarnings("UnusedMethod")
	private static class TestDomain {

		private Float myValue;

		public TestDomain(Float myValue) {
			this.myValue = myValue;
		}

		public Float getMyValue() {
			return myValue;
		}

		public void setMyValue(Float myValue) {
			this.myValue = myValue;
		}
	}
}
