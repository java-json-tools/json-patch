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
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

// Non final because we want to be able to Mockito.spy() on it
class DiffProcessor
{
    private final Map<JsonPointer, JsonNode> unchanged;

    private final List<DiffOperation> diffs = Lists.newArrayList();

    DiffProcessor(final Map<JsonPointer, JsonNode> unchanged)
    {
        this.unchanged = ImmutableMap.copyOf(unchanged);
    }

    void valueReplaced(final JsonPointer pointer, final JsonNode oldValue,
        final JsonNode newValue)
    {
        diffs.add(DiffOperation.replace(pointer, oldValue, newValue));
    }

    void valueRemoved(final JsonPointer pointer, final JsonNode value)
    {
        diffs.add(DiffOperation.remove(pointer, value));
    }

    void valueAdded(final JsonPointer pointer, final JsonNode value)
    {
        diffs.add(DiffOperation.add(pointer, value));
    }

    JsonPatch getPatch()
    {
        final List<JsonPatchOperation> list = Lists.newArrayList();

        for (final DiffOperation op: diffs)
            list.add(op.asJsonPatchOperation());

        return new JsonPatch(list);
    }
}
