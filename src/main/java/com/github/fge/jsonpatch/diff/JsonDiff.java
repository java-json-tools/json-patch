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
            diffs.add(Diff.simpleDiff(REPLACE, path, second));
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
            diffs.add(Diff.simpleDiff(ADD, path.append(added),
                second.get(added)));

        // removed fields
        fields = Lists.newArrayList(inFirst);
        fields.removeAll(inSecond);
        for (final String removed: fields)
            diffs.add(Diff.simpleDiff(REMOVE, path.append(removed),
                first.get(removed)));

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
        final List<JsonNode> lcs = LCS.getLCS(first, second);

        final IndexedJsonArray array1 = new IndexedJsonArray(first);
        final IndexedJsonArray array2 = new IndexedJsonArray(second);
        final IndexedJsonArray lcsArray = new IndexedJsonArray(lcs);

        preLCS(diffs, path, lcsArray, array1, array2);
        inLCS(diffs, path, lcsArray, array1, array2);
        postLCS(diffs, path, array1, array2);
    }

    /*
     * First method entered when computing array diffs. It will exit early if
     * the LCS is empty.
     *
     * If the LCS is not empty, it means that both array1 and  array2 have at
     * least one element left. In such a situation, this method will run until
     * elements extracted from both arrays are equivalent to the first element
     * of the LCS.
     */
    private static void preLCS(final List<Diff> diffs, final JsonPointer path,
        final IndexedJsonArray lcs, final IndexedJsonArray array1,
        final IndexedJsonArray array2)
    {
        if (lcs.isEmpty())
            return;
        /*
         * This is our sentinel: if nodes from both the first array and the
         * second array are equivalent to this done, we are done.
         */
        final JsonNode sentinel = lcs.getElement();

        /*
         * Those two variables hold nodes for the first and second array in the
         * main loop.
         */
        JsonNode node1;
        JsonNode node2;

        /*
         * This records the number of equivalences between the LCS node and
         * nodes from array1 and array2.
         */
        int nrEquivalences;

        while (true) {
            /*
             * At each step, we reset the number of equivalences to 0.
             */
            nrEquivalences = 0;
            node1 = array1.getElement();
            node2 = array2.getElement();
            if (EQUIVALENCE.equivalent(sentinel, node1))
                nrEquivalences++;
            if (EQUIVALENCE.equivalent(sentinel, node2))
                nrEquivalences++;
            /*
             * If both node1 and node2 are equivalent to our sentinel, we are
             * done; this is our exit condition.
             */
            if (nrEquivalences == 2)
                return;
            /*
             * If none of them are equivalent to the LCS node, compute diffs
             * in first array so that the element in this array's index be
             * transformed into the matching element in the second array; then
             * restart the loop.
             *
             * Note that since we are using an LCS, and no element of either
             * array is equivalent to the first element of the LCS (our
             * sentinel), a consequence is that indices in both arrays are
             * equal. In the path below, we could have equally used the index
             * from array2.
             */
            if (nrEquivalences == 0) {
                generateDiffs(diffs, path.append(array1.getIndex()), node1,
                    node2);
                array1.shift();
                array2.shift();
                continue;
            }
            /*
             * If we reach this point, one array has to catch up in order to
             * reach the first element of the LCS. The logic is as follows:
             *
             * - if the first array has to catch up, it means this array's
             *   element has been removed from the second array;
             * - if the second array has to catch up, it means the first array's
             *   element is being inserted into the second array.
             */
            if (!EQUIVALENCE.equivalent(sentinel, node1)) {
                diffs.add(Diff.arrayRemove(path, array1, array2));
                array1.shift();
            } else { // !match2, as a consequence, since match is exclusive
                diffs.add(Diff.arrayInsert(path, array1, array2));
                array2.shift();
            }
        }
    }

    /*
     * This method is called after preLCS(). Its role is to deplete the LCS.
     *
     * One particularity of using LCS is that as long as the LCS is not empty,
     * we can be sure that there is at least one element left in both arrays
     * (and, obviously enough, one element left in the LCS).
     */
    private static void inLCS(final List<Diff> diffs, final JsonPointer path,
        final IndexedJsonArray lcsArray, final IndexedJsonArray array1,
        final IndexedJsonArray array2)
    {
        JsonNode node1;
        JsonNode node2;
        JsonNode lcsNode;

        while (!lcsArray.isEmpty()) {
            node1 = array1.getElement();
            node2 = array2.getElement();
            lcsNode = lcsArray.getElement();
            if (!EQUIVALENCE.equivalent(node1, lcsNode)) {
                /*
                 * At this point, the first element of array1 (the array which
                 * needs to be patched so that it become array2) has failed to
                 * "reach" a matching element in array2.
                 *
                 * Such an element therefore needs to be removed from the
                 * patched node. We also need to shift array1, and restart the
                 * loop.
                 */
                diffs.add(Diff.arrayRemove(path, array1, array2));
                array1.shift();
                continue;
            }
            /*
             * When we arrive here, we know that the element extracted from the
             * first array is equivalent to the LCS element.
             *
             * Note that from this point on, whatever node2 (ie, the element
             * extracted from array2) is, we need to shift array2; but in the
             * event where the nodes from the first and second array differ, we
             * must first insert the element found in array2 into the patched
             * node. This is why we need to postpone array2 shifting.
             */
            if (EQUIVALENCE.equivalent(node1, node2)) {
                /*
                 * When we enter here, we know that the element extracted from
                 * the second array is equivalent to the LCS element; but it is
                 * also equivalent to the node extracted from the first array.
                 *
                 * We therefore have a common "LCS subsequence" element: what we
                 * need to do here is to shift elements of all three indexed
                 * arrays (array1, array2, lcsArray).
                 *
                 * Note that, as mentioned above, shifting of array2 is
                 * postponed.
                 */
                array1.shift();
                lcsArray.shift();
            } else {
                /*
                 * When we enter here, we know that:
                 *
                 * - the first element is equivalent to the LCS node;
                 * - the second node is NOT equivalent to the LCS node.
                 *
                 * This means that we need to _insert_ the element from the
                 * second array into the patched node, and advance the second
                 * array only. But since it is always done anyway, we need not
                 * worry about it at this point.
                 */
                diffs.add(Diff.arrayInsert(path, array1, array2));
            }
            /*
             * Shift/advance the second array, see above
             */
            array2.shift();
        }
    }

    /*
     * This function is run once the LCS has been exhausted.
     *
     * Since the LCS has been exhausted, it means that for whatever nodes node1
     * and node2 extracted from array1 and array2, they can never be equal.
     *
     * The algorithm is therefore as follows:
     *
     * - as long as both are not empty, grab both elements from both arrays and
     *   generate diff operations on them recursively;
     * - when we are out of this loop, add any elements remaining in the second
     *   array (if any), and remove any elements remaining in the first array
     *  (if any).
     *
     * Note that at the second step, only one of the two input arrays will ever
     * have any elements left; it is therefore safe to call the appropriate
     * functions for _both_ possibilities since only one will ever produce any
     * results.
     */
    private static void postLCS(final List<Diff> diffs, final JsonPointer path,
        final IndexedJsonArray array1, final IndexedJsonArray array2)
    {
        JsonNode node1, node2;

        while (!(array1.isEmpty() || array2.isEmpty())) {
            node1 = array1.getElement();
            node2 = array2.getElement();
            generateDiffs(diffs, path.append(array1.getIndex()), node1, node2);
            array1.shift();
            array2.shift();
        }
        addRemaining(diffs, path, array2);
        removeRemaining(diffs, path, array1);
    }

    private static void addRemaining(final List<Diff> diffs,
        final JsonPointer path, final IndexedJsonArray array)
    {
        Diff diff;
        JsonNode node;

        while (!array.isEmpty()) {
            node = array.getElement().deepCopy();
            array.shift();
            diff = Diff.arrayAdd(path, node);
            diffs.add(diff);
        }
    }

    private static void removeRemaining(final List<Diff> diffs,
        final JsonPointer path, final IndexedJsonArray array)
    {
        final int startingIndex = array.getIndex();

        Diff diff;
        JsonNode node;

        while (!array.isEmpty()) {
            node = array.getElement();
            array.shift();
            diff = Diff.arrayRemove(path, startingIndex, node);
            diffs.add(diff);
        }
    }

}
