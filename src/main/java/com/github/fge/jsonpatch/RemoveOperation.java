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
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * JSON Path {@code remove} operation
 *
 * <p>This operation only takes one pointer ({@code path}) as an argument. It
 * is an error condition if no JSON value exists at that pointer.</p>
 */
public final class RemoveOperation
    extends JsonPatchOperation
{
    @JsonCreator
    public RemoveOperation(@JsonProperty("path") final JsonPointer path)
    {
        super(path);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        if (path.isEmpty())
            return MissingNode.getInstance();
        if (path.path(node).isMissingNode())
            throw new JsonPatchException(JsonPatchMessages.NO_SUCH_PATH);
        final SplitPointer split = new SplitPointer(path);
        final JsonNode ret = node.deepCopy();
        final JsonNode parentNode = split.parent.get(ret);
        final String raw = split.lastToken.getToken().getRaw();
        if (parentNode.isObject())
            ((ObjectNode) parentNode).remove(raw);
        else
            ((ArrayNode) parentNode).remove(Integer.parseInt(raw));
        return ret;
    }

    @Override
    public String toString()
    {
        return "remove: " + super.toString();
    }
}
