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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public final class JsonPatchTestSuite
{
    private final JsonNode testNode;

    public JsonPatchTestSuite()
        throws IOException
    {
        testNode = JsonLoader.fromResource("/jsonpatch/testsuite.json");
    }

    @DataProvider
    public Iterator<Object[]> getTests()
        throws IOException
    {
        final List<Object[]> list = Lists.newArrayList();

        boolean valid;
        JsonPatch patch;
        JsonNode source, expected;

        for (final JsonNode element: testNode) {
            if (!element.has("patch"))
                continue;
            patch = JsonPatch.fromJson(element.get("patch"));
            source = element.get("doc");
            expected = element.get("expected");
            if (expected == null)
                expected = source;
            valid = !element.has("error");
            list.add(new Object[]{source, patch, expected, valid});
        }

        return list.iterator();
    }

    @Test(dataProvider = "getTests")
    public void testsFromTestSuitePass(final JsonNode source,
        final JsonPatch patch, final JsonNode expected, final boolean valid)
    {
        try {
            final JsonNode actual = patch.apply(source);
            if (!valid)
                fail("Test was expected to fail!!");
            // Have to do that... TestNG tries to be too smart with regards
            // to iterable collections...
            assertTrue(actual.equals(expected));
        } catch (JsonPatchException ignored) {
            if (valid)
                fail("Test was expected to succeed!!");
        }
    }
}
