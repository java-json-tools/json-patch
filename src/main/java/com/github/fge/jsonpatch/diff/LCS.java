/*
 * Copyright (c) 2013, Randy Watler <watler@wispertel.net>
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jackson.JsonNumEquals;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Longest common subsequence algorithm implementation
 *
 * <p>This is an adaptation of the code found at <a
 * href="http://rosettacode.org/wiki/Longest_common_subsequence#Dynamic_Programming_2">Rosetta
 * Code</a> for {@link ArrayNode} instances.</p>
 *
 * <p>For instance, given these two arrays:</p>
 *
 * <ul>
 *     <li>{@code [ 1, 2, 3, 4, 5, 6, 7, 8, 9 ]},</li>
 *     <li>{@code [ 1, 2, 10, 11, 5, 12, 8, 9 ]}</li>
 * </ul>
 *
 * <p>this code will return {@code [ 1, 2, 5, 8, 9 ]}.</p>
 */
final class LCS
{
    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

    private LCS()
    {
    }

    /**
     * Get the longest common subsequence of elements of two array nodes
     *
     * <p>This is an implementation of the classic 'diff' algorithm often used
     * to compare text files line by line.</p>
     *
     * @param first first array node to compare
     * @param second second array node to compare
     */
    static List<JsonNode> getLCS(final JsonNode first, final JsonNode second)
    {
        Preconditions.checkArgument(first.isArray(),
            "LCS can only work on JSON arrays");
        Preconditions.checkArgument(second.isArray(),
            "LCS can only work on JSON arrays");
        final int minSize = Math.min(first.size(), second.size());

        List<JsonNode> l1 = Lists.newArrayList(first);
        List<JsonNode> l2 = Lists.newArrayList(second);

        final List<JsonNode> ret = head(l1, l2);
        final int headSize = ret.size();

        l1 = l1.subList(headSize, l1.size());
        l2 = l2.subList(headSize, l2.size());

        final List<JsonNode> tail = tail(l1, l2);
        final int trim = tail.size();

        l1 = l1.subList(0, l1.size() - trim);
        l2 = l2.subList(0, l2.size() - trim);

        if (headSize < minSize)
            ret.addAll(doLCS(l1, l2));
        ret.addAll(tail);
        return ret;
    }

    /**
     * Compute longest common subsequence out of two lists
     *
     * <p>When entering this function, both lists are trimmed from their
     * common leading and trailing nodes.</p>
     *
     * @param l1 the first list
     * @param l2 the second list
     * @return the longest common subsequence
     */
    private static List<JsonNode> doLCS(final List<JsonNode> l1,
        final List<JsonNode> l2)
    {
        final List<JsonNode> lcs = Lists.newArrayList();
        // construct LCS lengths matrix
        final int size1 = l1.size();
        final int size2 = l2.size();
        final int[][] lengths = new int[size1 + 1][size2 + 1];

        JsonNode node1;
        JsonNode node2;
        int len;

        for (int i = 0; i < size1; i++)
            for (int j = 0; j < size2; j++) {
                node1 = l1.get(i);
                node2 = l2.get(j);
                len = EQUIVALENCE.equivalent(node1, node2) ? lengths[i][j] + 1
                    : Math.max(lengths[i + 1][j], lengths[i][j + 1]);
                lengths[i + 1][j + 1] = len;
            }

        // return result out of the LCS lengths matrix
        int x = size1, y = size2;
        while (x > 0 && y > 0) {
            if (lengths[x][y] == lengths[x - 1][y])
                x--;
            else if (lengths[x][y] == lengths[x][y - 1])
                y--;
            else {
                lcs.add(l1.get(x - 1));
                x--;
                y--;
            }
        }
        return Lists.reverse(lcs);
    }

    /**
     * Return a list with common head elements of two lists
     *
     * <p>Note that the arguments are NOT altered.</p>
     *
     * @param l1 first list
     * @param l2 second list
     * @return a list of common head elements
     */
    private static List<JsonNode> head(final List<JsonNode> l1,
        final List<JsonNode> l2)
    {
        final List<JsonNode> ret = Lists.newArrayList();
        final int len = Math.min(l1.size(), l2.size());

        JsonNode node;

        for (int index = 0; index < len; index++) {
            node = l1.get(index);
            if (!EQUIVALENCE.equivalent(node, l2.get(index)))
                break;
            ret.add(node);
        }

        return ret;
    }

    /**
     * Return the list of common tail elements of two lists
     *
     * <p>Note that the arguments are NOT altered. Elements are returned in
     * their order of appearance.</p>
     *
     * @param l1 first list
     * @param l2 second list
     * @return a list of common tail elements
     */
    private static List<JsonNode> tail(final List<JsonNode> l1,
        final List<JsonNode> l2)
    {
        final List<JsonNode> l = head(Lists.reverse(l1), Lists.reverse(l2));
        return Lists.reverse(l);
    }
}
