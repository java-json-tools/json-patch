/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonpatch.mergepatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.NodeType;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchMessages;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;

import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of <a href="http://tools.ietf.org/html/draft-ietf-appsawg-json-merge-patch-02">JSON merge patch</a>
 *
 * <p>Unlike JSON Patch, JSON Merge Patch only applies to JSON Objects or JSON
 * arrays.</p>
 */
public abstract class JsonMergePatch
{
    protected static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

    protected static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPatchMessages.class);

    public abstract JsonNode apply(final JsonNode input)
        throws JsonPatchException;

    public static JsonMergePatch fromJson(final JsonNode input)
        throws JsonPatchException
    {
        BUNDLE.checkNotNull(input, "jsonPatch.nullInput");
        BUNDLE.checkArgumentPrintf(input.isContainerNode(),
            "mergePatch.notContainer", NodeType.getNodeType(input));

        return input.isArray() ? new ArrayMergePatch(input)
            : new ObjectMergePatch(input);
    }

    protected static JsonNode clearNulls(final JsonNode node)
    {
        if (!node.isContainerNode())
            return node;

        return node.isArray() ? clearNullsFromArray(node)
            : clearNullsFromObject(node);
    }

    private static JsonNode clearNullsFromArray(final JsonNode node)
    {
        final ArrayNode ret = FACTORY.arrayNode();

        // Unclear whether null elements should be removed... Right now they are
        for (final JsonNode element: node)
            if (!element.isNull())
                ret.add(clearNulls(element));

        return ret;
    }

    private static JsonNode clearNullsFromObject(final JsonNode node)
    {
        final ObjectNode ret = FACTORY.objectNode();
        final Iterator<Map.Entry<String, JsonNode>> iterator
            = node.fields();

        Map.Entry<String, JsonNode> entry;
        JsonNode value;

        while (iterator.hasNext()) {
            entry = iterator.next();
            value = entry.getValue();
            if (!value.isNull())
                ret.put(entry.getKey(), clearNulls(value));
        }

        return ret;
    }
}
