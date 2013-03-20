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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public final class RemoveOperationTest
    extends JsonPatchOperationTest
{
    public RemoveOperationTest()
        throws IOException
    {
        super("remove", RemoveOperation.class);
    }

    @Test
    public void removingRootReturnsMissingNode()
        throws JsonPatchException
    {
        final JsonNode node = JacksonUtils.nodeFactory().nullNode();
        final JsonPatchOperation op = new RemoveOperation(JsonPointer.empty());
        final JsonNode ret = op.apply(node);
        assertTrue(ret.isMissingNode());
    }
}
