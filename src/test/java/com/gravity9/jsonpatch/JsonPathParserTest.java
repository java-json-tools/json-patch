package com.gravity9.jsonpatch;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JsonPathParserTest {

	@DataProvider
	public static Object[][] jsonPointerToJsonPathTestCases() {
		return new Object[][] {
				{"/productPrice/prodPriceAlteration", "$.productPrice.prodPriceAlteration"},
				{"/productPrice/1/prodPriceAlteration", "$.productPrice.[1].prodPriceAlteration"},
				{"/productPrice/prodPriceAlteration/1", "$.productPrice.prodPriceAlteration.[1]"},
				{"/2/1/-", "$.[2].[1].-"}
		};
	}

	@Test(dataProvider = "jsonPointerToJsonPathTestCases")
	public void shouldConvertPointerToJsonPath(String jsonPointerExpression, String expectedJsonPath) throws JsonPatchException {
		String result = JsonPathParser.parsePathToJsonPath(jsonPointerExpression);
		assertEquals(result, expectedJsonPath);
	}

	@Test
	public void shouldLeaveJsonPathStatementsUntouched() throws JsonPatchException {
		String filterQuery = "$.arrayPath[?(@.innerArray[?(@.nestedVal=='as')] empty false)].innerArray[?(@.nestedVal=='df')].name";
		String expected = "$.arrayPath[?(@.innerArray[?(@.nestedVal=='as')] empty false)].innerArray[?(@.nestedVal=='df')].name";
		String result = JsonPathParser.parsePathToJsonPath(filterQuery);
		assertEquals(result, expected);
	}

	@Test(expectedExceptions = JsonPatchException.class, expectedExceptionsMessageRegExp = "Invalid path, `//` is not allowed in JsonPointer expressions.")
	public void shouldThrowExceptionWhenDoubleSlashesInJsonPointerPath() throws JsonPatchException {
		String filterQuery = "/characteristic/0//age";
		JsonPathParser.parsePathToJsonPath(filterQuery);
	}

	@Test(expectedExceptions = JsonPatchException.class)
	public void shouldThrowExceptionWhenQuestionMarkInJsonPointerPath() throws JsonPatchException {
		String filterQuery = "/characteristic/0/age?";
		JsonPathParser.parsePathToJsonPath(filterQuery);
	}
}