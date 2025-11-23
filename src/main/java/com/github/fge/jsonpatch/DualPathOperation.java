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

package com.github.fge.jsonpatch;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.std.ToStringSerializer;
import com.github.fge.jackson.jsonpointer.JsonPointer;

import java.io.IOException;

/**
 * Base class for JSON Patch operations taking two JSON Pointers as arguments
 */
public abstract class DualPathOperation
    extends JsonPatchOperation
{
    @JsonSerialize(using = ToStringSerializer.class)
    protected final JsonPointer from;

    /**
     * Protected constructor
     *
     * @param op operation name
     * @param from source path
     * @param path destination path
     */
    protected DualPathOperation(final String op, final JsonPointer from,
        final JsonPointer path)
    {
        super(op, path);
        this.from = from;
    }

    @Override
    public final void serialize(final JsonGenerator jgen,
        final SerializationContext context)
        throws JacksonException
    {
        jgen.writeStartObject();
        jgen.writeStringProperty("op", op);
        jgen.writeStringProperty("path", path.toString());
        jgen.writeStringProperty("from", from.toString());
        jgen.writeEndObject();
    }

    @Override
    public final void serializeWithType(final JsonGenerator jgen,
        final SerializationContext context, final TypeSerializer typeSer)
        throws JacksonException
    {
        serialize(jgen, context);
    }

    public final JsonPointer getFrom() {
        return from;
    }

    @Override
    public final String toString()
    {
        return "op: " + op + "; from: \"" + from + "\"; path: \"" + path + '"';
    }
}
