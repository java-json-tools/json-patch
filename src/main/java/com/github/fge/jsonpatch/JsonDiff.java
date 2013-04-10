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

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * "Reverse" JSON Patch implementation
 *
 * <p>This class only has one method, {@link #asJson(JsonNode, JsonNode)}, which
 * takes two JSON values as arguments and returns a patch as a {@link JsonNode}.
 * This generated patch can then be used in {@link
 * JsonPatch#fromJson(JsonNode)}.</p>
 *
 * <p>Numeric equivalence is respected. When dealing with container values (ie,
 * objects or arrays), operations are always generated in the following order:
 * </p>
 *
 * <ul>
 *     <li>additions,</li>
 *     <li>removals,</li>
 *     <li>replacements.</li>
 * </ul>
 *
 * <p>Note that due to the way {@link JsonNode} is implemented, this class is
 * inherently <b>not</b> thread safe (since {@code JsonNode} is mutable). It is
 * therefore the responsibility of the caller to ensure that the calling context
 * is safe (by ensuring, for instance, that only the diff operation has
 * references to the values to be diff'ed).</p>
 *
 * <p>Note also that currently, no effort is made to "factorize" operations.
 * That is, generating the patch for the following two nodes:</p>
 *
 * <ul>
 *     <li>{@code { "a": "b" }} (first),</li>
 *     <li>{@code { "c": "b" }} (second)</li>
 * </ul>
 *
 * <p>will generate:</p>
 *
 * <pre>
 *     [
 *         { "op": "add": "path": "/c", "value": "b" },
 *         { "op": "remove", "path": "/a" }
 *     ]
 * </pre>
 *
 * <p>even though a shorter version would be:</p>
 *
 * <pre>
 *     [ { "op": "move": "from": "/a", "path": "/c" } ]
 * </pre>
 */
public final class JsonDiff
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();

    private JsonDiff()
    {
    }

    /**
     * Generate a patch for transforming the first node into the second node
     *
     * @param first the node to be patched
     * @param second the expected result after applying the patch
     * @return the patch as a {@link JsonNode}
     */
    public static JsonNode asJson(final JsonNode first, final JsonNode second)
    {
        final ArrayNode ret = FACTORY.arrayNode();
        final List<JsonNode> ops = Lists.newArrayList();

        genDiff(ops, JsonPointer.empty(), first, second);
        ret.addAll(ops);
        return ret;
    }

    private static void genDiff(final List<JsonNode> ops, final JsonPointer ptr,
        final JsonNode first, final JsonNode second)
    {
        if (JsonNumEquals.getInstance().equivalent(first, second))
            return;

        final NodeType firstType = NodeType.getNodeType(first);
        final NodeType secondType = NodeType.getNodeType(second);

        /*
         * If types are different, this is a replace operation.
         *
         * If types are the same BUT nodes are not containers (ie, objects or
         * arrays), then this is also a replace operation.
         */
        if (firstType != secondType || !first.isContainerNode()) {
            final ObjectNode op = createOp("replace", ptr);
            op.put("value", second.deepCopy());
            ops.add(op);
            return;
        }

        /*
         * Otherwise, recurse into object members/array elements.
         */
        if (firstType == NodeType.OBJECT)
            genObjectDiff(ops, ptr, first, second);
        else
            genArrayDiff(ops, ptr, first, second);
    }

    private static void genObjectDiff(final List<JsonNode> ops,
        final JsonPointer ptr, final JsonNode first, final JsonNode second)
    {
        final Set<String> firstKeys = Sets.newHashSet(first.fieldNames());
        final Set<String> secondKeys = Sets.newHashSet(second.fieldNames());

        ObjectNode op;

        /*
         * Deal with added members
         */
        for (final String fieldName: Sets.difference(secondKeys, firstKeys)) {
            op = createOp("add", ptr.append(fieldName));
            op.put("value", second.get(fieldName).deepCopy());
            ops.add(op);
        }

        /*
         * Deal with removed members
         */
        for (final String fieldName: Sets.difference(firstKeys, secondKeys))
            ops.add(createOp("remove", ptr.append(fieldName)));

        /*
         * Deal with modified members
         */
        for (final String fieldName: Sets.intersection(firstKeys, secondKeys))
            genDiff(ops, ptr.append(fieldName), first.get(fieldName),
                second.get(fieldName));
    }

    private static void genArrayDiff(final List<JsonNode> ops,
        final JsonPointer ptr, final JsonNode first, final JsonNode second)
    {
        final int firstSize = first.size();
        final int secondSize = second.size();

        ObjectNode op;

        /*
         * Deal with added elements
         */
        for (int index = firstSize; index < secondSize; index++) {
            op = createOp("add", ptr.append("-"));
            op.put("value", second.get(index).deepCopy());
            ops.add(op);
        }

        /*
         * Deal with removed elements
         */
        for (int index = firstSize - 1; index >= secondSize; index--)
            ops.add(createOp("remove", ptr.append(index)));

        /*
         * Deal with modified elements
         */
        final int size = Math.min(firstSize, secondSize);
        for (int index = 0; index < size; index++)
            genDiff(ops, ptr.append(index), first.get(index),
                second.get(index));
    }

    private static ObjectNode createOp(final String name,
        final JsonPointer ptr)
    {
        final ObjectNode ret = FACTORY.objectNode();
        ret.put("op", name);
        ret.put("path", ptr.toString());
        return ret;
    }
}
