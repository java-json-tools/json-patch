package com.gravity9.jsonpatch;

import com.jayway.jsonpath.internal.Path;
import com.jayway.jsonpath.internal.path.PathCompiler;
import java.util.ArrayList;
import java.util.List;

public class PathParser {

	private static final String FILTER_PLACEHOLDER = "[?]";

	/**
	 * This method parses JsonPath to find node name that needs to be added and path to the parent of new node.
	 * Additionally, it finds if path contains filter or multi index notation (like [1:5])
	 *
	 * @param path Path in JsonPath or JsonPointer notation
	 * @return PathDetails containing path to parent, name of new node and boolean value if path contains filter or multi
	 * index notation
	 * @throws JsonPatchException when invalid path provided
	 * */
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
			if (splitJsonPath[i].isEmpty()) {
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
		FilterParserState filterParserState = new FilterParserState();
		for (int i = 0; i < jsonPath.length(); i++) {
			checkIfOpenBracket(jsonPath, i, filterParserState);
			checkIfCloseBracket(jsonPath, i, filterParserState, filters);
		}
		return filters;
	}

	private static void checkIfCloseBracket(String jsonPath, int currentPosition, FilterParserState filterParserState, List<String> filters) {
		if (jsonPath.charAt(currentPosition) == ']' && filterParserState.getNumberOfOpenInternalSquareBrackets() > 0) {
			filterParserState.decreaseNumberOfOpenInternalSquareBrackets();
		} else if (jsonPath.charAt(currentPosition) == ']' && filterParserState.getNumberOfOpenedFilters() > 0) {
			filterParserState.decreaseNumberOfOpenedFilters();
			if (filterParserState.noOpenFiltersAndBrackets()) {
				filters.add(jsonPath.substring(filterParserState.getFilterOpeningPosition(), currentPosition + 1));
				filterParserState.setFilterOpeningPosition(-1);
			}
		}
	}

	private static void checkIfOpenBracket(String jsonPath, int currentPosition, FilterParserState filterParserState) {
		if (jsonPath.charAt(currentPosition) == '[' && jsonPath.charAt(currentPosition + 1) == '?') {
			if (!filterParserState.wasOpeningFilterBracketFound()) {
				filterParserState.setFilterOpeningPosition(currentPosition);
			}
			filterParserState.increaseNumberOfOpenedFilters();
		} else if (jsonPath.charAt(currentPosition) == '[') {
			filterParserState.increaseNumberOfOpenInternalSquareBrackets();
		}
	}

	private static class FilterParserState {

		private int filterOpeningPosition = -1;
		private int numberOfOpenedFilters = 0; // filter starts with [?
		private int numberOfOpenInternalSquareBrackets = 0; // only [, without ?

		public boolean wasOpeningFilterBracketFound() {
			return filterOpeningPosition != -1;
		}

		public void setFilterOpeningPosition(int position) {
			this.filterOpeningPosition = position;
		}

		public void increaseNumberOfOpenedFilters() {
			this.numberOfOpenedFilters++;
		}

		public void decreaseNumberOfOpenedFilters() {
			this.numberOfOpenedFilters--;
		}

		public void increaseNumberOfOpenInternalSquareBrackets() {
			this.numberOfOpenInternalSquareBrackets++;
		}

		public void decreaseNumberOfOpenInternalSquareBrackets() {
			this.numberOfOpenInternalSquareBrackets--;
		}

		public int getNumberOfOpenedFilters() {
			return numberOfOpenedFilters;
		}

		public int getNumberOfOpenInternalSquareBrackets() {
			return numberOfOpenInternalSquareBrackets;
		}

		public int getFilterOpeningPosition() {
			return filterOpeningPosition;
		}

		public boolean noOpenFiltersAndBrackets() {
			return numberOfOpenedFilters == 0 && numberOfOpenInternalSquareBrackets == 0;
		}
	}
}
