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

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * JSON Patch {@code move} operation
 *
 * <p>For this operation, {@code from} points to the value to move, and {@code
 * path} points to the new location of the moved value.</p>
 *
 * <p>As for {@code add}:</p>
 *
 * <ul>
 *     <li>the value at the destination path is either created or replaced;</li>
 *     <li>it is created only if the immediate parent exists;</li>
 *     <li>{@code -} appends at the end of an array.</li>
 * </ul>
 *
 * <p>It is an error condition if {@code from} does not point to a JSON value.
 * </p>
 *
 * <p>The specification adds another rule that the {@code from} path must not be
 * an immediate parent of {@code path}. Unfortunately, that doesn't really work.
 * Consider this patch:</p>
 *
 * <pre>
 *     { "op": "move", "from": "/0", "path": "/0/x" }
 * </pre>
 *
 * <p>Even though {@code /0} is an immediate parent of {@code /0/x}, when this
 * patch is applied to:</p>
 *
 * <pre>
 *     [ "victim", {} ]
 * </pre>
 *
 * <p>it actually succeeds and results in the patched value:</p>
 *
 * <pre>
 *     [ { "x": "victim" } ]
 * </pre>
 */
public final class MoveOperation
    extends DualPathOperation
{
    @JsonCreator
    public MoveOperation(@JsonProperty("from") final JsonPointer from,
        @JsonProperty("path") final JsonPointer path)
    {
        super("move", from, path);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        if (from.equals(path))
            return node.deepCopy();
        final JsonNode movedNode = from.path(node);
        if (movedNode.isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.noSuchPath"));
        final JsonPatchOperation remove = new RemoveOperation(from);
        final JsonPatchOperation add = new AddOperation(path, movedNode);
        return add.apply(remove.apply(node));
    }
}
