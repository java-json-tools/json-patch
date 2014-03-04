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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import org.testng.annotations.Test;

import java.io.IOException;

public final class AddOperationTest
    extends JsonPatchOperationTest
{
    public AddOperationTest()
        throws IOException
    {
        super("add");
    }

    @Test(expectedExceptions = JsonPatchException.class, expectedExceptionsMessageRegExp = "no such path in target JSON document")
    public void addingToANonContainerNodeThrowsException()
            throws JsonPatchException, JsonPointerException
    {
        final ObjectNode node = JacksonUtils.nodeFactory().objectNode();
        final TextNode textNode = JacksonUtils.nodeFactory().textNode("bar");
        node.put("foo", textNode);
        final JsonPointer p = new JsonPointer("/foo/f");
        final JsonPatchOperation op = new AddOperation(p, JacksonUtils.nodeFactory().nullNode());
        op.apply(node);
    }
}
