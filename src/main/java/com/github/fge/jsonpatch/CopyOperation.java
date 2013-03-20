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
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * JSON Patch {@code copy} operation
 *
 * <p>For this operation, {@code from} is the JSON Pointer of the value to copy,
 * and {@code path} is the destination where the value should be copied.</p>
 *
 * <p>As for {@code add}:</p>
 *
 * <ul>
 *     <li>the value at the destination path is either created or replaced;</li>
 *     <li>it is created only if the immediate parent exists;</li>
 *     <li>{@code -} appends at the end of an array.</li>
 * </ul>
 *
 * <p>It is an error if {@code from} fails to resolve to a JSON value.</p>
 */
public final class CopyOperation
    extends DualPathOperation
{
    @JsonCreator
    public CopyOperation(@JsonProperty("from") final JsonPointer from,
        @JsonProperty("path") final JsonPointer path)
    {
        super(from, path);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        final JsonNode dupData = from.path(node).deepCopy();
        if (dupData.isMissingNode())
            throw new JsonPatchException(JsonPatchMessages.NO_SUCH_PATH);
        return new AddOperation(path, dupData).apply(node);
    }

    @Override
    public String toString()
    {
        return "copy: " + super.toString();
    }
}
