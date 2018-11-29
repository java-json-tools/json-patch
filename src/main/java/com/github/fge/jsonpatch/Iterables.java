package com.github.fge.jsonpatch;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:S.Nasin@prof-schumann.ru">Sergey Nazin</a> on 30-Nov-18
 */
public final class Iterables {

    private Iterables() {}

    /**
     * Returns the last element of {@code iterable}.
     *
     * @param iterable type of iterable
     * @return the last element of {@code iterable}
     * @throws NoSuchElementException if the iterable is empty
     */
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
