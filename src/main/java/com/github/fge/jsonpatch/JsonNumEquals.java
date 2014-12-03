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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class JsonNumEquals
{

    public static boolean equivalent(final JsonNode a, final JsonNode b)
    {
        /*
         * If both are numbers, delegate to the helper method
         */
        if (a.isNumber() && b.isNumber())
            return numEquals(a, b);

        final JsonNodeType typeA = a.getNodeType();
        final JsonNodeType typeB = b.getNodeType();

        /*
         * If they are of different types, no dice
         */
        if (typeA != typeB)
            return false;

        /*
         * For all other primitive types than numbers, trust JsonNode
         */
        if (!a.isContainerNode())
            return a.equals(b);

        /*
         * OK, so they are containers (either both arrays or objects due to the
         * test on types above). They are obviously not equal if they do not
         * have the same number of elements/members.
         */
        if (a.size() != b.size())
            return false;

        /*
         * Delegate to the appropriate method according to their type.
         */
        return typeA == JsonNodeType.ARRAY ? arrayEquals(a, b) : objectEquals(a, b);
    }

    private static boolean numEquals(final JsonNode a, final JsonNode b)
    {
        /*
         * If both numbers are integers, delegate to JsonNode.
         */
        if (a.isIntegralNumber() && b.isIntegralNumber())
            return a.equals(b);

        /*
         * Otherwise, compare decimal values.
         */
        return a.decimalValue().compareTo(b.decimalValue()) == 0;
    }

    private static boolean arrayEquals(final JsonNode a, final JsonNode b)
    {
        /*
         * We are guaranteed here that arrays are the same size.
         */
        final int size = a.size();

        for (int i = 0; i < size; i++)
            if (!equivalent(a.get(i), b.get(i)))
                return false;

        return true;
    }

    private static boolean objectEquals(final JsonNode a, final JsonNode b)
    {
        /*
         * Grab the key set from the first node
         */
        final Set<String> keys = newHashSet(a.fieldNames());

        /*
         * Grab the key set from the second node, and see if both sets are the
         * same. If not, objects are not equal, no need to check for children.
         */
        final Set<String> set = newHashSet(b.fieldNames());
        if (!set.equals(keys))
            return false;

        /*
         * Test each member individually.
         */
        for (final String key: keys)
            if (!equivalent(a.get(key), b.get(key)))
                return false;

        return true;
    }

    private static Set<String> newHashSet(Iterator<String> fields) {
        final Set<String> elements = new HashSet<String>();

        while(fields.hasNext()) {
            elements.add(fields.next());
        }

        return elements;
    }

}
