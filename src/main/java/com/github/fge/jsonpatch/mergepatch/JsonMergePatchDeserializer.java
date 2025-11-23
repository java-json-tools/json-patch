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

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import com.github.fge.jackson.JacksonUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

final class JsonMergePatchDeserializer
    extends ValueDeserializer<JsonMergePatch>
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
    private static final ObjectMapper CODEC = JacksonUtils.newMapper();
    private static final ObjectReader READER = CODEC.readerFor(JsonMergePatch.class);

    @Override
    public JsonMergePatch deserialize(final JsonParser jp,
        final DeserializationContext ctxt)
        throws JacksonException
    {
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

        final Set<String> removedMembers = new HashSet<String>();
        final Map<String, JsonMergePatch> modifiedMembers = new HashMap<String, JsonMergePatch>();
        
        for (Map.Entry<String, JsonNode> property: node.properties()) {
            if (property.getValue().isNull())
                removedMembers.add(property.getKey());
            else {
                final JsonMergePatch value = READER.readValue(property.getValue());
                modifiedMembers.put(property.getKey(), value);
            }
        }
        
        return new ObjectMergePatch(removedMembers, modifiedMembers);
    }

    /*
     * This method MUST be overriden... The default is to return null, which is
     * not what we want.
     */
    @Override
    public JsonMergePatch getNullValue(DeserializationContext ctxt)
    {
        return new NonObjectMergePatch(NullNode.getInstance());
    }
}
