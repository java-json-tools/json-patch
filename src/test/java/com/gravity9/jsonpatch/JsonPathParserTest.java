package com.gravity9.jsonpatch;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JsonPathParserTest {

	@Test
	public void shouldConvertPointerToJsonPath() throws JsonPatchException {
		String jsonPointerWithQuery = "/productPrice/prodPriceAlteration";
		String expected = "$.productPrice.prodPriceAlteration";
		String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
		assertEquals(result, expected);
	}

	@Test
	public void shouldConvertPointerWithArrayToJsonPath() throws JsonPatchException {
		String jsonPointerWithQuery = "/productPrice/1/prodPriceAlteration";
		String expected = "$.productPrice.[1].prodPriceAlteration";
		String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
		assertEquals(result, expected);
	}

	@Test
	public void shouldConvertPointerWithArrayAtTheEndToJsonPath() throws JsonPatchException {
		String jsonPointerWithQuery = "/productPrice/prodPriceAlteration/1";
		String expected = "$.productPrice.prodPriceAlteration.[1]";
		String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
		assertEquals(result, expected);
	}

	@Test
	public void shouldConvertArrayPathToJsonPath() throws JsonPatchException {
		String jsonPointer = "/2/1/-";
		String expected = "$.[2].[1].-";
		String result = JsonPathParser.tmfStringToJsonPath(jsonPointer);
		assertEquals(result, expected);
	}

	@Test
	public void shouldLeaveJsonPathStatementsUntouched() throws JsonPatchException {
		String filterQuery = "$.arrayPath[?(@.innerArray[?(@.nestedVal=='as')] empty false)].innerArray[?(@.nestedVal=='df')].name";
		String expected = "$.arrayPath[?(@.innerArray[?(@.nestedVal=='as')] empty false)].innerArray[?(@.nestedVal=='df')].name";
		String result = JsonPathParser.tmfStringToJsonPath(filterQuery);
		assertEquals(result, expected);
	}

	@Test(expectedExceptions = JsonPatchException.class, expectedExceptionsMessageRegExp = "Invalid path, `//` is not allowed in JsonPointer expressions.")
	public void shouldThrowExceptionWhenDoubleSlashesInJsonPointerPath() throws JsonPatchException {
		String filterQuery = "/characteristic/0//age";
		JsonPathParser.tmfStringToJsonPath(filterQuery);
	}

	@Test(expectedExceptions = JsonPatchException.class)
	public void shouldThrowExceptionWhenQuestionMarkInJsonPointerPath() throws JsonPatchException {
		String filterQuery = "/characteristic/0/age?";
		JsonPathParser.tmfStringToJsonPath(filterQuery);
	}
}