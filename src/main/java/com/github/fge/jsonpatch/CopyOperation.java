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
        super("copy", from, path);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        final JsonNode dupData = from.path(node).deepCopy();
        if (dupData.isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.noSuchPath"));
        return new AddOperation(path, dupData).apply(node);
    }
}
