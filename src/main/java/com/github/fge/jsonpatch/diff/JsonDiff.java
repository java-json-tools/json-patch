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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;

import java.util.List;

import static com.github.fge.jsonpatch.diff.DiffOperation.*;

/**
 * "Reverse" factorizing JSON Patch implementation
 *
 * <p>This class only has one method, {@link #asJson(JsonNode, JsonNode)}, which
 * takes two JSON values as arguments and returns a patch as a {@link JsonNode}.
 * This generated patch can then be used in {@link
 * JsonPatch#fromJson(JsonNode)}.</p>
 *
 * <p>Numeric equivalence is respected. Operations are always generated in the
 * following order:</p>
 *
 * <ul>
 *     <li>additions,</li>
 *     <li>removals,</li>
 *     <li>replacements.</li>
 * </ul>
 *
 * <p>Array values generate operations in the order of elements. Factorizing is
 * done to merge add and remove into move operations and convert duplicate add
 * to copy operations if values are equivalent. No test operations are
 * generated (they don't really make sense for diffs anyway).</p>
 *
 * <p>Note that due to the way {@link JsonNode} is implemented, this class is
 * inherently <b>not</b> thread safe (since {@code JsonNode} is mutable). It is
 * therefore the responsibility of the caller to ensure that the calling context
 * is safe (by ensuring, for instance, that only the diff operation has
 * references to the values to be diff'ed).</p>
 *
 * @since 1.2
 * @author Randy Watler
 */
public final class JsonDiff
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();
    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

    private JsonDiff()
    {
    }

    /**
     * Generate a JSON patch for transforming the first node into the second
     * node
     *
     * @param first the node to be patched
     * @param second the expected result after applying the patch
     * @return the patch as a {@link JsonNode}
     */
    public static JsonNode asJson(final JsonNode first, final JsonNode second)
    {
        // recursively compute node diffs
        final List<Diff> diffs = Lists.newArrayList();
        generateDiffs(diffs, JsonPointer.empty(), first, second);

        // factorize diffs to optimize patch operations
        DiffFactorizer.factorizeDiffs(diffs);

        // generate patch operations from node diffs
        final ArrayNode patch = FACTORY.arrayNode();
        for (final Diff diff: diffs)
            patch.add(diff.asJsonPatch());
        return patch;
    }

    /**
     * Generate differences between first and second nodes.
     *
     * @param diffs returned ordered differences
     * @param path common parent path for both nodes
     * @param first first node to compare
     * @param second second node to compare
     */
    private static void generateDiffs(final List<Diff> diffs,
        final JsonPointer path, final JsonNode first, final JsonNode second)
    {
        /*
         * If both nodes are equivalent, there is nothing to do
         */
        if (EQUIVALENCE.equivalent(first, second))
            return;

        /*
         * Get both node types. We shortcut to a simple replace operation in the
         * following scenarios:
         *
         * - nodes are not the same type; or
         * - they are the same type, but are not containers (ie, they are
         *   neither objects nor arrays).
         */
        final NodeType firstType = NodeType.getNodeType(first);
        final NodeType secondType = NodeType.getNodeType(second);
        if (firstType != secondType || !first.isContainerNode()) {
            diffs.add(new Diff(REPLACE, path, second.deepCopy()));
            return;
        }

        /*
         * At this point, both nodes are either objects or arrays. Call the
         * appropriate diff generation methods.
         */

        if (firstType == NodeType.OBJECT)
            generateObjectDiffs(diffs, path, first, second);
        else // array
            generateArrayDiffs(diffs, path, first, second);
    }

    /**
     * Generate differences between two object nodes
     *
     * <p>Differences are generated in the following order: added members,
     * removed members, modified members.</p>
     *
     * @param diffs returned ordered differences
     * @param path parent path common to both nodes
     * @param first first object node to compare
     * @param second second object node to compare
     */
    private static void generateObjectDiffs(final List<Diff> diffs,
        final JsonPointer path, final JsonNode first, final JsonNode second)
    {
        // compare different objects fieldwise in predictable order;
        // maintaining order is cosmetic, but facilitates test construction
        final List<String> inFirst = Lists.newArrayList(first.fieldNames());
        final List<String> inSecond = Lists.newArrayList(second.fieldNames());

        List<String> fields;

        // added fields
        fields = Lists.newArrayList(inSecond);
        fields.removeAll(inFirst);
        for (final String added: fields)
            diffs.add(new Diff(ADD, path.append(added),
                    second.get(added).deepCopy()));

        // removed fields
        fields = Lists.newArrayList(inFirst);
        fields.removeAll(inSecond);
        for (final String removed: fields)
            diffs.add(new Diff(REMOVE, path.append(removed),
                first.get(removed).deepCopy()));

        // recursively generate diffs for fields in both objects
        fields = Lists.newArrayList(inFirst);
        fields.retainAll(inSecond);
        for (final String common: fields)
            generateDiffs(diffs, path.append(common), first.get(common),
                    second.get(common));
    }

    /**
     * Generate differences between two array nodes.
     *
     * <p>Differences are generated in order by comparing elements against the
     * longest common subsequence of elements in both arrays.</p>
     *
     * @param diffs returned ordered differences
     * @param path parent pointer of both array nodes
     * @param first first array node to compare
     * @param second second array node to compare
     *
     * @see LCS#getLCS(JsonNode, JsonNode)
     */
    private static void generateArrayDiffs(final List<Diff> diffs,
        final JsonPointer path, final JsonNode first, final JsonNode second)
    {
        // compare array elements linearly using longest common subsequence
        // algorithm applied to the array elements
        final int size1 = first.size();
        final int size2 = second.size();
        final List<JsonNode> lcs = LCS.getLCS(first, second);
        final int lcsSize = lcs.size();

        int index1 = 0;
        int index2 = 0;
        int lcsIndex = 0;

        JsonNode node1;
        JsonNode node2;
        JsonNode lcsNode;

        while (index1 < size1 || index2 < size2) {
            node1 = first.get(index1);
            node2 = second.get(index2);
            lcsNode = lcsIndex < lcsSize ? lcs.get(lcsIndex) : null;
            if (node1 == null) {
                /*
                 * Here, the LCS is "empty", and the first array has no
                 * elements left.
                 *
                 * This means all that remains is additions to the second array.
                 */
                addRemaining(diffs, path, second, index2);
                break;
            }
            if (EQUIVALENCE.equivalent(node1, lcsNode)) {
                if (EQUIVALENCE.equivalent(node1, node2)) {
                    // common subsequence elements
                    index1++;
                    index2++;
                    lcsIndex++;
                } else {
                    // inserted elements
                    diffs.add(new Diff(ADD, path, index1, index2,
                        second.get(index2).deepCopy()));
                    index2++;
                }
            } else if (node2 != null
                && !EQUIVALENCE.equivalent(node2, lcsNode)) {
                // generate diffs for or replaced elements
                if (index1 == index2)
                    generateDiffs(diffs, path.append(index1), node1, node2);
                else
                    diffs.add(new Diff(REPLACE, path, index1, index2,
                        second.get(index2).deepCopy()));
                index1++;
                index2++;
            } else {
                // removed elements
                diffs.add(new Diff(REMOVE, path, index1, index2,
                    first.get(index1).deepCopy()));
                index1++;
            }
        }
    }

    private static void addRemaining(final List<Diff> diffs,
        final JsonPointer path, final JsonNode node2, final int startingIndex)
    {
        final int size = node2.size();

        Diff diff;
        JsonNode node;

        for (int index = startingIndex; index < size; index++) {
            node = node2.get(index).deepCopy();
            diff = new Diff(ADD, path, -1, -1, node);
            diffs.add(diff);
        }
    }
}
