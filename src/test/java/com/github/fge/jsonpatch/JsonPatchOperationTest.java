/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNumEquals;
import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public abstract class JsonPatchOperationTest
{
    private static final Class<?> MSGCLASS
        = com.github.fge.jsonpatch.JsonPatchMessages.class;

    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

    private final JsonNode errors;
    private final JsonNode ops;
    private final Class<? extends JsonPatchOperation> opClass;
    private final ObjectReader reader;

    protected JsonPatchOperationTest(final String prefix,
        final Class<? extends JsonPatchOperation> opClass)
        throws IOException
    {
        final String resource = "/jsonpatch/" + prefix + ".json";
        final JsonNode node = JsonLoader.fromResource(resource);
        errors = node.get("errors");
        ops = node.get("ops");
        this.opClass = opClass;
        reader = JacksonUtils.getReader().withType(opClass);
    }

    @DataProvider
    public final Iterator<Object[]> getErrors()
        throws NoSuchFieldException, IllegalAccessException
    {
        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode node: errors)
            list.add(new Object[]{
                node.get("patch"),
                node.get("node"),
                getMessage(node.get("message").textValue())
            });

        return list.iterator();
    }

    @Test(dataProvider = "getErrors")
    public final void errorsAreCorrectlyReported(final JsonNode patch,
        final JsonNode node, final String message)
        throws IOException
    {
        final JsonPatchOperation op = reader.readValue(patch);

        try {
            op.apply(node);
            fail("No exception thrown!!");
        } catch (JsonPatchException e) {
            assertEquals(e.getMessage(), message);
        }
    }

    @DataProvider
    public final Iterator<Object[]> getOps()
    {
        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode node: ops)
            list.add(new Object[]{
                node.get("patch"),
                node.get("node"),
                node.get("expected")
            });

        return list.iterator();
    }

    @Test(dataProvider = "getOps")
    public final void operationsYieldExpectedResults(final JsonNode patch,
        final JsonNode node, final JsonNode expected)
        throws IOException, JsonPatchException
    {
        final JsonPatchOperation op = reader.readValue(patch);
        final JsonNode actual = op.apply(node);

        assertTrue(EQUIVALENCE.equivalent(actual, expected),
            "patched node differs from expectations: expected " + expected
            + " but found " + actual);
        if (EQUIVALENCE.equivalent(node, actual) && node.isContainerNode())
            assertNotSame(node, actual,
                "operation didn't make a copy of the input node");
    }

    private static String getMessage(final String name)
        throws NoSuchFieldException, IllegalAccessException
    {
        return (String) MSGCLASS.getDeclaredField(name).get(null);
    }
}

