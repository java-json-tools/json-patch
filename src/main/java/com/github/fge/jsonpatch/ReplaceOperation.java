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
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * JSON Patch {@code replace} operation
 *
 * <p>For this operation, {@code path} points to the value to replace, and
 * {@code value} is the replacement value.</p>
 *
 * <p>It is an error condition if {@code path} does not point to an actual JSON
 * value.</p>
 */
public final class ReplaceOperation extends PathValueOperation {

    @JsonCreator
    public ReplaceOperation(@JsonProperty("path") final String path, @JsonProperty("value") final JsonNode value) {
        super("replace", path, value);
    }

    @Override
    public JsonNode apply(final JsonNode node) throws JsonPatchException {
        final String jsonPath = JsonPathParser.tmfStringToJsonPath(path);
        final DocumentContext nodeContext = JsonPath.parse(node.deepCopy());
        final JsonNode nodeAtPath = nodeContext.read(jsonPath);
        if (nodeAtPath == null) {
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchPath"));
        }
        final JsonNode replacement = value.deepCopy();
        if (path.isEmpty()) {
            return replacement;
        }

        return nodeContext
                .set(jsonPath, value)
                .read("$", JsonNode.class);
    }
}
