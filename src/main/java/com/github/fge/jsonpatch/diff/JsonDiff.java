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

/**
 * "Reverse" factorizing JSON Patch implementation
 *
 * <p>This class only has one method, {@link #asJson(JsonNode, JsonNode)}, which
 * takes two JSON values as arguments and returns a patch as a {@link JsonNode}.
 * This generated patch can then be used in {@link
 * JsonPatch#fromJson(JsonNode)}.</p>
 *
 * <p>Numeric equivalence is respected. When dealing with object values,
 * operations are always generated in the following order:
 *
 * <ul>
 *     <li>additions,</li>
 *     <li>removals,</li>
 *     <li>replacements.</li>
 * </ul>
 *
 * Array values generate operations in the order of elements. Factorizing is
 * done to merge add and remove into move operations and convert duplicate
 * add to copy operations if values are equivalent. Test operations are not
 * generated.</p>
 *
 * <p>Note that due to the way {@link JsonNode} is implemented, this class is
 * inherently <b>not</b> thread safe (since {@code JsonNode} is mutable). It is
 * therefore the responsibility of the caller to ensure that the calling context
 * is safe (by ensuring, for instance, that only the diff operation has
 * references to the values to be diff'ed).</p>
 *
 * @since 1.3
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
     * @param path path to first and second nodes
     * @param first first node to compare
     * @param second second node to compare
     */
    private static void generateDiffs(final List<Diff> diffs,
        final JsonPointer path, final JsonNode first, final JsonNode second)
    {
        // compare deep nodes
        if (EQUIVALENCE.equivalent(first, second))
            return;

        // compare node types: if types are not the same or if not
        // an array or object, this is a replace operation
        final NodeType firstType = NodeType.getNodeType(first);
        final NodeType secondType = NodeType.getNodeType(second);
        if (firstType != secondType || !first.isContainerNode()) {
            diffs.add(new Diff(DiffOperation.REPLACE, path, second.deepCopy()));
            return;
        }

        // matching array or object nodes: recursively generate diffs
        // for object members or array elements

        if (firstType == NodeType.OBJECT)
            generateObjectDiffs(diffs, path, first, second);
        else // array
            generateArrayDiffs(diffs, path, first, second);
    }

    /**
     * Generate differences between first and second object nodes. Differences
     * are generated in the following order: added fields, removed fields, and
     * common fields differences.
     *
     * @param diffs returned ordered differences
     * @param path path to first and second nodes
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
            diffs.add(new Diff(DiffOperation.ADD, path.append(added),
                    second.get(added).deepCopy()));

        // removed fields
        fields = Lists.newArrayList(inFirst);
        fields.removeAll(inSecond);
        for (final String removed: fields)
            diffs.add(new Diff(DiffOperation.REMOVE, path.append(removed),
                    first.get(removed).deepCopy()));

        // recursively generate diffs for fields in both objects
        fields = Lists.newArrayList(inFirst);
        fields.retainAll(inSecond);
        for (final String common: fields)
            generateDiffs(diffs, path.append(common), first.get(common),
                    second.get(common));
    }

    /**
     * Generate differences between first and second array nodes. Differences
     * are generated in order by comparing elements against the longest common
     * subsequence of elements in both arrays.
     *
     * @param diffs returned ordered differences
     * @param path path to first and second nodes
     * @param first first array node to compare
     * @param second second array node to compare
     */
    private static void generateArrayDiffs(final List<Diff> diffs,
        final JsonPointer path, final JsonNode first, final JsonNode second)
    {
        // compare array elements linearly using longest common subsequence
        // algorithm applied to the array elements
        final List<JsonNode> lcs = LCS.getLCS(first, second);
        final int firstSize = first.size();
        final int secondSize = second.size();
        final int lcsSize = lcs.size();

        int firstIndex = 0;
        int secondIndex = 0;
        int lcsIndex = 0;

        JsonNode firstElement;
        JsonNode secondElement;
        JsonNode lcsElement;

        while (firstIndex < firstSize || secondIndex < secondSize) {
            firstElement = first.get(firstIndex);
            secondElement = second.get(secondIndex);
            lcsElement = lcsIndex < lcsSize ? lcs.get(lcsIndex) : null;
            if (firstElement == null) {
                // appended elements
                diffs.add(new Diff(DiffOperation.ADD, path, firstIndex, -1,
                        second.get(secondIndex).deepCopy()));
                secondIndex++;
                continue;
            }
            if (EQUIVALENCE.equivalent(firstElement, lcsElement)) {
                if (EQUIVALENCE.equivalent(firstElement, secondElement)) {
                    // common subsequence elements
                    firstIndex++;
                    secondIndex++;
                    lcsIndex++;
                } else {
                    // inserted elements
                    diffs.add(new Diff(DiffOperation.ADD, path, firstIndex,
                        secondIndex, second.get(secondIndex).deepCopy()));
                    secondIndex++;
                }
            } else if (secondElement != null
                && !EQUIVALENCE.equivalent(secondElement, lcsElement)) {
                // generate diffs for or replaced elements
                if (firstIndex == secondIndex)
                    generateDiffs(diffs, path.append(firstIndex), firstElement,
                        secondElement);
                else
                    diffs.add(new Diff(DiffOperation.REPLACE, path, firstIndex,
                        secondIndex, second.get(secondIndex).deepCopy()));
                firstIndex++;
                secondIndex++;
            } else {
                // removed elements
                diffs.add(new Diff(DiffOperation.REMOVE, path, firstIndex,
                    secondIndex, first.get(firstIndex).deepCopy()));
                firstIndex++;
            }
        }
    }
}
