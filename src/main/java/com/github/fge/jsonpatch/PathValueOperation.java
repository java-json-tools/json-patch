/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
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
