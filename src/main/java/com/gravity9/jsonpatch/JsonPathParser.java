package com.gravity9.jsonpatch;

class JsonPathParser {

	private JsonPathParser() {}

	private static final String ARRAY_ELEMENT_REGEX = "(?<=\\.)(\\d+)";

	/**
	 * Method parses JsonPointer or JsonPath path to JsonPath syntax
	 * @param path String containing JsonPath or JsonPointer expression
	 * @return String containing JsonPath expression
	 * @throws JsonPatchException throws when invalid JsonPointer expression provided
	 */
	static String parsePathToJsonPath(String path) throws JsonPatchException {
		if (path.startsWith("$")) {
			return path;
		} else if (path.contains("?")) {
			throw new JsonPatchException("Invalid path, `?` are not allowed in JsonPointer expressions.");
		} else if (path.contains("//")) {
			throw new JsonPatchException("Invalid path, `//` is not allowed in JsonPointer expressions.");
		}

		return "$" + path.replace('/', '.')
			.replace("~1", "/") // / must be escaped in JsonPointer using ~1
			.replace("~0", "~") // ~ must be escaped in JsonPointer using ~0
			.replaceAll(ARRAY_ELEMENT_REGEX, "[$1]");
	}
}
