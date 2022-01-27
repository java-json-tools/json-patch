package com.github.fge.jsonpatch;

public class JsonPathParser {

    private static final String ARRAY_ELEMENT_REGEX = "\\.(\\d+)\\.";
    private static final String ARRAY_ELEMENT_LAST_REGEX = "\\.(\\d+)$";

    public static String tmfStringToJsonPath(String path) {
        if ("/".equals(path)) {
            return "$";
        }
        final String jsonPath = "$" + path.replace('/', '.')
                .replaceAll(ARRAY_ELEMENT_REGEX, ".[$1].")
                .replaceAll(ARRAY_ELEMENT_LAST_REGEX, ".[$1]");
        return jsonPath;
    }
}
