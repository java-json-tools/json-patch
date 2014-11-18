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
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.google.common.base.Equivalence;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

// TODO: cleanup
final class DiffProcessor
{
    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

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
        final int removalIndex = findPreviouslyRemoved(value);
        if (removalIndex != -1) {
            final DiffOperation removed = diffs.get(removalIndex);
            diffs.remove(removalIndex);
            diffs.add(DiffOperation.move(removed.getFrom(),
                value, pointer, value));
            return;
        }
        final JsonPointer ptr = findUnchangedValue(value);
        final DiffOperation op = ptr != null
            ? DiffOperation.copy(ptr, pointer, value)
            : DiffOperation.add(pointer, value);

        diffs.add(op);
    }

    JsonPatch getPatch()
    {
        final List<JsonPatchOperation> list = Lists.newArrayList();

        for (final DiffOperation op: diffs)
            list.add(op.asJsonPatchOperation());

        return new JsonPatch(list);
    }

    @Nullable
    private JsonPointer findUnchangedValue(final JsonNode value)
    {
        final Predicate<JsonNode> predicate = EQUIVALENCE.equivalentTo(value);
        for (final Map.Entry<JsonPointer, JsonNode> entry: unchanged.entrySet())
            if (predicate.apply(entry.getValue()))
                return entry.getKey();
        return null;
    }

    private int findPreviouslyRemoved(final JsonNode value)
    {
        final Predicate<JsonNode> predicate = EQUIVALENCE.equivalentTo(value);

        DiffOperation op;

        for (int i = 0; i < diffs.size(); i++) {
            op = diffs.get(i);
            if (op.getType() == DiffOperation.Type.REMOVE
                && predicate.apply(op.getOldValue()))
                return i;
        }
        return -1;
    }
}
