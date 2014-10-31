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

package com.github.fge.jsonpatch.mergepatch;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.fge.jackson.JacksonUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

final class JsonMergePatchDeserializer
    extends JsonDeserializer<JsonMergePatch>
{
    /*
     * FIXME! UGLY! HACK!
     *
     * We MUST have an ObjectCodec ready so that the parser in .deserialize()
     * can actually do something useful -- for instance, deserializing even a
     * JsonNode.
     *
     * Jackson does not do this automatically; I don't know why...
     */
    private static final ObjectCodec CODEC = JacksonUtils.newMapper();

    @Override
    public JsonMergePatch deserialize(final JsonParser jp,
        final DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // FIXME: see comment above
        jp.setCodec(CODEC);
        final JsonNode node = jp.readValueAsTree();

        /*
         * Not an object: the simple case
         */
        if (!node.isObject())
            return new NonObjectMergePatch(node);

        /*
         * The complicated case...
         *
         * We have to build a set of removed members, plus a map of modified
         * members.
         */

        final Set<String> removedMembers = Sets.newHashSet();
        final Map<String, JsonMergePatch> modifiedMembers = Maps.newHashMap();
        final Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();

        Map.Entry<String, JsonNode> entry;

        while (iterator.hasNext()) {
            entry = iterator.next();
            if (entry.getValue().isNull())
                removedMembers.add(entry.getKey());
            else {
                final JsonMergePatch value
                    = deserialize(entry.getValue().traverse(), ctxt);
                modifiedMembers.put(entry.getKey(), value);
            }
        }

        return new ObjectMergePatch(removedMembers, modifiedMembers);
    }

    /*
     * This method MUST be overriden... The default is to return null, which is
     * not what we want.
     */
    @Override
    public JsonMergePatch getNullValue()
    {
        return new NonObjectMergePatch(NullNode.getInstance());
    }
}
