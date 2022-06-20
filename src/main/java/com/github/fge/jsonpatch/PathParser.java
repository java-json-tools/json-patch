package com.github.fge.jsonpatch;

import com.jayway.jsonpath.internal.Path;
import com.jayway.jsonpath.internal.path.PathCompiler;

import java.util.ArrayList;
import java.util.List;

public class PathParser {

    private static final String FILTER_PLACEHOLDER = "[?]";

    public static PathDetails getParentPathAndNewNodeName(String path) throws JsonPatchException {
        final String fullJsonPath = JsonPathParser.tmfStringToJsonPath(path);
        final Path compiledPath = compilePath(fullJsonPath);
        String[] splitJsonPath = splitJsonPath(compiledPath);

        int filterCounter = 0;
        List<String> filters = getFiltersOperations(fullJsonPath);
        boolean containsFiltersOrMultiIndexNotation = false;
        StringBuilder sb = new StringBuilder();
        sb.append("$");
        for (int i = 0; i < splitJsonPath.length - 1; i++) {
            if (splitJsonPath[i].equals("")) {
                continue;
            }
            if (splitJsonPath[i].equals(FILTER_PLACEHOLDER)) {
                sb.append(filters.get(filterCounter++));
                containsFiltersOrMultiIndexNotation = true;
            } else if (isArrayPart(splitJsonPath[i])) {
                sb.append(splitJsonPath[i]);
                if (isMultiIndexNotation(splitJsonPath[i])) {
                    containsFiltersOrMultiIndexNotation = true;
                }
            } else if (isDoubleDot(splitJsonPath[i])) {
                sb.append(splitJsonPath[i]);
            } else {
                sb.append("[").append(splitJsonPath[i]).append("]");
            }
        }
        final String pathToParent = sb.toString();
        final String newNodeName = getNewNodeName(splitJsonPath);
        return new PathDetails(pathToParent, newNodeName, containsFiltersOrMultiIndexNotation);
    }

    private static boolean isMultiIndexNotation(String path) {
        String pathWithoutBracket = path
                .replace("[", "")
                .replace("]", "");
        return !pathWithoutBracket.startsWith("'") && !pathWithoutBracket.matches("[0-9]+");
    }

    private static String getNewNodeName(String[] splitJsonPath) {
        return splitJsonPath[splitJsonPath.length - 1]
                .replace("'", "")
                .replace("[", "")
                .replace("]", "");
    }

    /**
     * Removes $ sign from the beginning of the path for easier processing - it will be added later on
     * This method is called after PathCompiler.compile, so we are sure now, that the path is correct
     * and bracket notation is used.
     * We can now split JsonPath using positive lookahead regex (without removing separator).
     * */
    private static String[] splitJsonPath(Path compiledPath) {
        return compiledPath.toString()
                .replace("$", "")
                .split("(?=\\[)");
    }

    private static Path compilePath(String fullJsonPath) throws JsonPatchException {
        try {
            return PathCompiler.compile(fullJsonPath);
        } catch (Exception e) {
            throw new JsonPatchException("Non-compilable path provided");
        }
    }

    private static boolean isDoubleDot(String jsonPathPart) {
        return jsonPathPart.equals("..");
    }

    private static boolean isArrayPart(String jsonPathPart) {
        return jsonPathPart.startsWith("[") && jsonPathPart.endsWith("]");
    }

    private static List<String> getFiltersOperations(String jsonPath) {
        if (!jsonPath.contains("[?(")) {
            return new ArrayList<>();
        }
        List<String> filters = new ArrayList<>();
        int openingBracketPosition = -1;
        int counter = 0;
        for (int i = 0; i < jsonPath.length(); i++) {
            if (jsonPath.charAt(i) == '[' && jsonPath.charAt(i+1) == '?') {
                if (openingBracketPosition == -1) {
                    openingBracketPosition = i;
                }
                counter++;
            }
            if (jsonPath.charAt(i) == ']' && counter > 0) {
                counter--;
                if (counter == 0) {
                    filters.add(jsonPath.substring(openingBracketPosition, i+1));
                    openingBracketPosition = -1;
                }
            }
        }
        return filters;
    }
}
