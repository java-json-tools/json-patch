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

package com.github.fge.jsonpatch.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

@Test
public abstract class JsonPatchOperationSerializationTest
{
    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

    private final Class<? extends JsonPatchOperation> c;
    private final JsonNode node;
    private final ObjectMapper mapper;

    protected JsonPatchOperationSerializationTest(final String prefix,
        final Class<? extends JsonPatchOperation> c)
        throws IOException
    {
        final String resource = "/jsonpatch/" + prefix + ".json";
        node = JsonLoader.fromResource(resource);
        mapper = JacksonUtils.newMapper();
        this.c = c;
    }

    @DataProvider
    public final Iterator<Object[]> getInputs()
    {
        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode n: node.get("errors"))
            list.add(new Object[] { n.get("op")});

        for (final JsonNode n: node.get("ops"))
            list.add(new Object[] { n.get("op")});

        return list.iterator();
    }

    @Test(dataProvider = "getInputs")
    public final void patchOperationSerializationWorks(final JsonNode input)
        throws IOException
    {
        /*
         * Deserialize a string input
         */
        final String in = input.toString();
        final JsonPatchOperation op
            = mapper.readValue(in, JsonPatchOperation.class);

        /*
         * Check that the class of the operation is what is expected
         */
        assertSame(op.getClass(), c);

        /*
         * Now, write the operation as a String...
         */
        final String out = mapper.writeValueAsString(op);

        /*
         * And read it as a JsonNode again, then test for equality.
         *
         * The reason we do that is that JSON does not guarantee the order of
         * object members; but JsonNode's .equals() method will correctly handle
         * this event, and we trust its .toString().
         */
        final JsonNode output = JacksonUtils.getReader().readTree(out);
        assertTrue(EQUIVALENCE.equivalent(input, output));
    }
}

