///
// User: Vitaly Sazanovich
// Date: 07/02/13
// Time: 19:23
// Email: Vitaly.Sazanovich@gmail.com
///

package world.bentobox.likes.utils.collections;


import java.util.NavigableSet;


/**
 * The interface Indexed navigable set.
 *
 * @param <E> the type parameter
 */
public interface IndexedNavigableSet<E> extends NavigableSet<E>
{
    /**
     * Returns the entry located at the index offset from the beginning of the sorted set
     *
     * @param index index of the entry
     * @return the entry located at the index (@code index) offset from the beginning of the sorted set
     * @throws ArrayIndexOutOfBoundsException if the specified index is less than 0 or greater than size-1
     */
    E exact(int index);


    /**
     * Searches the specified tree map for the specified entry using the put algorithm. Calculates its offset from the
     * beginning of the sorted map using weights.
     *
     * @param e the entry
     * @return index of the searched entry, if it is contained in the tree map; otherwise a NullPointerException is
     * thrown
     * @throws NullPointerException if the specified entry is null or does not exist
     */
    int entryIndex(E e);
}
