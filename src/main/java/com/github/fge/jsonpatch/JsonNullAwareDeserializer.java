/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
