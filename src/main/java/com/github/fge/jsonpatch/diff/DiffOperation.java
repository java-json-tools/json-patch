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
    /* Value at op's from, if any */
    private final JsonNode fromValue;
    /* An op's "path", if any */
    private final JsonPointer path;
    /* Value at op's from, if any */
    private final JsonNode pathValue;
    /* An op's "value", if any */
    private final JsonNode value;

    static DiffOperation add(final JsonPointer path, final JsonNode pathValue, final JsonNode value) {
        return new DiffOperation(Type.ADD, null, null, path, pathValue, value);
    }

    static DiffOperation copy(final JsonPointer from, final JsonNode fromValue,
            final JsonPointer path, final JsonNode pathValue) {
        return new DiffOperation(Type.COPY, from, fromValue, path, pathValue, null);
    }

    static DiffOperation move(final JsonPointer from, final JsonNode fromValue,
            final JsonPointer path, final JsonNode pathValue) {
        return new DiffOperation(Type.MOVE, from, fromValue, path, pathValue, null);
    }

    static DiffOperation remove(final JsonPointer path, final JsonNode pathValue) {
        return new DiffOperation(Type.REMOVE, null, null, path, pathValue, null);
    }

    static DiffOperation replace(final JsonPointer path, final JsonNode pathValue, final JsonNode value) {
        return new DiffOperation(Type.REPLACE, null, null, path, pathValue, value);
    }

    private DiffOperation(final Type type, final JsonPointer from, final JsonNode fromValue,
            final JsonPointer path, final JsonNode pathValue, final JsonNode value) {
        this.type = type;
        this.from = from;
        this.fromValue = fromValue;
        this.path = path;
        this.pathValue = pathValue;
        this.value = value;
    }

    Type getType() { return type; }

    JsonPointer getFrom()
    {
        return from;
    }

    JsonNode getFromValue()
    {
        return fromValue;
    }

    JsonPointer getPath()
    {
        return path;
    }

    JsonNode getPathValue() { return pathValue; }

    JsonNode getValue()
    {
        return value;
    }

    JsonPatchOperation asJsonPatchOperation(final boolean includeHistory)
    {
        return type.toOperation(this, includeHistory);
    }

    enum Type {
        ADD {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op, final boolean includeHistory) {
                return includeHistory ?
                        new AddOperation(op.path, op.value, op.pathValue) :
                        new AddOperation(op.path, op.value);
            }
        },
        COPY {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op, final boolean includeHistory) {
                return includeHistory ?
                        new CopyOperation(op.from, op.path, op.fromValue, op.pathValue) :
                        new CopyOperation(op.from, op.path);
            }
        },
        MOVE {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op, final boolean includeHistory) {
                return includeHistory ?
                        new MoveOperation(op.from, op.path, op.fromValue, op.pathValue) :
                        new MoveOperation(op.from, op.path);
            }
        },
        REMOVE
        {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op, final boolean includeHistory) {
                return includeHistory ?
                        new RemoveOperation(op.path, op.pathValue) :
                        new RemoveOperation(op.path);
            }
        },
        REPLACE {
            @Override
            JsonPatchOperation toOperation(final DiffOperation op, final boolean includeHistory) {
                return includeHistory ?
                        new ReplaceOperation(op.path, op.value, op.pathValue) :
                        new ReplaceOperation(op.path, op.value);
            }
        };

        abstract JsonPatchOperation toOperation(final DiffOperation op, final boolean includeHistory);
    }
}
