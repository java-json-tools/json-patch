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

package com.github.fge.jsonpatch.diff;

import java.util.Set;
import java.util.TreeSet;

public final class Sets
{

    private Sets()
    {
    }

    public static <E> Set<E> difference(final Set<E> set1, final Set<E> set2)
    {
        Set<E> temp = new TreeSet<E>(set1);
        temp.removeAll(set2);
        return temp;
    }

    public static <E> Set<E> intersection(Set<E> set1, Set<E> set2)
    {
        Set<E> temp = new TreeSet<E>();
        for (E x : set1)
            if (set2.contains(x))
                temp.add(x);
        return temp;
    }
}
