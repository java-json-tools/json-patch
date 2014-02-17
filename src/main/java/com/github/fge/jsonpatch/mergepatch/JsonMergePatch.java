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

package com.github.fge.jsonpatch.mergepatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.NodeType;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchMessages;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;

/**
 * Implementation of <a href="http://tools.ietf.org/html/draft-ietf-appsawg-json-merge-patch-02">JSON merge patch</a>
 *
 * <p>Unlike JSON Patch, JSON Merge Patch only applies to JSON Objects or JSON
 * arrays.</p>
 */
public abstract class JsonMergePatch
{
    protected static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPatchMessages.class);

    public abstract JsonNode apply(final JsonNode input)
        throws JsonPatchException;

    public static JsonMergePatch fromJson(final JsonNode input)
        throws JsonPatchException
    {
        BUNDLE.checkNotNull(input, "jsonPatch.nullInput");
        BUNDLE.checkArgumentFormat(input.isContainerNode(),
            "mergePatch.notContainer", NodeType.getNodeType(input));

        return input.isArray() ? new ArrayMergePatch(input)
            : new ObjectMergePatch(input);
    }
}
