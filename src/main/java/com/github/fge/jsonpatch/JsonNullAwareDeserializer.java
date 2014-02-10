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

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Custom deserializer for {@link JsonNode}
 *
 * <p>Up to, and including, versions 2.1.4, Jackson's {@link
 * JsonNodeDeserializer} deserializes a JSON null value as {@code null}. This
 * custom deserializer deserializes it as a {@link NullNode} instead.</p>
 *
 * <p>See also <a
 * href="https://github.com/FasterXML/jackson-databind/issues/186">here</a>.</p>
 */
public final class JsonNullAwareDeserializer
    extends JsonNodeDeserializer
{
    @Override
    public JsonNode getNullValue()
    {
        return NullNode.getInstance();
    }
}
