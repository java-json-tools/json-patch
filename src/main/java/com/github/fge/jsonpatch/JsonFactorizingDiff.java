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

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

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
 * done to merge add and remove into move operations if values are equivalent.
 * Copy and test operations are not generated.</p>
 *
 * <p>Note that due to the way {@link JsonNode} is implemented, this class is
 * inherently <b>not</b> thread safe (since {@code JsonNode} is mutable). It is
 * therefore the responsibility of the caller to ensure that the calling context
 * is safe (by ensuring, for instance, that only the diff operation has
 * references to the values to be diff'ed).</p>
 */
public final class JsonFactorizingDiff
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();
    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

    private JsonFactorizingDiff()
    {
    }

    /**
     * Generate a factorized patch for transforming the first node into the
     * second node.
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
        factorizeDiffs(diffs);

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
        // compare different objects fieldwise
        final Set<String> inFirst = Sets.newHashSet(first.fieldNames());
        final Set<String> inSecond = Sets.newHashSet(second.fieldNames());

        Set<String> set;

        // added fields
        set = Sets.difference(inSecond, inFirst);
        for (final String added: set)
            diffs.add(new Diff(DiffOperation.ADD, path.append(added),
                    second.get(added).deepCopy()));

        // removed fields
        set = Sets.difference(inFirst, inSecond);
        for (final String removed : set)
            diffs.add(new Diff(DiffOperation.REMOVE, path.append(removed),
                    first.get(removed).deepCopy()));

        // recursively generate diffs for fields in both objects
        set = Sets.intersection(inFirst, inSecond);
        for (final String common: set)
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
        final List<JsonNode> lcs = getLCSDiffs(first, second);
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
            firstElement = firstIndex < firstSize ? first.get(firstIndex)
                : null;
            secondElement = secondIndex < secondSize ? second.get(secondIndex)
                : null;
            lcsElement = lcsIndex < lcsSize ? lcs.get(lcsIndex) : null;
            if (firstElement == null) {
                // appended elements
                diffs.add(new Diff(DiffOperation.ADD, path, null, -1,
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
                    diffs.add(new Diff(DiffOperation.ADD, path, null,
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

    /**
     * Get longest common subsequence of elements in first and second array nodes.
     * For example:
     *
     * <pre>
     *     first array: [ 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
     *     second array: [ 1, 2, 10, 11, 5, 12, 8, 9 ]
     *     LCS: [ 1, 2, 5, 8, 9 ]
     * </pre>
     *
     * This is an implementation of the classic 'diff' algorithm often used to
     * compare text files line by line.
     *
     * @param first first array node to compare
     * @param second second array node to compare
     */
    private static List<JsonNode> getLCSDiffs(final JsonNode first,
        final JsonNode second)
    {
        final int firstSize = first.size();
        final int secondSize = second.size();
        final int minSize = Math.min(firstSize, secondSize);

        // trim common beginning and ending elements
        int offset = 0;
        int trim = 0;

        for (int index = 0; index < minSize; index++) {
            if (!EQUIVALENCE.equivalent(first.get(index), second.get(index)))
                break;
            offset++;
        }

        for (int i = firstSize - 1, j = secondSize - 1; i > offset && j > offset;
            i--, j--) {
            if (!EQUIVALENCE.equivalent(first.get(i), second.get(j)))
                break;
            trim++;
        }

        // find longest common subsequence in remaining elements
        List<JsonNode> lcs = Lists.newArrayList();
        if (offset < minSize) {
            // construct LCS lengths matrix
            final int firstLimit = firstSize - offset - trim;
            final int secondLimit = secondSize - offset - trim;
            final int[][] lengths = new int[firstLimit+1][secondLimit+1];

            for (int i = 0; i < firstLimit; i++)
                for (int j = 0; j < secondLimit; j++)
                    if (EQUIVALENCE.equivalent(first.get(i + offset), second.get(j + offset)))
                        lengths[i + 1][j + 1] = lengths[i][j] + 1;
                    else
                        lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j],
                            lengths[i][j + 1]);

            // return result out of the LCS lengths matrix
            int x = firstLimit, y = secondLimit;
            while (x > 0 && y > 0) {
                if (lengths[x][y] == lengths[x - 1][y])
                    x--;
                else if (lengths[x][y] == lengths[x][y - 1])
                    y--;
                else {
                    lcs.add(first.get(x - 1 + offset));
                    x--;
                    y--;
                }
            }
            lcs = Lists.reverse(lcs);
        }

        // prepend/append common elements
        for (int i = 0; i < offset; i++)
            lcs.add(first.get(i));
        for (int i = firstSize - trim; i < firstSize; i++)
            lcs.add(first.get(i));

        return lcs;
    }

    /**
     * Factorize list of ordered differences. Where removed values are
     * equivalent to added values, merge add and remove to move operation
     * differences. Because remove operation differences are relocated in
     * the process of merging, other differences can be side effected.
     *
     * @param diffs list of ordered differences.
     */
    private static void factorizeDiffs(final List<Diff> diffs)
    {
        // get remove diffs to be factored
        final List<Diff> removeDiffs = Lists.newArrayList();
        for (final Diff diff: diffs)
            if (diff.getOperation() == DiffOperation.REMOVE)
                removeDiffs.add(diff);

        // pair remove with add diff that has identical values
        for (final Diff removeDiff: removeDiffs) {
            for (int diffIndex = 0, diffsSize = diffs.size();
                 diffIndex < diffsSize; diffIndex++) {
                final Diff diff = diffs.get(diffIndex);
                if (diff.getOperation() != DiffOperation.ADD)
                    continue;
                if (!EQUIVALENCE.equivalent(diff.getValue(), removeDiff.getValue()))
                    continue;

                JsonPointer fromPath = removeDiff.getPath();

                // adjust fromPath and subsequent diff array indexes
                final int removeDiffIndex = diffs.indexOf(removeDiff);
                final JsonPointer removeDiffArrayPath = removeDiff.getArrayPath();
                if (removeDiffArrayPath != null)
                    if (removeDiffIndex < diffIndex) {
                        // remove deferred with move: use secondary array path for from path
                        fromPath = removeDiff.getSecondArrayPath();

                        // remove deferred with move: adjust subsequent diff array second
                        // indexes between the remove diff and the paired diff to reflect the
                        // fact that the remove will not be done until move diff is applied
                        for (int i = removeDiffIndex + 1; i < diffIndex; i++) {
                            final Diff adjustDiff = diffs.get(i);
                            if (removeDiffArrayPath.equals(adjustDiff.getArrayPath())) {
                                final int secondArrayIndex = adjustDiff.getSecondArrayIndex();
                                if (secondArrayIndex != -1)
                                    adjustDiff.setSecondArrayIndex(secondArrayIndex+1);
                            }
                        }
                    } else {
                        // remove done early with move: use first array path for from path
                        fromPath = removeDiff.getFirstArrayPath();

                        // remove done early with move: adjust subsequent diff array first
                        // indexes after the remove diff to reflect the fact that the remove
                        // will be done before subsequent remove diff are factorized
                        for (int i = removeDiffIndex + 1; i < diffsSize; i++) {
                            final Diff adjustDiff = diffs.get(i);
                            if (!removeDiffArrayPath.equals(adjustDiff.getArrayPath()))
                                break;
                            final Integer firstArrayIndex = adjustDiff.getFirstArrayIndex();
                            if (firstArrayIndex != null)
                                adjustDiff.setFirstArrayIndex(firstArrayIndex-1);
                        }
                    }

                // remove remove diff to be converted to a move diff
                diffs.remove(removeDiffIndex);

                // convert add into a move diff
                diff.setOperation(DiffOperation.MOVE);
                diff.setFromPath(fromPath);
                break;
            }
        }
    }

    /**
     * Difference operation types. Add, remove, and replace operations
     * are directly generated by node comparison. Move operations are
     * the result of factorized add and remove operations.
     */
    private static enum DiffOperation {ADD, REMOVE, REPLACE, MOVE}

    /**
     * Difference representation. Captures diff information required to
     * generate JSON patch operations and factorize differences.
     */
    private static class Diff
    {
        private DiffOperation operation;
        private JsonPointer path;
        private JsonPointer arrayPath;
        private Integer firstArrayIndex;
        private Integer secondArrayIndex;
        private JsonNode value;
        private JsonPointer fromPath;

        private Diff(final DiffOperation operation, final JsonPointer path, final JsonNode value)
        {
            this.operation = operation;
            this.path = path;
            this.value = value;
        }

        private Diff(final DiffOperation operation, final JsonPointer arrayPath, Integer firstArrayIndex,
                    Integer secondArrayIndex, final JsonNode value)
        {
            this.operation = operation;
            this.arrayPath = arrayPath;
            this.firstArrayIndex = firstArrayIndex;
            this.secondArrayIndex = secondArrayIndex;
            this.value = value;
        }

        private JsonNode asJsonPatch()
        {
            final ObjectNode patch = FACTORY.objectNode();
            String path = ((getArrayPath() != null) ? getSecondArrayPath() : getPath()).toString();
            switch (operation)
            {
                case ADD:
                    patch.put("op", "add");
                    patch.put("path", path);
                    patch.put("value", value);
                    break;
                case REMOVE:
                    patch.put("op", "remove");
                    patch.put("path", path);
                    break;
                case REPLACE:
                    patch.put("op", "replace");
                    patch.put("path", path);
                    patch.put("value", value);
                    break;
                case MOVE:
                    patch.put("op", "move");
                    patch.put("from", fromPath.toString());
                    patch.put("path", path);
                    break;
            }
            return patch;
        }

        private DiffOperation getOperation()
        {
            return operation;
        }

        private void setOperation(DiffOperation operation)
        {
            this.operation = operation;
        }

        private JsonPointer getPath()
        {
            return path;
        }

        private JsonPointer getFirstArrayPath()
        {
            // compute path from array path and index
            return arrayPath.append(firstArrayIndex);
        }

        private JsonPointer getSecondArrayPath()
        {
            // compute path from array path and index
            if (secondArrayIndex != -1)
                return arrayPath.append(secondArrayIndex);
            return arrayPath.append("-");
        }

        private JsonPointer getArrayPath()
        {
            return arrayPath;
        }

        private Integer getFirstArrayIndex()
        {
            return firstArrayIndex;
        }

        private void setFirstArrayIndex(int firstArrayIndex)
        {
            this.firstArrayIndex = firstArrayIndex;
        }

        private Integer getSecondArrayIndex()
        {
            return secondArrayIndex;
        }

        private void setSecondArrayIndex(int secondArrayIndex)
        {
            this.secondArrayIndex = secondArrayIndex;
        }

        private JsonNode getValue()
        {
            return value;
        }

        private void setFromPath(JsonPointer fromPath)
        {
            this.fromPath = fromPath;
        }
    }
}
