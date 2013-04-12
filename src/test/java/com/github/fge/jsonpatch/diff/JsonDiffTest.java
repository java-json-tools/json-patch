package com.github.fge.jsonpatch.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public final class JsonDiffTest
{
    private static final Equivalence<JsonNode> EQUIVALENCE
            = JsonNumEquals.getInstance();

    private final JsonNode data;

    public JsonDiffTest()
            throws IOException
    {
        data = JsonLoader.fromResource("/jsonpatch/factorizing-diff.json");
    }

    @DataProvider
    public Iterator<Object[]> getData()
    {
        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode node: data)
            list.add(new Object[] { node.get("first"), node.get("second"),
                node.get("patch") });

        return list.iterator();
    }

    @Test(dataProvider = "getData")
    public void diffsAreCorrectlyComputed(final JsonNode first,
        final JsonNode second, final JsonNode expected)
        throws IOException, JsonPatchException
    {
        final JsonNode actual = JsonDiff.asJson(first, second);
        assertEquals(actual, expected,
            "generated patch differs from expectations");

        final JsonPatch patch = JsonPatch.fromJson(actual);
        assertTrue(EQUIVALENCE.equivalent(patch.apply(first), second),
            "reapplying generated patch does not generate the correct result");
    }


}
