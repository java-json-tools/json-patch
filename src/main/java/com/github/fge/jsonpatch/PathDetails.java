package com.github.fge.jsonpatch;

public class PathDetails {

    private String pathToParent;

    private String newNodeName;

    private boolean containsFiltersOrMultiIndexesNotation;

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

    public boolean isContainsFiltersOrMultiIndexesNotation() {
        return containsFiltersOrMultiIndexesNotation;
    }
}
