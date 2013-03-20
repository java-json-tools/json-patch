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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.google.common.collect.Lists;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonSubTypes.*;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.*;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "op")

@JsonSubTypes({
    @Type(name = "add", value = AddOperation.class),
    @Type(name = "copy", value = CopyOperation.class),
    @Type(name = "move", value = MoveOperation.class),
    @Type(name = "remove", value = RemoveOperation.class),
    @Type(name = "replace", value = ReplaceOperation.class),
    @Type(name = "test", value = TestOperation.class)
})

/**
 * Base abstract class for one patch operation
 *
 * <p>Two more abstract classes extend this one according to the arguments of
 * the operation:</p>
 *
 * <ul>
 *     <li>{@link DualPathOperation} for operations taking a second pointer as
 *     an argument ({@code copy} and {@code move});</li>
 *     <li>{@link PathValueOperation} for operations taking a value as an
 *     argument ({@code add}, {@code replace} and {@code test}).</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class JsonPatchOperation
{
    /*
     * Note: no need for a custom deserializer, Jackson will try and find a
     * constructor with a single string argument and use it
     */
    protected final JsonPointer path;

    /**
     * Constructor
     *
     * @param path the JSON Pointer for this operation
     */
    protected JsonPatchOperation(final JsonPointer path)
    {
        this.path = path;
    }

    /**
     * Apply this operation to a JSON value
     *
     * @param node the value to patch
     * @return the patched value
     * @throws JsonPatchException operation failed to apply to this value
     */
    public abstract JsonNode apply(final JsonNode node)
        throws JsonPatchException;

    @Override
    public String toString()
    {
        return "path = \"" + path + '"';
    }

    /**
     * Utility class used by some operations to split a JSON Pointer in two
     *
     * <p>The two elements are:</p>
     *
     * <ul>
     *     <p>the immediate parent pointer;</p>
     *     <p>the {@link TokenResolver} for the last reference token.</p>
     * </ul>
     */
    protected static final class SplitPointer
    {
        final JsonPointer parent;
        final TokenResolver<JsonNode> lastToken;

        SplitPointer(final JsonPointer pointer)
        {
            final List<TokenResolver<JsonNode>> tokens
                = Lists.newArrayList(pointer);
            lastToken = tokens.remove(tokens.size() - 1);
            parent = new JsonPointer(tokens);
        }
    }
}
