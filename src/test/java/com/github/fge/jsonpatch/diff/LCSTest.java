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

package com.github.fge.jsonpatch.diff;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.JacksonUtils;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;

public final class LCSTest
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();

    @DataProvider
    public Iterator<Object[]> getData()
    {
        final List<Object[]> list = Lists.newArrayList();

        List<Integer> l1, l2, ret;

        l1 = ImmutableList.of();
        l2 = ImmutableList.of();
        ret = ImmutableList.of();
        list.add(new Object[]{ l1, l2, ret });

        l1 = ImmutableList.of(1);
        l2 = ImmutableList.of();
        ret = ImmutableList.of();
        list.add(new Object[]{ l1, l2, ret });

        l1 = ImmutableList.of(1, 2);
        l2 = ImmutableList.of(1);
        ret = ImmutableList.of(1);
        list.add(new Object[]{ l1, l2, ret });

        l1 = ImmutableList.of(1, 2);
        l2 = ImmutableList.of(1, 2);
        ret = ImmutableList.of(1, 2);
        list.add(new Object[]{ l1, l2, ret });

        l1 = ImmutableList.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        l2 = ImmutableList.of(1, 2, 10, 11, 5, 12, 8, 9);
        ret = ImmutableList.of(1, 2, 5, 8, 9);
        list.add(new Object[]{ l1, l2, ret });

        return list.iterator();
    }

    @Test(dataProvider = "getData")
    public void LCSIsCorrectlyComputed(final List<Integer> l1,
        final List<Integer> l2, final List<Integer> ret)
    {
        final ArrayNode node1 = FACTORY.arrayNode();
        node1.addAll(toNodeList(l1));
        final ArrayNode node2 = FACTORY.arrayNode();
        node2.addAll(toNodeList(l2));
        final List<JsonNode> expected = toNodeList(ret);

        List<JsonNode> list;

        // Test both ways: LCS should be symmetric
        list = LCS.getLCS(node1, node2);
        assertEquals(list, expected);
        list = LCS.getLCS(node2, node1);
        assertEquals(list, expected);
    }

    private static List<JsonNode> toNodeList(final List<Integer> l)
    {
        final List<JsonNode> ret = Lists.newArrayList();

        for (final int i: l)
            ret.add(FACTORY.numberNode(i));

        return ret;
    }

}
