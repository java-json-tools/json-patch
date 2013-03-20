/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        super(path, value);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        if (path.isEmpty())
            return value;

        /*
         * Check the parent node: it must at the very least exist for the add
         * operation to work
         */
        final SplitPointer split = new SplitPointer(path);
        final JsonNode parentNode = split.parent.path(node);
        if (parentNode.isMissingNode())
            throw new JsonPatchException(JsonPatchMessages.NO_SUCH_PARENT);
        return parentNode.isArray()
            ? addToArray(split, node)
            : addToObject(split, node);
    }

    private JsonNode addToArray(final SplitPointer split, final JsonNode node)
        throws JsonPatchException
    {
        final JsonNode ret = node.deepCopy();
        final ArrayNode target = (ArrayNode) split.parent.get(ret);
        final TokenResolver<JsonNode> token = split.lastToken;

        if (token.getToken().equals(LAST_ARRAY_ELEMENT)) {
            target.add(value);
            return ret;
        }

        final int size = target.size();
        final int index;
        try {
            index = Integer.parseInt(token.toString());
        } catch (NumberFormatException ignored) {
            throw new JsonPatchException(JsonPatchMessages.NOT_AN_INDEX);
        }

        if (index < 0 || index > size)
            throw new JsonPatchException(JsonPatchMessages.NO_SUCH_INDEX);

        target.insert(index, value);
        return ret;
    }

    private JsonNode addToObject(final SplitPointer split, final JsonNode node)
    {
        final JsonNode ret = node.deepCopy();
        final ObjectNode target = (ObjectNode) split.parent.get(ret);
        target.put(split.lastToken.getToken().getRaw(), value);
        return ret;
    }

    @Override
    public String toString()
    {
        return "add: " + super.toString();
    }
}
