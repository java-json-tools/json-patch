package com.gravity9.jsonpatch;

public class PathDetails {

	private final String pathToParent;

	private final String newNodeName;

	private final boolean containsFiltersOrMultiIndexesNotation;

	public PathDetails(String pathToParent, String newNodeName, boolean containsFiltersOrMultiIndexesNotation) {
		this.pathToParent = pathToParent;
		this.newNodeName = newNodeName;
		this.containsFiltersOrMultiIndexesNotation = containsFiltersOrMultiIndexesNotation;
	}

	public String getPathToParent() {
		return pathToParent;
	}

	public String getNewNodeName() {
		return newNodeName;
	}

	public boolean doesContainFiltersOrMultiIndexesNotation() {
		return containsFiltersOrMultiIndexesNotation;
	}

	@Override
	public String toString() {
		return "PathDetails{" +
			"pathToParent='" + pathToParent + '\'' +
			", newNodeName='" + newNodeName + '\'' +
			", containsFiltersOrMultiIndexesNotation=" + containsFiltersOrMultiIndexesNotation +
			'}';
	}
}
