package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.collect.Lists;

import java.util.List;

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

        final ObjectNode op = createOp("replace", ptr);
        op.put("value", second.deepCopy());
        ops.add(op);
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
