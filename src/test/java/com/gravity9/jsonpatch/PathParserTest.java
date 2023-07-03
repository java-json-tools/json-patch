package com.gravity9.jsonpatch;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PathParserTest {

	@DataProvider
	public static Object[][] pathToParse() {
		return new Object[][] {
				{"/user/1/name", "$['user'][1]", "name"},
				{"/user/name", "$['user']", "name"},
				{"$.user[1].name", "$['user'][1]", "name"},
				{"$['user'][1]['name']", "$['user'][1]", "name"},
				{"$['user'][1].name", "$['user'][1]", "name"},
				{"$[?(@.book[?(@.ratingScore == '111')] empty false)].title", "$[?(@.book[?(@.ratingScore == '111')] empty false)]", "title"},
				{"$..book[-1:].title", "$..['book'][-1:]", "title"},
				{"$..book[1:2].title", "$..['book'][1:2]", "title"},
				{"$..book[1:].title", "$..['book'][1:]", "title"},
				{"$..book[:5].title", "$..['book'][:5]", "title"},
				{"$..book[*].title", "$..['book'][*]", "title"},
				{"$.user[0].name[", "$['user'][0]", "name"},
				{"$.user['0'].name[", "$['user']['0']", "name"},
				{"/id", "$", "id"},
				{"$.id", "$", "id"},
				{"$['id']", "$", "id"},
				{"$[?(@.contactMedium[?(@.characteristic.contactType == 'primary' && @.characteristic.emailAddress)] empty true)].contactMedium.-", "$[?(@.contactMedium[?(@.characteristic.contactType == 'primary' && @.characteristic.emailAddress)] empty true)]['contactMedium']", "-"}
		};
	}

	@Test(dataProvider = "pathToParse")
	void should_parse_path(String path, String expectedPathToParent, String expectedNewNodeName) throws JsonPatchException {
		// when
		PathDetails result = PathParser.getParentPathAndNewNodeName(path);

		// then
		assertEquals(result.getPathToParent(), expectedPathToParent);
		assertEquals(result.getNewNodeName(), expectedNewNodeName);
	}


	// This notation is not supported by JsonPath PathCompiler.compile
	@Test(expectedExceptions = JsonPatchException.class)
	void should_throw_exception_with_double_dots_and_length() throws JsonPatchException {
		// given
		String path = "$..book[(@.length-1)].title";

		// when
		PathParser.getParentPathAndNewNodeName(path);
	}

	@Test(expectedExceptions = JsonPatchException.class)
	void should_throw_json_patch_exception_when_invalid_json_path_provided() throws JsonPatchException {
		// given
		final String invalidPath = "$.user[[]][.name";

		// when
		PathParser.getParentPathAndNewNodeName(invalidPath);
	}
}