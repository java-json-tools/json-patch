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

package com.github.fge.jsonpatch.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.fge.jackson.jsonpointer.JsonPointer;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link JsonPointer}
 *
 * <p>When serializing to JSON, we want a JSON Pointer to appear as its string
 * representation.</p>
 *
 * <p>Note that we don't need a custom _deserializer_ for {@link JsonPointer}:
 * Jackson will automatically pick a constructor with a single String as an
 * argument, and {@link JsonPointer#JsonPointer(String) JsonPointer has such a
 * constructor}.</p>
 *
 * @since 1.5
 */
public final class JsonPointerSerializer
    extends JsonSerializer<JsonPointer>
{
    @Override
    public void serialize(final JsonPointer value, final JsonGenerator jgen,
        final SerializerProvider provider)
        throws IOException
    {
        jgen.writeString(value.toString());
    }
}
