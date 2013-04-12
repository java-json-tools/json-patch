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
