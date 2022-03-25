package com.github.fge.jsonpatch;

import static com.github.fge.jsonpatch.JsonPatchOperation.BUNDLE;

public class JsonPathParser {

    private static final String ARRAY_ELEMENT_REGEX = "(?<=\\.)(\\d+)";

    public static String tmfStringToJsonPath(String path) throws JsonPatchException {
        if ("/".equals(path) || path.isEmpty()) {
            return "$";
        }
        if (path.startsWith("$")) {
            return path;
        }
        if (path.contains("?")) {
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.invalidPathExpression"));
        }

        return "$" + path.replace('/', '.').replaceAll(ARRAY_ELEMENT_REGEX, "[$1]");
    }
}
