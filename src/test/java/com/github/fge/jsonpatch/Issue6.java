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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public final class Issue6
{
    private Issue6()
    {
    }

    public static void main(final String... args)
        throws IOException
    {
        deserializePatch();
        deserializeOp();
    }

    // Doesn't work...
    private static void deserializePatch()
        throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        final String jsonPatch = "[" +
            "{\"op\": \"add\", \"path\": \"/foo/bar\", \"value\": \"baz\"}," +
            "{\"op\": \"remove\", \"path\": \"/foo/crux\", " +
            "\"value\": \"baz\"}" +
            "]";

        final JsonPatch patch = mapper.readValue(jsonPatch, JsonPatch.class);

        System.out.printf("Deserialized patch: %s\n", patch);

        final String jsonSer = mapper.writeValueAsString(patch);

        System.out.printf("Serialized Json: %s\n", jsonSer);
    }

    // Doesn't work...
    private static void deserializeOp()
        throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        final String input = "{\"op\": \"add\", \"path\": \"/foo/bar\", \"value\": \"baz\"}";

        final JsonPatchOperation op
            = mapper.readValue(input, JsonPatchOperation.class);

        System.out.printf("Deserialized op: %s\n", op);

        final String jsonSer = mapper.writeValueAsString(op);

        System.out.printf("Serialized Json: %s\n", jsonSer);
    }
}
