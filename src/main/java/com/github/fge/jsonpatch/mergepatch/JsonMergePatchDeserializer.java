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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Custom {@link JsonDeserializer} for {@link JsonMergePatch} instances
 *
 * <p>Unlike "real" JSON Patches (ie, as defined by RFC 6902), JSON merge patch
 * instances are "free form", they can be either JSON arrays or JSON objects
 * without any restriction on the contents; only the content itself may guide
 * the patching process (null elements in arrays, null values in objects).</p>
 *
 * <p>Jackson does not provide a deserializer for such a case; we therefore
 * write our own here.</p>
 */
public final class JsonMergePatchDeserializer
    extends JsonDeserializer<JsonMergePatch>
{
    @Override
    public JsonMergePatch deserialize(final JsonParser jp,
        final DeserializationContext ctxt)
        throws IOException
    {
        final JsonNode node = jp.readValueAs(JsonNode.class);
        if (!node.isContainerNode())
            throw new JsonMappingException("expected either an array or an " +
                "object");
        return node.isArray() ? new ArrayMergePatch(node)
            : new ObjectMergePatch(node);
    }
}
