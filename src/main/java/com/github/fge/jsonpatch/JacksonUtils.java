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


import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public final class JacksonUtils
{
    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;
    private static final ObjectReader READER;

    static {
        final ObjectMapper mapper = newMapper();
        READER = mapper.reader();
    }

    private JacksonUtils()
    {
        throw new Error("nice try!");
    }

    public static ObjectMapper newMapper()
    {
        return new ObjectMapper().setNodeFactory(FACTORY)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
    }

    /**
     * Return a preconfigured {@link ObjectReader} to read JSON inputs
     *
     * @return the reader
     *
     * @see #newMapper()
     */
    public static ObjectReader getReader()
    {
        return READER;
    }

    /**
     * Return a preconfigured {@link JsonNodeFactory} to generate JSON data as
     * {@link JsonNode}s
     *
     * @return the factory
     */
    public static JsonNodeFactory nodeFactory()
    {
        return FACTORY;
    }

    public static String getLast(final JsonPointer jsonPointer)
    {
        final String representation = jsonPointer.toString();

        // TODO: in theory a .toString() never returns null, so what the...
        if (representation == null)
            return "";

        final int slashPosition = representation.lastIndexOf('/');
        return slashPosition == -1 ? representation
            : representation.substring(slashPosition + 1);
    }

    public static JsonPointer empty()
    {
        return JsonPointer.compile("");
    }

    public static JsonPointer head(final JsonPointer jsonPointer)
    {
        final String pointer = jsonPointer.toString();
        final int lastSlash = pointer.lastIndexOf('/');

        return lastSlash == -1 ? empty()
            : JsonPointer.compile(pointer.substring(0, lastSlash));
    }

    public static JsonPointer append(final JsonPointer pointer, final String
        raw)
    {
        final String p = pointer.toString();
        return JsonPointer.compile(p + '/' + raw);
    }

    public static JsonPointer append(final JsonPointer pointer, final int raw)
    {
        final String p = pointer.toString();
        return JsonPointer.compile(p + '/' + Integer.toString(raw));
    }
}

