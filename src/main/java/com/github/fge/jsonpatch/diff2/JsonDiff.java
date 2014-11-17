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

package com.github.fge.jsonpatch.diff2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.MoveOperation;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@ParametersAreNonnullByDefault
public final class JsonDiff
{
    private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

    private static final Equivalence<JsonNode> EQUIVALENCE
        = JsonNumEquals.getInstance();

    private JsonDiff()
    {
    }

    public static JsonPatch asJsonPatch(final JsonNode first,
        final JsonNode second)
    {
        final Map<JsonPointer, JsonNode> unchanged
            = getUnchangedValues(first, second);
        final DiffProcessor processor = new DiffProcessor(unchanged);

        generateDiffs(processor, JsonPointer.empty(), first, second);
        return processor.getPatch();
    }

    public static JsonNode asJson(final JsonNode first, final JsonNode second)
    {
        final String s;
        try {
            s = MAPPER.writeValueAsString(asJsonPatch(first, second));
            return MAPPER.readTree(s);
        } catch (IOException e) {
            throw new RuntimeException("cannot generate JSON diff", e);
        }
    }

    private static void generateDiffs(final DiffProcessor processor,
        final JsonPointer pointer, final JsonNode first, final JsonNode second)
    {
        if (EQUIVALENCE.equivalent(first, second))
            return;

        final NodeType firstType = NodeType.getNodeType(first);
        final NodeType secondType = NodeType.getNodeType(second);

        /*
         * Node types differ: generate a replacement operation.
         */
        if (firstType != secondType) {
            processor.valueReplaced(pointer, first, second);
            return;
        }

        /*
         * If we reach this point, it means that both nodes are the same type,
         * but are not equivalent.
         *
         * If this is not a container, generate a replace operation.
         */
        if (!first.isContainerNode()) {
            processor.valueReplaced(pointer, first, second);
            return;
        }

        /*
         * If we reach this point, both nodes are either objects or arrays;
         * delegate.
         */
        if (firstType == NodeType.OBJECT)
            generateObjectDiffs(processor, pointer, (ObjectNode) first,
                (ObjectNode) second);
        else // array
            generateArrayDiffs(processor, pointer, (ArrayNode) first,
                (ArrayNode) second);
    }

    private static void generateObjectDiffs(final DiffProcessor processor,
        final JsonPointer pointer, final ObjectNode first,
        final ObjectNode second)
    {
        final Set<String> firstFields
            = Sets.newTreeSet(Sets.newHashSet(first.fieldNames()));
        final Set<String> secondFields
            = Sets.newTreeSet(Sets.newHashSet(second.fieldNames()));

        for (final String field: Sets.difference(firstFields, secondFields))
            processor.valueRemoved(pointer.append(field), first.get(field));

        for (final String field: Sets.difference(secondFields, firstFields))
            processor.valueAdded(pointer.append(field), second.get(field));

        for (final String field: Sets.intersection(firstFields, secondFields))
            generateDiffs(processor, pointer.append(field), first.get(field),
                second.get(field));
    }

    private static void generateArrayDiffs(final DiffProcessor processor,
        final JsonPointer pointer, final ArrayNode first,
        final ArrayNode second)
    {
        final int firstSize = first.size();
        final int secondSize = second.size();
        final int size = Math.min(firstSize, secondSize);

        for (int index = 0; index < size; index++)
            generateDiffs(processor, pointer.append(index), first.get(index),
                second.get(index));

        /*
         * Source array is larger; in this case, elements are removed from the
         * target; the index of removal is always the original arrays's length.
         */
        for (int index = size; index < firstSize; index++)
            processor.valueRemoved(pointer.append(size), first.get(index));

        // Deal with the destination array being larger...
        for (int index = size; index < secondSize; index++)
            processor.valueAdded(pointer.append("-"), second.get(index));
    }


    @VisibleForTesting
    static Map<JsonPointer, JsonNode> getUnchangedValues(final JsonNode first,
        final JsonNode second)
    {
        final Map<JsonPointer, JsonNode> ret = Maps.newHashMap();
        computeUnchanged(ret, JsonPointer.empty(), first, second);
        return ret;
    }

    private static void computeUnchanged(final Map<JsonPointer, JsonNode> ret,
        final JsonPointer pointer, final JsonNode first, final JsonNode second)
    {
        if (EQUIVALENCE.equivalent(first, second)) {
            ret.put(pointer, second);
            return;
        }

        final NodeType firstType = NodeType.getNodeType(first);
        final NodeType secondType = NodeType.getNodeType(second);

        if (firstType != secondType)
            return; // nothing in common

        // We know they are both the same type, so...

        switch (firstType) {
            case OBJECT:
                computeObject(ret, pointer, first, second);
                break;
            case ARRAY:
                computeArray(ret, pointer, first, second);
            default:
                /* nothing */
        }
    }

    private static void computeObject(final Map<JsonPointer, JsonNode> ret,
        final JsonPointer pointer, final JsonNode first, final JsonNode second)
    {
        final Iterator<String> firstFields = first.fieldNames();

        String name;

        while (firstFields.hasNext()) {
            name = firstFields.next();
            if (!second.has(name))
                continue;
            computeUnchanged(ret, pointer.append(name), first.get(name),
                second.get(name));
        }
    }

    private static void computeArray(final Map<JsonPointer, JsonNode> ret,
        final JsonPointer pointer, final JsonNode first, final JsonNode second)
    {
        final int size = Math.min(first.size(), second.size());

        for (int i = 0; i < size; i++)
            computeUnchanged(ret, pointer.append(i), first.get(i),
                second.get(i));
    }

    public static void main(final String... args)
        throws IOException
    {
        final ImmutableMap.Builder<JsonPointer, JsonNode> builder
            = ImmutableMap.builder();

        builder.put(JsonPointer.of("a"), JsonNodeFactory.instance.arrayNode());

        final ImmutableMap<JsonPointer, JsonNode> map = builder.build();

        final ObjectMapper mapper = JacksonUtils.newMapper();

        final JsonNode node = mapper.convertValue(map, JsonNode.class);
        System.out.println(node);

        final TypeReference<Map<JsonPointer, JsonNode>> typeRef
            = new TypeReference<Map<JsonPointer, JsonNode>>()
        {
        };

        final Map<JsonPointer, JsonNode> map2
            = mapper.readValue(node.traverse(), typeRef);

        System.out.println(map2);

        System.out.println(mapper.writeValueAsString(new MoveOperation
            (JsonPointer.of("a"), JsonPointer.of("b"))));
    }
}
