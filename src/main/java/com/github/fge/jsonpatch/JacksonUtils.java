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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public final class JacksonUtils {

    private final static JsonNodeFactory FACTORY = JsonNodeFactory.instance;
    private final static int NO_SLASH = -1;
    private final static ObjectReader READER;

    static {
        final ObjectMapper mapper = newMapper();
        READER = mapper.reader();
    }

    private JacksonUtils()
    {
    }

    public final static ObjectMapper newMapper() {
        return new ObjectMapper().setNodeFactory(FACTORY)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Return a preconfigured {@link ObjectReader} to read JSON inputs
     *
     * @return the reader
     * @see #newMapper()
     */
    public static ObjectReader getReader()
    {
        return READER;
    }

    /**
     * Return a preconfigured {@link JsonNodeFactory} to generate JSON data as
     * {@link com.fasterxml.jackson.databind.JsonNode}s
     *
     * @return the factory
     */
    public final static JsonNodeFactory nodeFactory()
    {
        return FACTORY;
    }

    public final static String getLast(JsonPointer jsonPointer)
    {
        String representation = jsonPointer.toString();

        if(representation == null) return "";

        int slashPosition = -1;
        if((slashPosition = representation.lastIndexOf('/')) != -1)
        {
            return representation.substring(slashPosition+1);
        }
        else
        {
            return representation;
        }


    }

    public final static JsonPointer empty()
    {
        return JsonPointer.compile("");
    }

    public final static JsonPointer head(JsonPointer jsonPointer)
    {
        String pointer = jsonPointer.toString();

        int lastSlash = pointer.lastIndexOf('/');

        if(lastSlash == NO_SLASH)
        {
            return empty();
        }
        else
        {
            return JsonPointer.compile(pointer.substring(0, lastSlash));
        }
    }

    public final static JsonPointer append(JsonPointer pointer, String raw)
    {
        String p = pointer.toString();
        return JsonPointer.compile(p + "/" + raw);
    }

    public final static JsonPointer append(JsonPointer pointer, int raw)
    {
        String p = pointer.toString();
        return JsonPointer.compile(p + "/" + Integer.toString(raw));
    }
}

