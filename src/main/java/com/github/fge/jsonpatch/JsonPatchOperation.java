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

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;

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
    implements JsonSerializable
{
    protected static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPatchMessages.class);

    protected final String op;

    /*
     * Note: no need for a custom deserializer, Jackson will try and find a
     * constructor with a single string argument and use it.
     *
     * However, we need to serialize using .toString().
     */
    protected final JsonPointer path;

    /**
     * Constructor
     *
     * @param op the operation name
     * @param path the JSON Pointer for this operation
     */
    protected JsonPatchOperation(final String op, final JsonPointer path)
    {
        this.op = op;
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

    public final String getOp() {
        return op;
    }

    public final JsonPointer getPath() {
        return path;
    }

    @Override
    public abstract String toString();
}
