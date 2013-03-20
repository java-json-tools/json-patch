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
            assertEquals(actual, expected);
        } catch (JsonPatchException ignored) {
            if (valid)
                fail("Test was expected to succeed!!");
        }
    }
}
