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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * Merge patch for a JSON Object
 *
 * <p>This only takes care of the top level, and delegates to other {@link
 * JsonMergePatch} instances for deeper levels.</p>
 */
final class ObjectMergePatch
    extends JsonMergePatch
{
    private final Map<String, JsonNode> fields;
    private final Set<String> removals;

    ObjectMergePatch(final JsonNode content)
    {
        fields = JacksonUtils.asMap(content);
        removals = Sets.newHashSet();

        for (final Map.Entry<String, JsonNode> entry: fields.entrySet())
            if (entry.getValue().isNull())
                removals.add(entry.getKey());

        fields.keySet().removeAll(removals);
    }

    @Override
    public JsonNode apply(final JsonNode input)
        throws JsonPatchException
    {
        if (!input.isObject())
            return mapToNode(fields);

        final Map<String, JsonNode> map = JacksonUtils.asMap(input);

        // Remove all entries which must be removed
        map.keySet().removeAll(removals);

        // Now cycle through what is left
        String memberName;
        JsonNode patchNode;

        for (final Map.Entry<String, JsonNode> entry: map.entrySet()) {
            memberName = entry.getKey();
            patchNode = fields.get(memberName);

            // Leave untouched if no mention in the patch
            if (patchNode == null)
                continue;

            // If the patch node is a primitive type, replace in the result.
            // Reminder: there cannot be a JSON null anymore
            if (!patchNode.isContainerNode()) {
                entry.setValue(patchNode); // no need for .deepCopy()
                continue;
            }

            final JsonMergePatch patch = JsonMergePatch.fromJson(patchNode);
            entry.setValue(patch.apply(entry.getValue()));
        }

        // Finally, if there are members in the patch not present in the input,
        // fill in members
        for (final String key: Sets.difference(fields.keySet(), map.keySet()))
            map.put(key, clearNulls(fields.get(key)));

        return mapToNode(map);
    }

    private static JsonNode mapToNode(final Map<String, JsonNode> map)
    {
        final ObjectNode ret = FACTORY.objectNode();
        return ret.putAll(map);
    }
}
