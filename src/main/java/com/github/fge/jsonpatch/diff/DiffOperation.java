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

package com.github.fge.jsonpatch.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.CopyOperation;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.MoveOperation;
import com.github.fge.jsonpatch.RemoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;

final class DiffOperation
{
    private final Type type;
    /* An op's "from", if any */
    private final JsonPointer from;
    /* Value displaced by this operation, if any */
    private final JsonNode oldValue;
    /* An op's "path", if any */
    private final JsonPointer path;
    /* An op's "value", if any */
    private final JsonNode value;

    static DiffOperation add(final JsonPointer path,
        final JsonNode value)
    {
        return new DiffOperation(Type.ADD, null, null, path, value);
    }

    static DiffOperation copy(final JsonPointer from,
        final JsonPointer path, final JsonNode value)
    {
        return new DiffOperation(Type.COPY, from, null, path,
            value);
    }

    static DiffOperation move(final JsonPointer from,
        final JsonNode oldValue, final JsonPointer path,
        final JsonNode value)
    {
        return new DiffOperation(Type.MOVE, from, oldValue, path,
            value);
    }

    static DiffOperation remove(final JsonPointer from,
        final JsonNode oldValue)
    {
        return new DiffOperation(Type.REMOVE, from, oldValue, null, null);
    }

    static DiffOperation replace(final JsonPointer from,
        final JsonNode oldValue, final JsonNode value)
    {
        return new DiffOperation(Type.REPLACE, from, oldValue, null,
            value);
    }

    private DiffOperation(final Type type, final JsonPointer from,
        final JsonNode oldValue, final JsonPointer path,
        final JsonNode value)
    {
        this.type = type;
        this.from = from;
        this.oldValue = oldValue;
        this.path = path;
        this.value = value;
    }

    Type getType()
    {
        return type;
    }

    JsonPointer getFrom()
    {
        return from;
    }

    JsonNode getOldValue()
    {
        return oldValue;
    }

    JsonPointer getPath()
    {
        return path;
    }

    JsonNode getValue()
    {
        return value;
    }

    JsonPatchOperation asJsonPatchOperation()
    {
        return type.toOperation(this);
    }

    enum Type {
        ADD
            {
                @Override
                JsonPatchOperation toOperation(final DiffOperation op)
                {
                    return new AddOperation(op.path, op.value);
                }
            },
        COPY
        {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op)
            {
                return new CopyOperation(op.from, op.path);
            }
        },
        MOVE
        {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op)
            {
                return new MoveOperation(op.from, op.path);
            }
        },
        REMOVE
        {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op)
            {
                return new RemoveOperation(op.from);
            }
        },
        REPLACE
        {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op)
            {
                return new ReplaceOperation(op.from, op.value);
            }
        },
        ;

        abstract JsonPatchOperation toOperation(final DiffOperation op);
    }
}
