package com.gravity9.jsonpatch;

import java.util.Arrays;

public class JsonPathParser {

    private JsonPathParser() {
    }

    private static final String ARRAY_ELEMENT_REGEX = "\\A((\\d+)[^a-zA-Z]*)\\z";

    /**
     * Method parses JsonPointer or JsonPath path to JsonPath syntax
     *
     * @param path String containing JsonPath or JsonPointer expression
     * @return String containing JsonPath expression
     * @throws JsonPatchException throws when invalid JsonPointer expression provided
     */
    public static String parsePathToJsonPath(String path) throws JsonPatchException {
        if (path.startsWith("$")) {
            return path;
        } else if (path.contains("?")) {
            throw new JsonPatchException("Invalid path, `?` are not allowed in JsonPointer expressions.");
        } else if (path.contains("//")) {
            throw new JsonPatchException("Invalid path, `//` is not allowed in JsonPointer expressions.");
        }

        return "$" + Arrays.stream(path.replace('/', '.')
                        .replace("~1", "/") // / must be escaped in JsonPointer using ~1
                        .replace("~0", "~") // ~ must be escaped in JsonPointer using ~0
                        .split("\\."))    // split string to analyze every path  field separately
                .filter(s -> !s.isEmpty())    // skip empty string on the beginning of the array
                .map(s -> s.replaceAll(ARRAY_ELEMENT_REGEX, "[$1]"))
                .reduce("", (s, s2) -> s + "." + s2);    //connect the path
    }
}
