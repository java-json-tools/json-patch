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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

final class DiffFactorizer
{
    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

    private DiffFactorizer()
    {
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
    public static void factorizeDiffs(final List<Diff> diffs)
    {
        findPairs(diffs);
        factorizePairs(diffs);

        // Factorize add diffs with equivalent non-empty object or array
        // values into copy diffs; from paths for copy diffs can be set using
        // previous add diff paths and/or array paths because diff order is
        // acyclic and immutable for this factorization. The only exception
        // to this rule are adds that append to arrays: these have no concrete
        // path that can serve as a copy diff from path.
        final List<Diff> addDiffs = Lists.newArrayList();
        for (final Diff diff: diffs) {
            /*
             * Ignore non add operations
             */
            if (diff.operation != DiffOperation.ADD)
                continue;
            /*
             * Skip value nodes or empty objects/arrays
             */
            if (diff.value.size() == 0)
                continue;
            // check add diff value against list of previous add diffs
            Diff addDiff = null;
            for (final Diff testAddDiff : addDiffs)
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
            diff.fromPath = addDiff.arrayPath != null
                ? addDiff.getSecondArrayPath() : addDiff.path;
        }
    }

    /**
     * Find additions/removal pairs
     *
     * <p>Find addition operations which can be paired value-wise with removal
     * operations.</p>
     *
     * <p>Note that only the first pair is considered.</p>
     *
     * @param diffs the list of diffs
     */
    private static void findPairs(final List<Diff> diffs)
    {
        final int diffsSize = diffs.size();

        Diff addition, removal;

        for (int addIndex = 0; addIndex < diffsSize; addIndex++) {
            addition = diffs.get(addIndex);
            if (addition.operation != DiffOperation.ADD)
                continue;

            /*
             * Found an addition: try and find a matching removal
             */
            for (int removeIndex = 0; removeIndex < diffsSize; removeIndex++) {
                removal = diffs.get(removeIndex);
                if (removal.operation != DiffOperation.REMOVE)
                    continue;
                if (!EQUIVALENCE.equivalent(removal.value, addition.value))
                    continue;

                /*
                 * Found a pair: record it
                 */
                addition.pairedDiff = removal;
                addition.firstOfPair = addIndex < removeIndex;
                removal.pairedDiff = addition;
                removal.firstOfPair = removeIndex < addIndex;
                break;
            }
        }

    }

    /**
     * Factorize additions/removals
     *
     * <p>Removals, when paired with additions, are removed from the list.</p>
     *
     * <p>Special care must be taken for additions/removal pairs happening
     * within the same array, so that array indices can be adjusted properly.
     * </p>
     *
     * @param diffs the list of diffs
     */
    private static void factorizePairs(final List<Diff> diffs)
    {
        /*
         * Those two arrays will hold array removals seen before or after their
         * paired additions.
         */
        final List<Diff> seenBefore = Lists.newArrayList();
        final List<Diff> seenAfter = Lists.newArrayList();
        final Iterator<Diff> iterator = diffs.iterator();

        Diff diff;
        while (iterator.hasNext()) {
            diff = iterator.next();

            if (diff.pairedDiff != null) {
                switch (diff.operation) {
                    case REMOVE:
                        /*
                         * If removal is from an array and we reach this point,
                         * it means the matching addition has not been seen yet.
                         * Add this diff to the relevant list.
                         */
                        if (diff.arrayPath != null && diff.firstOfPair)
                            seenBefore.add(diff);
                        // remove paired remove and continue
                        iterator.remove();
                        continue;
                    case ADD:
                        /*
                         * Turn paired additions into move operations
                         */
                        transformAddition(seenBefore, seenAfter, diff);
                    /*
                     * Note: a paired diff cannot be of any other type
                     */
                }
            }

            // adjust secondary index for all array diffs with matching
            // deferred array removes; note:  all non remove array diffs
            // have a valid second array index
            if (diff.arrayPath != null)
                diff.secondArrayIndex = adjustSecondArrayIndex(seenBefore,
                    diff.arrayPath, diff.secondArrayIndex);
        }
    }

    private static void transformAddition(final List<Diff> seenBefore,
        final List<Diff> seenAfter, final Diff diff)
    {
        final Diff removal = diff.pairedDiff;
        // convert paired add diff into a move
        diff.operation = DiffOperation.MOVE;
        diff.pairedDiff = null;

        /*
         * Compute the "from" path of this move operation
         */
        if (removal.arrayPath == null) {
            /*
             * If removal was not from an array, we just need to grab
             * the path of this remove operation as the origin path
             * for this move
             */
            diff.fromPath = removal.path;
            return;
        }

        if (diff.firstOfPair) {
            // move diff is first of pair: remove will be advanced
            // and will use original first indexes into array
            int removeIndex = removal.firstArrayIndex;
            // adjust remove index for operations on arrays with
            // matching advanced array removes
            removeIndex = adjustFirstArrayIndex(seenAfter, removal.arrayPath,
                removeIndex);
            // if move diff and remove diff are from the same array,
            // remove index must be based on an original index offset
            // from the move diff secondary index; this is reflecting
            // the fact that array diff operations up to the move diff
            // have been applied, but those following the move diff to
            // the remove diff have not and thus require original
            // first array index adjustments
            if (removal.arrayPath.equals(diff.arrayPath)) {
                final int moveSecondArrayIndex = adjustSecondArrayIndex(
                    seenBefore, diff.arrayPath, diff.secondArrayIndex);
                final int moveFirstArrayIndex = adjustFirstArrayIndex(seenAfter,
                    diff.arrayPath, diff.firstArrayIndex);
                removeIndex += moveSecondArrayIndex - moveFirstArrayIndex;
            }
            // set move diff from using adjusted remove index
            diff.fromPath = removal.arrayPath.append(removeIndex);
            // track advanced array removes
            seenAfter.add(removal);
        } else {
            // remove diff is first of pair: remove has been deferred
            // for this move; remove tracked deferred array remove
            seenBefore.remove(removal);
            // remove can now be moved using second index
            int removeIndex = removal.secondArrayIndex;
            // adjust remove index for operations on arrays with
            // matching deferred array removes
            removeIndex = adjustSecondArrayIndex(seenBefore, removal.arrayPath,
                removeIndex);
            // set move diff from using adjusted remove index
            diff.fromPath = removal.arrayPath.append(removeIndex);
        }
    }

    /**
     * Adjust array index based on array removes seen before their matching
     * additions. Missing second array indexes (-1) are not adjusted.
     *
     * @param seenAfter list of removals seen before their matching additions
     * @param arrayPath array path of array index to adjust
     * @param arrayIndex index to adjust and upper range of removes
     * @return index adjusted by advanced array moves in range
     */
    private static int adjustFirstArrayIndex(final List<Diff> seenAfter,
        final JsonPointer arrayPath, final int arrayIndex)
    {
        /*
         * Adjust index of removal operations on arrays with matching advanced
         * array removes: for each advanced remove, decrement the index assuming
         * remove will have been done before remaining diffs on array
         */
        int arrayRemoves = 0;
        for (final Diff removal: seenAfter)
            if (arrayPath.equals(removal.arrayPath)
                && arrayIndex > removal.firstArrayIndex)
                arrayRemoves++;
        return arrayIndex - arrayRemoves;
    }

    /**
     * Adjust array index of removal operations seen after their matching
     * additions. Missing second array indexes (-1) are not adjusted.
     *
     * @param seenAfter list of removals seen before their matching additions
     * @param arrayPath array path of array index to adjust
     * @param arrayIndex index to adjust and upper range of moves
     * @return index adjusted by deferred array moves in range
     */
    private static int adjustSecondArrayIndex(final List<Diff> seenAfter,
        final JsonPointer arrayPath, final int arrayIndex)
    {
        if (arrayIndex == -1)
            return arrayIndex;
        /*
         * adjust secondary index for operations on arrays with matching
         * deferred array removes: for each deferred remove, increment the index
         * assuming remove will not be done until the move diff is performed
         */
        int arrayRemoves = 0;
        for (final Diff removal: seenAfter)
            if (arrayPath.equals(removal.arrayPath)
                && arrayIndex >= removal.secondArrayIndex)
                arrayRemoves++;
        return arrayIndex + arrayRemoves;
    }
}
