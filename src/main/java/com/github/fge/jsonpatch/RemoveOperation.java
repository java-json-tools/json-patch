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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * JSON Path {@code remove} operation
 *
 * <p>This operation only takes one pointer ({@code path}) as an argument. It
 * is an error condition if no JSON value exists at that pointer.</p>
 */
public final class RemoveOperation
    extends JsonPatchOperation
{
    @JsonCreator
    public RemoveOperation(@JsonProperty("path") final JsonPointer path)
    {
        super(path);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        if (path.isEmpty())
            return MissingNode.getInstance();
        if (path.path(node).isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage("NO_SUCH_PATH"));
        final SplitPointer split = new SplitPointer(path);
        final JsonNode ret = node.deepCopy();
        final JsonNode parentNode = split.parent.get(ret);
        final String raw = split.lastToken.getToken().getRaw();
        if (parentNode.isObject())
            ((ObjectNode) parentNode).remove(raw);
        else
            ((ArrayNode) parentNode).remove(Integer.parseInt(raw));
        return ret;
    }

    @Override
    public String toString()
    {
        return "remove: " + super.toString();
    }
}
