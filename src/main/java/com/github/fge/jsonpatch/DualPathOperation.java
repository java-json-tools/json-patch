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

import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * Base class for JSON Patch operations taking two JSON Pointers as arguments
 */
public abstract class DualPathOperation
    extends JsonPatchOperation
{
    protected final JsonPointer from;

    protected DualPathOperation(final JsonPointer from,
        final JsonPointer path)
    {
        super(path);
        this.from = from;
    }

    @Override
    public String toString()
    {
        return "from = \"" + from + "', " + super.toString();
    }
}
