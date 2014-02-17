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

package com.github.fge.jsonpatch.mergepatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public final class ClearNullsTest
{
    private final JsonNode testNode;

    public ClearNullsTest()
        throws IOException
    {
        testNode = JsonLoader.fromResource("/jsonpatch/mergepatch/clearNulls" +
            ".json");
    }

    @DataProvider
    public Iterator<Object[]> testData()
    {
        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode node: testNode)
            list.add(new Object[] { node.get("orig"), node.get("expected")});

        return list.iterator();
    }

    @Test(dataProvider = "testData")
    public void clearNullsWorksCorrectly(final JsonNode orig,
        final JsonNode expected)
        throws JsonPatchException
    {
        final JsonNode actual = JsonMergePatch.clearNulls(orig);
        assertTrue(actual.equals(expected),
            "clearing failed: have " + actual + ", expected " + expected);
    }
}
