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

package com.gravity9.jsonpatch.diff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNumEquals;
import com.google.common.collect.Lists;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class JsonDiffTest {

	private static final JsonNumEquals EQUIVALENCE = JsonNumEquals.getInstance();

	private final JsonNode testData;

	public JsonDiffTest()
		throws IOException {
		final String resource = "/jsonpatch/diff/diff.json";
		testData = JsonLoader.fromResource(resource);
	}

	@DataProvider
	public Iterator<Object[]> getPatchesOnly() {
		final List<Object[]> list = Lists.newArrayList();

		for (final JsonNode node : testData)
			if (!node.has("ignoreFields")) {
				list.add(new Object[]{node.get("first"), node.get("second")});
			}

		return list.iterator();
	}

	@Test(dataProvider = "getPatchesOnly")
	public void generatedPatchAppliesCleanly(final JsonNode first,
											 final JsonNode second)
		throws JsonPatchException {
		final JsonPatch patch = JsonDiff.asJsonPatch(first, second);
		final JsonNode actual = patch.apply(first);

		assertThat(EQUIVALENCE.equivalent(second, actual)).overridingErrorMessage(
			"Generated patch failed to apply\nexpected: %s\nactual: %s",
			second, actual
		).isTrue();
	}

	@DataProvider
	public Iterator<Object[]> getLiteralPatches() {
		final List<Object[]> list = Lists.newArrayList();

		for (final JsonNode node : testData) {
			if (!node.has("patch") || node.has("ignoreFields"))
				continue;
			list.add(new Object[]{
				node.get("message").textValue(), node.get("first"),
				node.get("second"), node.get("patch")
			});
		}

		return list.iterator();
	}

	@Test(
		dataProvider = "getLiteralPatches",
		dependsOnMethods = "generatedPatchAppliesCleanly"
	)
	public void generatedPatchesAreWhatIsExpected(final String message,
												  final JsonNode first, final JsonNode second, final JsonNode expected) throws JsonPatchException {
		final JsonNode actual = JsonDiff.asJson(first, second);

		assertThat(EQUIVALENCE.equivalent(expected, actual)).overridingErrorMessage(
			"patch is not what was expected\nscenario: %s\n"
				+ "expected: %s\nactual: %s\n", message, expected, actual
		).isTrue();
	}

	@DataProvider
	public Iterator<Object[]> getDiffsWithIgnoredFields() {
		final List<Object[]> list = Lists.newArrayList();

		for (final JsonNode node : testData) {
			if (node.has("ignoreFields")) {
				list.add(new Object[]{
						node.get("message").textValue(), node.get("first"),
						node.get("second"), node.get("patch"), node.get("ignoreFields")
				});
			}
		}

		return list.iterator();
	}

	@Test(
			dataProvider = "getDiffsWithIgnoredFields"
	)
	public void generatedPatchesIgnoreFields(final String message,
											 final JsonNode first, final JsonNode second, final JsonNode expected,
											 final JsonNode ignoreFields) throws JsonPatchException {

		final List<String> ignoreFieldsList = new ArrayList<>();
		final Iterator<JsonNode> ignoreFieldsIterator = ignoreFields.elements();
		while (ignoreFieldsIterator.hasNext()) {
			ignoreFieldsList.add(ignoreFieldsIterator.next().textValue());
		}

		final JsonNode actual = JsonDiff.asJsonIgnoringFields(first, second, ignoreFieldsList);

		assertThat(EQUIVALENCE.equivalent(expected, actual)).overridingErrorMessage(
				"patch is not what was expected\nscenario: %s\n"
						+ "expected: %s\nactual: %s\n", message, expected, actual
		).isTrue();
	}

	@DataProvider
	public Iterator<Object[]> getInvalidIgnoreFieldsExpressions() {
		final List<Object[]> list = Lists.newArrayList();
		list.add(new Object[]{
				"$.a[(@.length-1)]", "Could not parse token starting at position 3. Expected ?, ', 0-9, * "
		});
		list.add(new Object[]{
				"/a/?", "Invalid path, `?` are not allowed in JsonPointer expressions."
		});
		return list.iterator();
	}

	@Test(
			dataProvider = "getInvalidIgnoreFieldsExpressions"
	)
	public void shouldNotPerformDiffWhenIgnoreFieldsContainsInvalidExpression(String ignoreFieldsExpression, String expectedExceptionMessage) throws JsonProcessingException {
		// given
		JsonNode source = new ObjectMapper().readTree("{\"a\": \"1\"}");
		JsonNode target = new ObjectMapper().readTree("{\"a\": \"1\"}");
		List<String> ignoreFields = new ArrayList<>();
		ignoreFields.add(ignoreFieldsExpression);

		// when
		assertThatThrownBy(() -> JsonDiff.asJsonIgnoringFields(source, target, ignoreFields))
				.isExactlyInstanceOf(JsonPatchException.class)
				.hasMessageStartingWith(expectedExceptionMessage);

		assertThatThrownBy(() -> JsonDiff.asJsonPatchIgnoringFields(source, target, ignoreFields))
				.isExactlyInstanceOf(JsonPatchException.class)
				.hasMessageStartingWith(expectedExceptionMessage);
	}
}
