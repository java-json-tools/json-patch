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
 * Base class for JSON Patch operations taking two JSON Pointers as arguments
 */
public abstract class DualPathOperation
    extends JsonPatchOperation
{
    protected JsonPointer from;

    /**
     * The value present at the path location when the patch operation was created. This history is necessary for
     * using json patch with conflict resolution when optimistic locking is involved.
     */
    protected JsonNode pathValue;

    /**
     * The value present at the from path location when the patch operation was created. This history is necessary for
     * using json patch with conflict resolution when optimistic locking is involved.
     */
    protected JsonNode fromValue;

    /**
     * Constructor for deserialization. Other parameters are set through the private setters. This allows fields to
     * be represented as null if missing and a default if present and set to null.
     *
     * @param op operation name
     */
    protected DualPathOperation(final String op)
    {
        super(op);
    }

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
        this(op, from, path, null, null);
    }

    /**
     * Protected constructor with history.
     *
     * @param op operation name
     * @param path affected path
     * @param fromValue JSON value at from path prior to operation
     * @param pathValue JSON value at path prior to operation
     */
    protected DualPathOperation(final String op, final JsonPointer from,
            final JsonPointer path, final JsonNode fromValue, final JsonNode pathValue)
    {
        super(op, path);
        setFrom(from);
        setPathValue(pathValue);
        setFromValue(fromValue);
    }

    private void setFrom(JsonPointer from) {
        this.from = from;
    }

    public JsonPointer getFrom() {
        return from;
    }

    private void setPathValue(JsonNode pathValue) {
        this.pathValue = pathValue == null ? null : pathValue.deepCopy();
    }

    public JsonNode getPathValue() {
        return pathValue == null ? null : pathValue.deepCopy();
    }

    private void setFromValue(JsonNode fromValue) {
        this.fromValue = fromValue == null ? null : fromValue.deepCopy();
    }

    public JsonNode getFromValue() {
        return fromValue == null ? null : fromValue.deepCopy();
    }

    @Override
    public final void serialize(final JsonGenerator jgen,
        final SerializerProvider provider)
        throws IOException
    {
        jgen.writeStartObject();
        jgen.writeStringField("op", op);
        jgen.writeStringField("path", path.toString());
        jgen.writeStringField("from", from.toString());
        if (null != pathValue) {
            jgen.writeFieldName("pathValue");
            jgen.writeTree(pathValue);
        }
        if (null != fromValue) {
            jgen.writeFieldName("fromValue");
            jgen.writeTree(fromValue);
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
        return "op: " + op + "; from: \"" + from + "\"; path: \"" + path + '"';
    }
}
