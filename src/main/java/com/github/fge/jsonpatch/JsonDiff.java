package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.JacksonUtils;

public final class JsonDiff
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();

    private JsonDiff()
    {
    }

    public static JsonNode asJson(final JsonNode first, final JsonNode second)
    {
        return FACTORY.arrayNode();
    }
}
