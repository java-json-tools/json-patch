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

/**
 * JSON Patch {@code replace} operation
 *
 * <p>For this operation, {@code path} points to the value to replace, and
 * {@code value} is the replacement value.</p>
 *
 * <p>It is an error condition if {@code path} does not point to an actual JSON
 * value.</p>
 */
public final class ReplaceOperation
    extends PathValueOperation
{
    @JsonCreator
    public ReplaceOperation(@JsonProperty("path") final JsonPointer path,
        @JsonProperty("value") final JsonNode value)
    {
        super(path, value);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        /*
         * FIXME cannot quite be replaced by a remove + add because of arrays.
         * For instance:
         *
         * { "op": "replace", "path": "/0", "value": 1 }
         *
         * with
         *
         * [ "x" ]
         *
         * If remove is done first, the array is empty and add rightly complains
         * that there is no such index in the array.
         */
        if (path.path(node).isMissingNode())
            throw new JsonPatchException(JsonPatchMessages.NO_SUCH_PATH);
        final JsonNode replacement = value.deepCopy();
        if (path.isEmpty())
            return replacement;
        final SplitPointer split = new SplitPointer(path);
        final JsonNode ret = node.deepCopy();
        final JsonNode parent = split.parent.get(ret);
        final String rawToken = split.lastToken.getToken().getRaw();
        if (parent.isObject())
            ((ObjectNode) parent).put(rawToken, replacement);
        else
            ((ArrayNode) parent).set(Integer.parseInt(rawToken), replacement);
        return ret;
    }

    @Override
    public String toString()
    {
        return "replace: " + super.toString();
    }
}
