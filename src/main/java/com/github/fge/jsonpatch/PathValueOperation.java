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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.github.fge.jackson.jsonpointer.JsonPointer;

import java.io.IOException;

/**
 * Base class for patch operations taking a value in addition to a path
 */
public abstract class PathValueOperation
    extends JsonPatchOperation
{
    protected JsonNode value;

    /**
     * The value present at the path location when the patch operation was created. This history is necessary for
     * using json patch with conflict resolution when optimistic locking is involved.
     */
    protected JsonNode pathValue;

    /**
     * Constructor for deserialization. Other parameters are set through the private setters. This allows fields to
     * be represented as null if missing and as default if present and set to null.
     *
     * @param op operation name
     */
    protected PathValueOperation(final String op)
    {
        super(op);
    }

    /**
     * Protected constructor
     *
     * @param op operation name
     * @param path affected path
     * @param value JSON value
     */
    protected PathValueOperation(final String op, final JsonPointer path,
        final JsonNode value)
    {
        this(op, path, value, null);
    }

    /**
     * Protected constructor with history.
     *
     * @param op operation name
     * @param path affected path
     * @param value JSON value
     * @param pathValue JSON value at path prior to operation
     */
    protected PathValueOperation(final String op, final JsonPointer path,
            final JsonNode value, final JsonNode pathValue)
    {
        super(op, path);
        setValue(value);
        setPathValue(pathValue);
    }

    private void setValue(JsonNode value) {
        this.value = value.deepCopy();
    }

    public JsonNode getValue() {
        return value.deepCopy();
    }

    private void setPathValue(JsonNode pathValue) {
        this.pathValue = pathValue == null ? null : pathValue.deepCopy();
    }

    public JsonNode getPathValue() {
        return pathValue == null ? null : pathValue.deepCopy();
    }

    @Override
    public final void serialize(final JsonGenerator jgen,
        final SerializerProvider provider)
        throws IOException
    {
        jgen.writeStartObject();
        jgen.writeStringField("op", op);
        jgen.writeStringField("path", path.toString());
        jgen.writeFieldName("value");
        jgen.writeTree(value);
        if (null != pathValue) {
            jgen.writeFieldName("pathValue");
            jgen.writeTree(pathValue);
        }

        jgen.writeEndObject();
    }

    @Override
    public final void serializeWithType(final JsonGenerator jgen,
        final SerializerProvider provider, final TypeSerializer typeSer)
        throws IOException
    {
        serialize(jgen, provider);
    }

    @Override
    public final String toString()
    {
        return "op: " + op + "; path: \"" + path + "\"; value: " + value;
    }
}
