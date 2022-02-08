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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;

/**
 * JSON Path {@code remove} operation
 *
 * <p>This operation only takes one pointer ({@code path}) as an argument. It
 * is an error condition if no JSON value exists at that pointer.</p>
 */
public final class RemoveOperation extends JsonPatchOperation {
    @JsonCreator
    public RemoveOperation(@JsonProperty("path") final String path) {
        super("remove", path);
    }

    @Override
    public JsonNode apply(final JsonNode node) throws JsonPatchException {
        if (path.isEmpty()) {
            return MissingNode.getInstance();
        }

        final DocumentContext nodeContext = JsonPath.parse(node.deepCopy());
        final String jsonPath = JsonPathParser.tmfStringToJsonPath(path);

        if (nodeContext.read(jsonPath) == null) {
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchPath"));
        }
        return nodeContext
                .delete(jsonPath)
                .read("$", JsonNode.class);
    }

    @Override
    public void serialize(final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("op", "remove");
        jgen.writeStringField("path", path.toString());
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer)
            throws IOException, JsonProcessingException {
        serialize(jgen, provider);
    }

    @Override
    public String toString() {
        return "op: " + op + "; path: \"" + path + '"';
    }
}
