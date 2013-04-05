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

public final class JsonDiff
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();

    private JsonDiff()
    {
    }

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
         * Deal with keys added to the second node
         */
        final Set<String> added = Sets.difference(secondKeys, firstKeys);

        for (final String fieldName: added) {
            op = createOp("add", ptr.append(fieldName));
            op.put("value", second.get(fieldName).deepCopy());
            ops.add(op);
        }

        /*
         * Deal with keys removed from the second node
         */
        final Set<String> removed = Sets.difference(firstKeys, secondKeys);

        for (final String fieldName: removed)
            ops.add(createOp("remove", ptr.append(fieldName)));

        final Set<String> inCommon = Sets.intersection(firstKeys, secondKeys);

        for (final String fieldName: inCommon)
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
