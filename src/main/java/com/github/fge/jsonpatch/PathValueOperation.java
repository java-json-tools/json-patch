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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * Base class for patch operations taking a value in addition to a path
 *
 * <p>The JSON value is deserialized using {@link JsonNullAwareDeserializer},
 * since a JSON null is a valid value.</p>
 */
public abstract class PathValueOperation
    extends JsonPatchOperation
{
    @JsonDeserialize(using = JsonNullAwareDeserializer.class)
    protected final JsonNode value;

    /**
     * Protected constructor
     *
     * @param path the JSON Pointer for this operation
     * @param value the JSON value for this operation
     */
    protected PathValueOperation(final JsonPointer path, final JsonNode value)
    {
        super(path);
        this.value = value.deepCopy();
    }

    @Override
    public String toString()
    {
        return super.toString() + ", value = " + value;
    }
}
