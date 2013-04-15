/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
