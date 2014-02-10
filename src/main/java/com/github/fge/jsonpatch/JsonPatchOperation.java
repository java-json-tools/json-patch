/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
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
    protected static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPatchMessages.class);

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
