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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.base.Objects;

/**
 * Difference representation. Captures diff information required to
 * generate JSON patch operations and factorize differences.
 */
final class Diff
{
    DiffOperation operation;
    JsonPointer path;
    JsonPointer arrayPath;
    int firstArrayIndex;
    int secondArrayIndex;
    final JsonNode value;
    JsonPointer fromPath;
    Diff pairedDiff;
    boolean firstOfPair;

    static Diff simpleDiff(final DiffOperation operation,
        final JsonPointer path, final JsonNode value)
    {
        return new Diff(operation, path, value.deepCopy());
    }

    /*
     * "Stateless" removal of a given node from an array given a base path (the
     * immediate parent of an array) and an array index; as the name suggests,
     * this factory method is called only when a node is removed from the tail
     * of a target array; in other words, the source node has extra elements.
     */
    static Diff tailArrayRemove(final JsonPointer basePath, final int index,
        final int removeIndex, final JsonNode victim)
    {
        return new Diff(DiffOperation.REMOVE, basePath, index, removeIndex,
            victim.deepCopy());
    }

    /*
     * FIXME: in both usages of this function, array1 is shifted; but we do not
     * do that here: doing it would hide an essential piece of information to
     * the caller.
     *
     * In other words, there is some embarrassing entanglement here which needs
     * to be understood and "decorrelated".
     */
    static Diff arrayRemove(final JsonPointer basePath,
        final IndexedJsonArray array1, final IndexedJsonArray array2)
    {
        return new Diff(DiffOperation.REMOVE, basePath, array1.getIndex(),
            array2.getIndex(), array1.getElement().deepCopy());
    }

    static Diff arrayAdd(final JsonPointer basePath, final JsonNode node)
    {
        return new Diff(DiffOperation.ADD, basePath, -1, -1, node.deepCopy());
    }

    static Diff arrayInsert(final JsonPointer basePath,
        final IndexedJsonArray array1, final IndexedJsonArray array2)
    {
        return new Diff(DiffOperation.ADD, basePath, array1.getIndex(),
            array2.getIndex(), array2.getElement().deepCopy());
    }

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

    JsonNode asJsonPatch()
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

    JsonPointer getSecondArrayPath()
    {
        // compute path from array path and index
        if (secondArrayIndex != -1)
            return arrayPath.append(secondArrayIndex);
        return arrayPath.append("-");
    }


    @Override
    public int hashCode()
    {
        return Objects.hashCode(operation, path, arrayPath, firstArrayIndex,
            secondArrayIndex, JsonNumEquals.getInstance().wrap(value),
            fromPath, pairedDiff != null, firstOfPair);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Diff other = (Diff) obj;
        return operation == other.operation
            && Objects.equal(path, other.path)
            && Objects.equal(arrayPath, other.arrayPath)
            && firstArrayIndex == other.firstArrayIndex
            && secondArrayIndex == other.secondArrayIndex
            && JsonNumEquals.getInstance().equivalent(value, other.value)
            && Objects.equal(fromPath, other.fromPath)
            && Objects.equal(pairedDiff != null, other.pairedDiff != null)
            && firstOfPair == other.firstOfPair;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).add("op", operation)
            .add("path", path).add("arrayPath", arrayPath)
            .add("firstArrayIndex", firstArrayIndex)
            .add("secondArrayIndex", secondArrayIndex)
            .add("value", value).add("fromPath", fromPath)
            .add("paired", pairedDiff != null).add("firstOfPair", firstOfPair)
            .toString();
    }
}
