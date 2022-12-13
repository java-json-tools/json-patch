package com.github.fge.jsonpatch;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author {@literal @}soberich on 30-Nov-18
 */
public final class Iterables {

    private Iterables() {
    }

    /**
     * Returns the last element of {@code iterable}.
     *
     * @param <T>      underlying type being iterated
     * @param iterable type of iterable
     * @return the last element of {@code iterable}
     * @throws NoSuchElementException if the iterable is empty
     */

    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> T getLast(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        while (true) {
            T current = iterator.next();
            if (!iterator.hasNext()) {
                return current;
            }
        }
    }
}
