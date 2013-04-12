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

import java.util.Iterator;
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

            JsonNode firstNode;
            JsonNode secondNode;
            int newLength;

            for (int i = 0; i < firstLimit; i++)
                for (int j = 0; j < secondLimit; j++) {
                    firstNode = first.get(i + offset);
                    secondNode = second.get(j + offset);
                    newLength = EQUIVALENCE.equivalent(firstNode, secondNode)
                        ? lengths[i][j] + 1
                        : Math.max(lengths[i + 1][j], lengths[i][j + 1]);
                    lengths[i + 1][j + 1] = newLength;
                }

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
            lcs.add(0, first.get(i));
        for (int i = firstSize - trim; i < firstSize; i++)
            lcs.add(first.get(i));

        return lcs;
    }

    /**
     * Factorize list of ordered differences. Where removed values are
     * equivalent to added values, merge add and remove to move
     * differences. Because remove differences are relocated in the
     * process of merging, other differences can be side effected.
     * Add differences with equivalent values to previous add
     * differences are converted to copy differences.
     *
     * @param diffs list of ordered differences.
     */
    private static void factorizeDiffs(final List<Diff> diffs)
    {
        // find add diffs to be factored and pair with remove diffs
        // that have equivalent values; first matching remove is
        // paired with each add since there is no context in which to
        // select a more appropriate pairing
        for (int addDiffIndex = 0, diffsSize = diffs.size();
            addDiffIndex < diffsSize; addDiffIndex++) {
            final Diff addDiff = diffs.get(addDiffIndex);
            if (addDiff.operation == DiffOperation.ADD) {
                for (int removeDiffIndex = 0;
                    removeDiffIndex < diffsSize; removeDiffIndex++) {
                    final Diff removeDiff = diffs.get(removeDiffIndex);
                    if (removeDiff.operation == DiffOperation.REMOVE
                        && EQUIVALENCE.equivalent(removeDiff.value, addDiff.value)) {
                        // paired add and remove diffs: link and save which diff
                        // of the pair appears first in the ordered diffs list
                        addDiff.pairedDiff = removeDiff;
                        addDiff.firstOfPair = addDiffIndex < removeDiffIndex;
                        removeDiff.pairedDiff = addDiff;
                        removeDiff.firstOfPair = removeDiffIndex < addDiffIndex;
                        break;
                    }
                }
            }
        }

        // factorize paired add and remove diffs: in this process, removes
        // are performed out of the original diff ordering just before the
        // paired add when converted to a move. consequently, moves that are
        // deferred or advanced must be tracked to allow proper diff array
        // index adjustments for diffs operating on the same arrays. diff
        // order is assumed to be acyclic and linearly processing arrays.
        final List<Diff> deferredArrayRemoves = Lists.newArrayList();
        final List<Diff> advancedArrayRemoves = Lists.newArrayList();
        for (final Iterator<Diff> diffIter = diffs.iterator();
            diffIter.hasNext();) {
            final Diff diff = diffIter.next();

            // remove paired remove diffs
            if (diff.operation == DiffOperation.REMOVE
                && diff.pairedDiff != null) {
                // track deferred array removes
                if (diff.arrayPath != null
                    && diff.firstOfPair)
                    deferredArrayRemoves.add(diff);
                // remove paired remove and continue
                diffIter.remove();
                continue;
            }

            // factorize paired add diffs to move diffs
            if (diff.operation == DiffOperation.ADD
                && diff.pairedDiff != null) {
                final Diff removeDiff = diff.pairedDiff;
                // convert paired add diff into a move
                diff.operation = DiffOperation.MOVE;
                diff.pairedDiff = null;
                // compute move diff from path
                if (removeDiff.arrayPath == null) {
                    // paired remove is not from an array: simply use remove
                    // path for move from
                    diff.fromPath = removeDiff.path;
                } else if (diff.firstOfPair) {
                    // move diff is first of pair: remove will be advanced
                    // and will use original first indexes into array
                    int removeIndex = removeDiff.firstArrayIndex;
                    // adjust remove index for operations on arrays with
                    // matching advanced array removes
                    removeIndex = adjustFirstArrayIndex(advancedArrayRemoves,
                        removeDiff.arrayPath, removeIndex);
                    // if move diff and remove diff are from the same array,
                    // remove index must be based on an original index offset
                    // from the move diff secondary index; this is reflecting
                    // the fact that array diff operations up to the move diff
                    // have been applied, but those following the move diff to
                    // the remove diff have not and thus require original
                    // first array index adjustments
                    if (removeDiff.arrayPath.equals(diff.arrayPath)) {
                        final int moveSecondArrayIndex = adjustSecondArrayIndex(
                            deferredArrayRemoves, diff.arrayPath,
                            diff.secondArrayIndex);
                        final int moveFirstArrayIndex = adjustFirstArrayIndex(
                            advancedArrayRemoves, diff.arrayPath,
                            diff.firstArrayIndex);
                        removeIndex += moveSecondArrayIndex - moveFirstArrayIndex;
                    }
                    // set move diff from using adjusted remove index
                    diff.fromPath = removeDiff.arrayPath.append(removeIndex);
                    // track advanced array removes
                    advancedArrayRemoves.add(removeDiff);
                } else {
                    // remove diff is first of pair: remove has been deferred
                    // for this move; remove tracked deferred array remove
                    deferredArrayRemoves.remove(removeDiff);
                    // remove can now be moved using second index
                    int removeIndex = removeDiff.secondArrayIndex;
                    // adjust remove index for operations on arrays with
                    // matching deferred array removes
                    removeIndex = adjustSecondArrayIndex(deferredArrayRemoves,
                        removeDiff.arrayPath, removeIndex);
                    // set move diff from using adjusted remove index
                    diff.fromPath = removeDiff.arrayPath.append(removeIndex);
                }
            }

            // adjust secondary index for all array diffs with matching
            // deferred array removes; note:  all non remove array diffs
            // have a valid second array index
            if (diff.arrayPath != null)
                diff.secondArrayIndex = adjustSecondArrayIndex(deferredArrayRemoves,
                    diff.arrayPath, diff.secondArrayIndex);
        }

        // Factorize add diffs with equivalent non-empty object or array
        // values into copy diffs; from paths for copy diffs can be set using
        // previous add diff paths and/or array paths because diff order is
        // acyclic and immutable for this factorization. The only exception
        // to this rule are adds that append to arrays: these have no concrete
        // path that can serve as a copy diff from path.
        final List<Diff> addDiffs = Lists.newArrayList();
        for (final Diff diff: diffs)
            if (diff.operation == DiffOperation.ADD)
                if (diff.value.size() > 0) {
                    // check add diff value against list of previous add diffs
                    Diff addDiff = null;
                    for (final Diff testAddDiff: addDiffs)
                        if (EQUIVALENCE.equivalent(diff.value, testAddDiff.value)) {
                            addDiff = testAddDiff;
                            break;
                        }
                    // if not found previously, save add diff, (if not appending
                    // to an array which can have no concrete from path), and continue
                    if (addDiff == null) {
                        if (diff.arrayPath == null || diff.secondArrayIndex != -1)
                            addDiffs.add(diff);
                        continue;
                    }
                    // previous add diff found by value: convert add diff to copy
                    // diff with from path set to concrete add diff path
                    diff.operation = DiffOperation.COPY;
                    diff.fromPath = addDiff.arrayPath != null ? addDiff.getSecondArrayPath()
                        : addDiff.path;
                }
    }

    /**
     * Adjust array index based on advanced array removes before
     * the specified index to adjust. Missing second array indexes,
     * (-1), are not adjusted.
     *
     * @param advancedArrayRemoves tracked advanced array removes
     * @param arrayPath array path of array index to adjust
     * @param arrayIndex index to adjust and upper range of removes
     * @return index adjusted by advanced array moves in range
     */
    private static int adjustFirstArrayIndex(final List<Diff> advancedArrayRemoves,
        final JsonPointer arrayPath, final int arrayIndex)
    {
        if (arrayIndex == -1)
            return arrayIndex;
        // adjust remove index for operations on arrays with
        // matching advanced array removes: for each advanced
        // remove, decrement the index assuming remove will have
        // been done before remaining diffs on array
        int arrayRemoves = 0;
        for (final Diff advancedArrayRemove: advancedArrayRemoves)
            if (arrayPath.equals(advancedArrayRemove.arrayPath)
                && arrayIndex > advancedArrayRemove.firstArrayIndex)
                arrayRemoves++;
        return arrayIndex - arrayRemoves;
    }

    /**
     * Adjust array index based on deferred array removes before or
     * at the specified index to adjust. Missing second array indexes,
     * (-1), are not adjusted.
     *
     * @param deferredArrayRemoves tracked deferred array moves
     * @param arrayPath array path of array index to adjust
     * @param arrayIndex index to adjust and upper range of moves
     * @return index adjusted by deferred array moves in range
     */
    private static int adjustSecondArrayIndex(final List<Diff> deferredArrayRemoves,
        final JsonPointer arrayPath, final int arrayIndex)
    {
        if (arrayIndex == -1)
            return arrayIndex;
        // adjust secondary index for operations on arrays with
        // matching deferred array removes: for each deferred remove,
        // increment the index assuming remove will not be done until
        // the move diff is performed
        int arrayRemoves = 0;
        for (final Diff deferredArrayRemove: deferredArrayRemoves)
            if (arrayPath.equals(deferredArrayRemove.arrayPath)
                && arrayIndex >= deferredArrayRemove.secondArrayIndex)
                arrayRemoves++;
        return arrayIndex + arrayRemoves;
    }

    /**
     * Difference operation types. Add, remove, and replace operations
     * are directly generated by node comparison. Move operations are
     * the result of factorized add and remove operations.
     */
    private enum DiffOperation
    {
        ADD("add"),
        REMOVE("remove"),
        REPLACE("replace"),
        MOVE("move"),
        COPY("copy"),
        ;

        private final String opName;

        DiffOperation(final String opName)
        {
            this.opName = opName;
        }

        private ObjectNode newOp(final JsonPointer ptr)
        {
            final ObjectNode ret = FACTORY.objectNode();
            ret.put("op", opName);
            ret.put("path", ptr.toString());
            return ret;
        }

        @Override
        public String toString()
        {
            return opName;
        }
    }

    /**
     * Difference representation. Captures diff information required to
     * generate JSON patch operations and factorize differences.
     */
    private static final class Diff
    {
        private DiffOperation operation;
        private JsonPointer path;
        private JsonPointer arrayPath;
        private int firstArrayIndex;
        private int secondArrayIndex;
        private final JsonNode value;
        private JsonPointer fromPath;
        private Diff pairedDiff;
        private boolean firstOfPair;

        private Diff(final DiffOperation operation, final JsonPointer path,
            final JsonNode value)
        {
            this.operation = operation;
            this.path = path;
            this.value = value;
        }

        private Diff(final DiffOperation operation, final JsonPointer arrayPath,
            final int firstArrayIndex, final int secondArrayIndex,
            final JsonNode value)
        {
            this.operation = operation;
            this.arrayPath = arrayPath;
            this.firstArrayIndex = firstArrayIndex;
            this.secondArrayIndex = secondArrayIndex;
            this.value = value;
        }

        private JsonNode asJsonPatch()
        {
            final JsonPointer ptr = arrayPath != null ? getSecondArrayPath()
                : path;
            final ObjectNode patch = operation.newOp(ptr);
            /*
             * A remove only has a path
             */
            if (operation == DiffOperation.REMOVE)
                return patch;
            /*
             * A move has a "source path" (the "from" member), other defined
             * operations (add and replace) have a value instead.
             */
            if (operation == DiffOperation.MOVE
                || operation == DiffOperation.COPY)
                patch.put("from", fromPath.toString());
            else
                patch.put("value", value);
            return patch;
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

        @Override
        public String toString()
        {
            return asJsonPatch().toString();
        }
    }
}
