package world.bentobox.likes.database.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.bentobox.api.logs.LogEntry;

/**
 * Tests for {@link LikesObject}.
 */
class LikesObjectTest
{
    private LikesObject object;

    private final UUID user = UUID.randomUUID();

    private final UUID other = UUID.randomUUID();


    @BeforeEach
    void setUp()
    {
        this.object = new LikesObject();
    }


    @Test
    void testNewObjectIsEmpty()
    {
        assertEquals(0, this.object.getLikes());
        assertEquals(0, this.object.getDislikes());
        assertEquals(0, this.object.getStars());
        assertEquals(0, this.object.getRank());
        assertEquals(0, this.object.getStarsValue());
        assertEquals(0, this.object.numberOfStars());
        assertFalse(this.object.isNotEmpty());
        assertFalse(this.object.isChanged());
        assertTrue(this.object.getHistory().isEmpty());
    }


    @Test
    void testAddLike()
    {
        this.object.addLike(this.user);

        assertEquals(1, this.object.getLikes());
        assertTrue(this.object.hasLiked(this.user));
        assertFalse(this.object.hasLiked(this.other));
        assertTrue(this.object.isNotEmpty());
        assertTrue(this.object.isChanged());
    }


    @Test
    void testRemoveLike()
    {
        this.object.addLike(this.user);
        this.object.setChanged(false);

        this.object.removeLike(this.other);
        assertEquals(1, this.object.getLikes());
        assertFalse(this.object.isChanged());

        this.object.removeLike(this.user);
        assertEquals(0, this.object.getLikes());
        assertFalse(this.object.hasLiked(this.user));
        assertTrue(this.object.isChanged());
    }


    @Test
    void testAddDislike()
    {
        this.object.addDislike(this.user);

        assertEquals(1, this.object.getDislikes());
        assertTrue(this.object.hasDisliked(this.user));
        assertEquals(-1, this.object.getRank());
        assertTrue(this.object.isNotEmpty());
    }


    @Test
    void testRemoveDislike()
    {
        this.object.addDislike(this.user);
        this.object.removeDislike(this.other);
        assertEquals(1, this.object.getDislikes());

        this.object.removeDislike(this.user);
        assertEquals(0, this.object.getDislikes());
        assertFalse(this.object.hasDisliked(this.user));
    }


    @Test
    void testLikeAndDislikeAreExclusive()
    {
        this.object.addLike(this.user);
        this.object.addDislike(this.user);

        assertFalse(this.object.hasLiked(this.user));
        assertTrue(this.object.hasDisliked(this.user));
        assertEquals(0, this.object.getLikes());
        assertEquals(1, this.object.getDislikes());

        this.object.addLike(this.user);

        assertTrue(this.object.hasLiked(this.user));
        assertFalse(this.object.hasDisliked(this.user));
        assertEquals(1, this.object.getLikes());
        assertEquals(0, this.object.getDislikes());
    }


    @Test
    void testRank()
    {
        this.object.addLike(this.user);
        this.object.addLike(this.other);
        this.object.addDislike(UUID.randomUUID());

        assertEquals(1, this.object.getRank());
    }


    @Test
    void testStars()
    {
        this.object.addStars(this.user, 5);
        this.object.addStars(this.other, 2);

        assertEquals(7, this.object.getStars());
        assertEquals(2, this.object.numberOfStars());
        assertEquals(3.5, this.object.getStarsValue());
        assertTrue(this.object.hasStarred(this.user));
        assertEquals(5, this.object.getStarred(this.user));
        assertEquals(0, this.object.getStarred(UUID.randomUUID()));
        assertTrue(this.object.isNotEmpty());
    }


    @Test
    void testRemoveStars()
    {
        this.object.addStars(this.user, 5);
        this.object.addStars(this.other, 2);
        this.object.setChanged(false);

        this.object.removeStars(UUID.randomUUID());
        assertFalse(this.object.isChanged());

        this.object.removeStars(this.user);

        assertEquals(2, this.object.getStars());
        assertEquals(2.0, this.object.getStarsValue());
        assertFalse(this.object.hasStarred(this.user));
        assertTrue(this.object.isChanged());
    }


    @Test
    void testClearCollections()
    {
        this.object.addLike(this.user);
        this.object.addDislike(this.other);
        this.object.addStars(this.user, 3);

        this.object.clearCollections();

        assertTrue(this.object.getLikedBy().isEmpty());
        assertTrue(this.object.getDislikedBy().isEmpty());
        assertTrue(this.object.getStarredBy().isEmpty());
    }


    @Test
    void testLogRecord()
    {
        this.object.addLogRecord(new LogEntry.Builder("ADD_LIKE").data("user-id", this.user.toString()).build());

        assertEquals(1, this.object.getHistory().size());
        assertTrue(this.object.isChanged());
    }


    @Test
    void testSetters()
    {
        Set<UUID> likedBy = new HashSet<>(Set.of(this.user));
        Set<UUID> dislikedBy = new HashSet<>(Set.of(this.other));
        Map<UUID, Integer> starredBy = new HashMap<>(Map.of(this.user, 4));

        this.object.setUniqueId("island");
        this.object.setGameMode("BSkyBlock");
        this.object.setLikes(4);
        this.object.setDislikes(3);
        this.object.setStars(4);
        this.object.setLikedBy(likedBy);
        this.object.setDislikedBy(dislikedBy);
        this.object.setStarredBy(starredBy);
        this.object.setHistory(new LinkedList<>());

        assertEquals("island", this.object.getUniqueId());
        assertEquals("BSkyBlock", this.object.getGameMode());
        assertEquals(4, this.object.getLikes());
        assertEquals(3, this.object.getDislikes());
        assertEquals(4, this.object.getStars());
        assertEquals(likedBy, this.object.getLikedBy());
        assertEquals(dislikedBy, this.object.getDislikedBy());
        assertEquals(starredBy, this.object.getStarredBy());
        assertTrue(this.object.getHistory().isEmpty());
        assertTrue(this.object.isChanged());

        this.object.setChanged(false);
        assertFalse(this.object.isChanged());
    }
}
