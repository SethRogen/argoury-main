package com.runescape.logic.utility;

/**
 * This interface is made to allow easy implementation of comparing enums.
 *
 * @param <E> The specified enum.
 * @author Maxime
 */
public interface EnumBasedComparator<E extends java.lang.Enum<E>> {

    /**
     * This method can be implemented to compare two enums and return an integer
     * based on their difference.
     *
     * @param e1 The first enum to compare with the second.
     * @param e2 The second enum to compare with the first.
     * @return Depending on the implementation an integer will be returned representing
     *         an abstract difference between the two elements.
     */
    public int compare(E e1, E e2);

}
