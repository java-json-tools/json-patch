/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;


/**
 * JSON Patch {@code add} operation
 *
 * <p>For this operation, {@code path} is the JSON Pointer where the value
 * should be added, and {@code value} is the value to add.</p>
 *
 * <p>Note that if the target value pointed to by {@code path} already exists,
 * it is replaced. In this case, {@code add} is equivalent to {@code replace}.
 * </p>
 *
 * <p>Note also that a value will be created at the target path <b>if and only
 * if</b> the immediate parent of that value exists (and is of the correct
 * type).</p>
 *
 * <p>Finally, if the last reference token of the JSON Pointer is {@code -} and
 * the immediate parent is an array, the given value is added at the end of the
 * array. For instance, applying:</p>
 *
 * <pre>
 *     { "op": "add", "path": "/-", "value": 3 }
 * </pre>
 *
 * <p>to:</p>
 *
 * <pre>
 *     [ 1, 2 ]
 * </pre>
 *
 * <p>will give:</p>
 *
 * <pre>
 *     [ 1, 2, 3 ]
 * </pre>
 */
public final class AddOperation extends PathValueOperation {
    public static final String LAST_ARRAY_ELEMENT_SYMBOL = "-";

    @JsonCreator
    public AddOperation(@JsonProperty("path") final String path,
                        @JsonProperty("value") final JsonNode value) {
        super("add", path, value);
    }

    @Override
    public JsonNode apply(final JsonNode node) throws JsonPatchException {
        if (path.isEmpty()) {
            return value;
        }
        /*
         * Check the parent node: it must exist and be a container (ie an array
         * or an object) for the add operation to work.
         */
        final String fullJsonPath = JsonPathParser.tmfStringToJsonPath(path);
        final int lastDotIndex = fullJsonPath.lastIndexOf('.');
        final String newNodeName = fullJsonPath.substring(lastDotIndex + 1)
                .replace("[", "").replace("]", "");
        final String pathToParent = fullJsonPath.substring(0, lastDotIndex);

        final DocumentContext nodeContext = JsonPath.parse(node.deepCopy());

        final JsonNode evaluatedJsonParents = nodeContext.read(pathToParent);
        if (evaluatedJsonParents == null) {
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchParent"));
        }
        if (!evaluatedJsonParents.isContainerNode()) {
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.parentNotContainer"));
        }

        if (pathToParent.contains("[?(")) { // json filter result is always a list
            for (int i = 0; i < evaluatedJsonParents.size(); i++) {
                JsonNode parentNode = evaluatedJsonParents.get(i);
                if (!parentNode.isContainerNode()) {
                    throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.parentNotContainer"));
                }
                DocumentContext containerContext = JsonPath.parse(parentNode);
                if (parentNode.isArray()) {
                    addToArray(containerContext, "$", newNodeName);
                } else {
                    addToObject(containerContext, "$", newNodeName);
                }
            }
            return nodeContext.read("$");
        } else {
            return evaluatedJsonParents.isArray()
                    ? addToArray(nodeContext, pathToParent, newNodeName)
                    : addToObject(nodeContext, pathToParent, newNodeName);
        }
    }

    private JsonNode addToArray(final DocumentContext node, String jsonPath, String newNodeName) throws JsonPatchException {
        if (newNodeName.equals(LAST_ARRAY_ELEMENT_SYMBOL)) {
            return node.add(jsonPath, value).read("$", JsonNode.class);
        }

        final int size = node.read(jsonPath, JsonNode.class).size();
        final int index = verifyAndGetArrayIndex(newNodeName, size);

        ArrayNode updatedArray = node.read(jsonPath, ArrayNode.class).insert(index, value);
        return "$".equals(jsonPath) ? updatedArray : node.set(jsonPath, updatedArray).read("$", JsonNode.class);
    }

    private JsonNode addToObject(final DocumentContext node, String jsonPath, String newNodeName) {
        return node
                .put(jsonPath, newNodeName, value)
                .read("$", JsonNode.class);
    }

    private int verifyAndGetArrayIndex(String stringIndex, int size) throws JsonPatchException {
        int index;
        try {
            index = Integer.parseInt(stringIndex);
        } catch (NumberFormatException ignored) {
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.notAnIndex"));
        }
        if (index < 0 || index > size) {
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchIndex"));
        }
        return index;
    }
}
