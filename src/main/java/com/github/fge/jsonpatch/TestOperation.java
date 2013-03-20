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

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.base.Equivalence;

/**
 * JSON Patch {@code test} operation
 *
 * <p>The two arguments for this operation are the pointer containing the value
 * to test ({@code path}) and the value to test equality against ({@code
 * value}).</p>
 *
 * <p>It is an error if no value exists at the given path.</p>
 *
 * <p>Also note that equality as defined by JSON Patch is exactly the same as it
 * is defined by JSON Schema itself. As such, this operation reuses {@link
 * JsonNumEquals} for testing equality.</p>
 */
public final class TestOperation
    extends PathValueOperation
{
    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

    @JsonCreator
    public TestOperation(@JsonProperty("path") final JsonPointer path,
        @JsonProperty("value") final JsonNode value)
    {
        super(path, value);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        final JsonNode tested = path.path(node);
        if (tested.isMissingNode())
            throw new JsonPatchException(JsonPatchMessages.NO_SUCH_PATH);
        if (!EQUIVALENCE.equivalent(tested, value))
            throw new JsonPatchException(JsonPatchMessages.VALUE_TEST_FAILURE);
        return node.deepCopy();
    }

    @Override
    public String toString()
    {
        return "test: " + super.toString();
    }
}
