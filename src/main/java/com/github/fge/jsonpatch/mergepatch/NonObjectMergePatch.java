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

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsontype.TypeSerializer;
import com.github.fge.jsonpatch.JsonPatchException;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

@ParametersAreNonnullByDefault
final class NonObjectMergePatch
    extends JsonMergePatch
{
    private final JsonNode node;

    NonObjectMergePatch(final JsonNode node)
    {
        if (node == null) {
            throw new NullPointerException();
        }
        this.node = node;
    }

    @Override
    public JsonNode apply(final JsonNode input)
        throws JsonPatchException
    {
        BUNDLE.checkNotNull(input, "jsonPatch.nullValue");
        return node;
    }

    @Override
    public void serialize(final JsonGenerator jgen,
        final SerializationContext context)
        throws JacksonException
    {
        jgen.writeTree(node);
    }

    @Override
    public void serializeWithType(final JsonGenerator jgen,
        final SerializationContext context, final TypeSerializer typeSer)
        throws JacksonException
    {
        serialize(jgen, context);
    }
}
