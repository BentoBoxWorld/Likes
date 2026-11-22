package world.bentobox.likes.utils.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IndexedTreeSet} methods that are used by Likes addon.
 */
class IndexedTreeSetTest
{
    private IndexedTreeSet<Integer> set;


    @BeforeEach
    void setUp()
    {
        this.set = new IndexedTreeSet<>(Comparator.reverseOrder());
    }


    @Test
    void testExactOnEmptySet()
    {
        assertNull(this.set.exact(0));
    }


    @Test
    void testExactOutOfBounds()
    {
        this.set.addAll(List.of(1, 2, 3));

        assertNull(this.set.exact(-1));
        assertNull(this.set.exact(3));
    }


    @Test
    void testExactFollowsOrder()
    {
        for (int i = 0; i < 100; i++)
        {
            this.set.add(i);
        }

        for (int i = 0; i < 100; i++)
        {
            assertEquals(99 - i, this.set.exact(i));
            assertEquals(i, this.set.entryIndex(99 - i));
        }
    }


    @Test
    void testRemoveKeepsIndexes()
    {
        this.set.addAll(List.of(10, 20, 30, 40));

        assertTrue(this.set.remove(30));
        assertFalse(this.set.remove(30));

        assertEquals(3, this.set.size());
        assertFalse(this.set.contains(30));
        assertEquals(40, this.set.exact(0));
        assertEquals(20, this.set.exact(1));
        assertEquals(10, this.set.exact(2));
        assertEquals(1, this.set.entryIndex(20));
    }


    @Test
    void testNoDuplicates()
    {
        assertTrue(this.set.add(1));
        assertFalse(this.set.add(1));
        assertEquals(1, this.set.size());
    }
}
