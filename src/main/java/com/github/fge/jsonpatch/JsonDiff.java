package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.jsonpointer.JsonPointer;

public final class JsonDiff
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();

    private JsonDiff()
    {
    }

    public static JsonNode asJson(final JsonNode first, final JsonNode second)
    {
        final ArrayNode ret = FACTORY.arrayNode();

        ObjectNode op;
        if (!JsonNumEquals.getInstance().equivalent(first, second)) {
            op = createOp("replace", JsonPointer.empty());
            op.put("value", second.deepCopy());
            ret.add(op);
        }
        return ret;
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
