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

package com.github.fge.jsonpatch.diff2;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

final class DiffOperation
{
    private final Type type;
    private final JsonPointer oldPointer;
    private final JsonNode oldValue;
    private final JsonPointer newPointer;
    private final JsonNode newValue;

    static DiffOperation add(final JsonPointer newPointer,
        final JsonNode newValue)
    {
        return new DiffOperation(Type.ADD, null, null, newPointer, newValue);
    }

    static DiffOperation copy(final JsonPointer oldPointer,
        final JsonPointer newPointer, final JsonNode newValue)
    {
        return new DiffOperation(Type.COPY, oldPointer, null, newPointer,
            newValue);
    }

    static DiffOperation move(final JsonPointer oldPointer,
        final JsonNode oldValue, final JsonPointer newPointer,
        final JsonNode newValue)
    {
        return new DiffOperation(Type.MOVE, oldPointer, oldValue, newPointer,
            newValue);
    }

    static DiffOperation remove(final JsonPointer oldPointer,
        final JsonNode oldValue)
    {
        return new DiffOperation(Type.REMOVE, oldPointer, oldValue, null, null);
    }

    static DiffOperation replace(final JsonPointer oldPointer,
        final JsonNode oldValue, final JsonPointer newPointer,
        final JsonNode newValue)
    {
        return new DiffOperation(Type.REPLACE, oldPointer, oldValue, newPointer,
            newValue);
    }

    private DiffOperation(final Type type, final JsonPointer oldPointer,
        final JsonNode oldValue, final JsonPointer newPointer,
        final JsonNode newValue)
    {
        this.type = type;
        this.oldPointer = oldPointer;
        this.oldValue = oldValue;
        this.newPointer = newPointer;
        this.newValue = newValue;
    }

    Type getType()
    {
        return type;
    }

    JsonPointer getOldPointer()
    {
        return oldPointer;
    }

    JsonNode getOldValue()
    {
        return oldValue;
    }

    JsonPointer getNewPointer()
    {
        return newPointer;
    }

    JsonNode getNewValue()
    {
        return newValue;
    }

    enum Type {
        ADD,
        COPY,
        MOVE,
        REMOVE,
        REPLACE,
    }
}
