package com.github.fge.jsonpatch;

import static com.github.fge.jsonpatch.JsonPatchOperation.BUNDLE;

public class JsonPathParser {

    private static final String ARRAY_ELEMENT_REGEX = "\\.(\\d+)\\.";
    private static final String ARRAY_ELEMENT_LAST_REGEX = "\\.(\\d+)$";

    public static String tmfStringToJsonPath(String path) throws JsonPatchException {
        if (!path.startsWith("/") && !path.isEmpty()) {
            return "$." + path;
        }
        if ("/".equals(path)) {
            return "$";
        }
        final String[] pointerAndQuery = path
                .replaceAll("(\\w)\\?", "$1#THIS_IS_SPLIT_PLACEHOLDER#")
                .split("#THIS_IS_SPLIT_PLACEHOLDER#", -1);
        if (pointerAndQuery.length > 2) {
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.invalidPathExpression"));
        }

        final String jsonPath = "$" + pointerAndQuery[0].replace('/', '.')
                .replaceAll(ARRAY_ELEMENT_REGEX, ".[$1].")
                .replaceAll(ARRAY_ELEMENT_REGEX, ".[$1].") // has to be repeated due to positive lookahead not working properly
                .replaceAll(ARRAY_ELEMENT_LAST_REGEX, ".[$1]");
        final String jsonPathWithQuery = addQueryIfApplicable(jsonPath, pointerAndQuery);
        return jsonPathWithQuery;
    }

    private static String addQueryIfApplicable(String jsonPath, String[] pointerAndQuery) {
        if (pointerAndQuery.length == 2) {
            String preparedFilter = pointerAndQuery[1]
                    .replaceAll("]", "] empty false") // add empty false to nested array expressions
                    .replaceAll("(\\w)=(\\w)", "$1==$2") // replace single equals with double
                    .replaceAll("==([\\w .]+)", "=='$1'") // surround strings with single quotes
                    .replaceFirst("\\w+", "@") // jsonpath expression should start with @ as the name of item
                    .replaceAll("([&|])\\w+", " $1$1 @"); // replace single | and & with doubles
            String filterWithBooleansAndNumbers = preparedFilter
                    .replaceAll("@([\\w.]+)=='(true|false)'", "(@$1==$2 || @$1=='$2')") // prepare a statement for boolean and boolean as string
                    .replaceAll("@([\\w.]+)=='(\\d+)'", "(@$1==$2 || @$1=='$2')") // prepare a statement for an integer and integer as string
                    .replaceAll("@([\\w.]+)=='(\\d+\\.\\d+)'", "(@$1==$2 || @$1=='$2')"); // prepare a statement for float and float as string
            return jsonPath.replaceFirst("(\\w+)", "$1[?(" + filterWithBooleansAndNumbers + ")]");
        } else {
            return jsonPath;
        }
    }
}
