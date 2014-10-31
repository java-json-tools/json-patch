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

package com.github.fge.jsonpatch.mergepatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchMessages;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public final class ObjectMergePatchTest
{
    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();
    private static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPatchMessages.class);

    private final JsonNode testData;

    public ObjectMergePatchTest()
        throws IOException
    {
        final String resource = "/jsonpatch/mergepatch/patch-object.json";
        testData = JsonLoader.fromResource(resource);
    }

    @Test
    public void patchYellsOnNullInput()
        throws JsonPatchException
    {
        try {
            JsonMergePatch.fromJson(JsonNodeFactory.instance.arrayNode())
                .apply(null);
            fail("No exception thrown!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(),
                BUNDLE.getMessage("jsonPatch.nullValue"));
        }
    }

    @DataProvider
    public Iterator<Object[]> getData()
    {
        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode node: testData)
            list.add(new Object[] {
                node.get("patch"), node.get("victim"), node.get("result")
            });

        return list.iterator();
    }

    @Test(dataProvider = "getData")
    public void patchingWorksAsExpected(final JsonNode input,
        final JsonNode victim, final JsonNode result)
        throws JsonPatchException
    {
        final JsonMergePatch patch = JsonMergePatch.fromJson(input);
        final JsonNode patched = patch.apply(victim);

        assertTrue(EQUIVALENCE.equivalent(result, patched));
    }
}
