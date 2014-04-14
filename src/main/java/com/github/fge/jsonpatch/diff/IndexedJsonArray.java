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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jackson.JacksonUtils;
import com.google.common.base.Preconditions;

import java.util.List;

final class IndexedJsonArray
{
    private final int size;
    private final JsonNode node;

    private int index = 0;

    IndexedJsonArray(final JsonNode node)
    {
        Preconditions.checkArgument(node.isArray(),
            "Class only applies to JSON arrays");
        this.node = node;
        size = node.size();
    }

    IndexedJsonArray(final List<JsonNode> list)
    {
        final ArrayNode arrayNode = JacksonUtils.nodeFactory().arrayNode();
        arrayNode.addAll(list);
        node = arrayNode;
        size = arrayNode.size();
    }

    int getIndex()
    {
        return isEmpty() ? -1 : index;
    }

    void shift()
    {
        index++;
    }

    JsonNode getElement()
    {
        return node.get(index);
    }

    boolean isEmpty()
    {
        return index >= size;
    }
}
