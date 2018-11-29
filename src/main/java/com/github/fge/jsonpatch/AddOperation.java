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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.ReferenceToken;
import com.github.fge.jackson.jsonpointer.TokenResolver;

import java.util.NoSuchElementException;


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
public final class AddOperation
    extends PathValueOperation
{
    private static final ReferenceToken LAST_ARRAY_ELEMENT
        = ReferenceToken.fromRaw("-");

    @JsonCreator
    public AddOperation(@JsonProperty("path") final JsonPointer path,
        @JsonProperty("value") final JsonNode value)
    {
        super("add", path, value);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        if (path.isEmpty())
            return value;

        /*
         * Check the parent node: it must exist and be a container (ie an array
         * or an object) for the add operation to work.
         */
        final JsonNode parentNode = path.parent().path(node);
        if (parentNode.isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.noSuchParent"));
        if (!parentNode.isContainerNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.parentNotContainer"));
        return parentNode.isArray()
            ? addToArray(path, node)
            : addToObject(path, node);
    }

    private JsonNode addToArray(final JsonPointer path, final JsonNode node)
        throws JsonPatchException
    {
        final JsonNode ret = node.deepCopy();
        final ArrayNode target = (ArrayNode) path.parent().get(ret);

        final TokenResolver<JsonNode> token = Iterables.getLast(path);

        if (token.getToken().equals(LAST_ARRAY_ELEMENT)) {
            target.add(value);
            return ret;
        }

        final int size = target.size();
        final int index;
        try {
            index = Integer.parseInt(token.toString());
        } catch (NumberFormatException ignored) {
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.notAnIndex"));
        }

        if (index < 0 || index > size)
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.noSuchIndex"));

        target.insert(index, value);
        return ret;
    }

    private JsonNode addToObject(final JsonPointer path, final JsonNode node)
    {
        final TokenResolver<JsonNode> token = Iterables.getLast(path);
        final JsonNode ret = node.deepCopy();
        final ObjectNode target = (ObjectNode) path.parent().get(ret);
        target.set(token.getToken().getRaw(), value);
        return ret;
    }
}
