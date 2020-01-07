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

package com.github.fge.jsonpatch.mergepatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchMessages;
import com.github.fge.jsonpatch.Patch;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

/**
 * Implementation of JSON Merge Patch (RFC 7386)
 *
 * <p><a href="http://tools.ietf.org/html/rfc7386">JSON Merge Patch</a> is a
 * "toned down" version of JSON Patch. However, it covers a very large number of
 * use cases for JSON value modifications; its focus is mostly on patching
 * JSON Objects, which are by far the most common type of JSON texts exchanged
 * on the Internet.</p>
 *
 * <p>Applying a JSON Merge Patch is defined by a single, pseudo code function
 * as follows (quoted from the RFC; indentation fixed):</p>
 *
 * <pre>
 *     define MergePatch(Target, Patch):
 *         if Patch is an Object:
 *             if Target is not an Object:
 *                 Target = {} # Ignore the contents and set it to an empty Object
 *             for each Name/Value pair in Patch:
 *                 if Value is null:
 *                     if Name exists in Target:
 *                         remove the Name/Value pair from Target
 *                 else:
 *                     Target[Name] = MergePatch(Target[Name], Value)
 *             return Target
 *         else:
 *             return Patch
 * </pre>
 */
@ParametersAreNonnullByDefault
@JsonDeserialize(using = JsonMergePatchDeserializer.class)
public abstract class JsonMergePatch
    implements JsonSerializable, Patch
{
    private static final ObjectMapper MAPPER = JacksonUtils.newMapper();
    protected static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPatchMessages.class);

    /**
     * Build an instance from a JSON input
     *
     * @param node the input
     * @return a JSON Merge Patch instance
     * @throws JsonPatchException failed to deserialize
     * @throws NullPointerException node is null
     */
    public static JsonMergePatch fromJson(final JsonNode node)
        throws JsonPatchException
    {
        BUNDLE.checkNotNull(node, "jsonPatch.nullInput");
        try {
            return MAPPER.readValue(node.traverse(), JsonMergePatch.class);
        } catch (IOException e) {
            throw new JsonPatchException(
                BUNDLE.getMessage("jsonPatch.deserFailed"), e);
        }
    }

    /**
     * Apply the patch to a given JSON value
     *
     * @param input the value to patch
     * @return the patched value
     * @throws JsonPatchException never thrown; only for consistency with
     * {@link JsonPatch}
     * @throws NullPointerException value is null
     */
    @Override
    public abstract JsonNode apply(final JsonNode input)
        throws JsonPatchException;
}
