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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * Base class for JSON Patch operations taking two JSON Pointers as arguments
 */
@JsonPropertyOrder({"op", "from", "path"})
public abstract class DualPathOperation
    extends JsonPatchOperation
{
    @JsonSerialize(using = ToStringSerializer.class)
    protected final JsonPointer from;

    /**
     * Protected constructor
     *
     * @param op operation name
     * @param from source path
     * @param path destination path
     */
    protected DualPathOperation(final String op, final JsonPointer from,
        final JsonPointer path)
    {
        super(op, path);
        this.from = from;
    }

    @Override
    public final String toString()
    {
        return "op: " + op + "; from: \"" + from + "\"; path: \"" + path + '"';
    }
}
