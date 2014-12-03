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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceUtil
{

    public static JsonNode fromResource(final String resource)
            throws IOException
    {
        URL url;
        url = JsonPatchOperationTest.class.getResource(resource);

        if (url == null)
            throw new IOException("resource " + resource + " not found");

        ObjectMapper mapper = JacksonUtils.newMapper();

        ObjectReader reader = mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
                .reader(JsonNode.class);

        InputStream is = null;
        JsonNode node = null;
        try {
            is = url.openStream();
            node = reader.readValue(is);
        } finally {
            if(is != null)
            {
                is.close();
            }
        }
        return node;
    }

}
