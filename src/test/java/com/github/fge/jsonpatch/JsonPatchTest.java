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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.JacksonUtils;
import com.google.common.collect.ImmutableList;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.github.fge.jsonpatch.JsonPatchMessages.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class JsonPatchTest
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();
    private JsonPatchOperation op1;
    private JsonPatchOperation op2;

    @BeforeMethod
    public void init()
    {
        op1 = mock(JsonPatchOperation.class);
        op2 = mock(JsonPatchOperation.class);
    }

    @Test
    public void nullInputsDuringBuildAreRejected()
        throws IOException
    {
        try {
            JsonPatch.fromJson(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), NULL_INPUT);
        }
    }

    @Test
    public void cannotPatchNull()
        throws JsonPatchException
    {
        final JsonPatch patch = new JsonPatch(ImmutableList.of(op1, op2));

        try {
            patch.apply(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), NULL_INPUT);
        }
    }

    @Test
    public void operationsAreCalledInOrder()
        throws JsonPatchException
    {
        final JsonNode node1 = FACTORY.textNode("hello");
        final JsonNode node2 = FACTORY.textNode("world");

        when(op1.apply(node1)).thenReturn(node2);

        final JsonPatch patch = new JsonPatch(ImmutableList.of(op1, op2));

        final ArgumentCaptor<JsonNode> captor
            = ArgumentCaptor.forClass(JsonNode.class);

        patch.apply(node1);
        verify(op1, only()).apply(same(node1));
        verify(op2, only()).apply(captor.capture());

        assertSame(captor.getValue(), node2);
    }

    @Test
    public void whenOneOperationFailsNextOperationIsNotCalled()
        throws JsonPatchException
    {
        final String message = "foo";
        when(op1.apply(any(JsonNode.class)))
            .thenThrow(new JsonPatchException(message));

        final JsonPatch patch = new JsonPatch(ImmutableList.of(op1, op2));

        try {
            patch.apply(FACTORY.nullNode());
            fail("No exception thrown!!");
        } catch (JsonPatchException e) {
            assertEquals(e.getMessage(), message);
        }

        verifyZeroInteractions(op2);
    }
}
