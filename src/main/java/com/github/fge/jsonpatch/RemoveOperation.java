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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.collect.Iterables;

import java.io.IOException;

/**
 * JSON Path {@code remove} operation
 *
 * <p>This operation only takes one pointer ({@code path}) as an argument. It
 * is an error condition if no JSON value exists at that pointer.</p>
 */
public final class RemoveOperation
    extends JsonPatchOperation
{
    /**
     * The value present at the path location when the patch operation was created. This history is necessary for
     * using json patch with conflict resolution when optimistic locking is involved.
     */
    protected JsonNode pathValue;

    @JsonCreator
    private RemoveOperation()
    {
        super("remove");
    }

    public RemoveOperation(final JsonPointer path)
    {
        this(path, null);
    }

    public RemoveOperation(final JsonPointer path, final JsonNode pathValue)
    {
        super("remove", path);
        setPathValue(pathValue);
    }

    private void setPathValue(JsonNode pathValue) {
        this.pathValue = pathValue == null ? null : pathValue.deepCopy();
    }

    public JsonNode getPathValue() {
        return pathValue == null ? null : pathValue.deepCopy();
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        if (path.isEmpty())
            return MissingNode.getInstance();
        if (path.path(node).isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.noSuchPath"));
        final JsonNode ret = node.deepCopy();
        final JsonNode parentNode = path.parent().get(ret);
        final String raw = Iterables.getLast(path).getToken().getRaw();
        if (parentNode.isObject())
            ((ObjectNode) parentNode).remove(raw);
        else
            ((ArrayNode) parentNode).remove(Integer.parseInt(raw));
        return ret;
    }

    @Override
    public void serialize(final JsonGenerator jgen,
        final SerializerProvider provider)
        throws IOException
    {
        jgen.writeStartObject();
        jgen.writeStringField("op", "remove");
        jgen.writeStringField("path", path.toString());
        if (null != pathValue) {
            jgen.writeFieldName("pathValue");
            jgen.writeTree(pathValue);
        }
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(final JsonGenerator jgen,
        final SerializerProvider provider, final TypeSerializer typeSer)
        throws IOException
    {
        serialize(jgen, provider);
    }

    @Override
    public String toString()
    {
        return "op: " + op + "; path: \"" + path + '"';
    }
}
