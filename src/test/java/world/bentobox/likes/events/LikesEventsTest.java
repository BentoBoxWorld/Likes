package world.bentobox.likes.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Tests for Likes addon events.
 */
class LikesEventsTest
{
    private final UUID user = UUID.randomUUID();

    private final UUID other = UUID.randomUUID();


    @Test
    void testLikeAddEvent()
    {
        LikeAddEvent event = new LikeAddEvent(this.user, "island");

        assertEquals(this.user, event.getUser());
        assertEquals("island", event.getIslandId());

        event.setUser(this.other);
        event.setIslandId("other");

        assertEquals(this.other, event.getUser());
        assertEquals("other", event.getIslandId());
        assertNotNull(event.getHandlers());
        assertSame(LikeAddEvent.getHandlerList(), event.getHandlers());
    }


    @Test
    void testLikeRemoveEvent()
    {
        LikeRemoveEvent event = new LikeRemoveEvent(this.user, "island");

        assertEquals(this.user, event.getUser());
        assertEquals("island", event.getIslandId());

        event.setUser(this.other);
        event.setIslandId("other");

        assertEquals(this.other, event.getUser());
        assertEquals("other", event.getIslandId());
        assertSame(LikeRemoveEvent.getHandlerList(), event.getHandlers());
    }


    @Test
    void testDislikeAddEvent()
    {
        DislikeAddEvent event = new DislikeAddEvent(this.user, "island");

        assertEquals(this.user, event.getUser());
        assertEquals("island", event.getIslandId());

        event.setUser(this.other);
        event.setIslandId("other");

        assertEquals(this.other, event.getUser());
        assertEquals("other", event.getIslandId());
        assertSame(DislikeAddEvent.getHandlerList(), event.getHandlers());
    }


    @Test
    void testDislikeRemoveEvent()
    {
        DislikeRemoveEvent event = new DislikeRemoveEvent(this.user, "island");

        assertEquals(this.user, event.getUser());
        assertEquals("island", event.getIslandId());

        event.setUser(this.other);
        event.setIslandId("other");

        assertEquals(this.other, event.getUser());
        assertEquals("other", event.getIslandId());
        assertSame(DislikeRemoveEvent.getHandlerList(), event.getHandlers());
    }


    @Test
    void testStarsAddEvent()
    {
        StarsAddEvent event = new StarsAddEvent(this.user, 4, "island");

        assertEquals(this.user, event.getUser());
        assertEquals(4, event.getValue());
        assertEquals("island", event.getIslandId());

        event.setUser(this.other);
        event.setValue(2);
        event.setIslandId("other");

        assertEquals(this.other, event.getUser());
        assertEquals(2, event.getValue());
        assertEquals("other", event.getIslandId());
        assertSame(StarsAddEvent.getHandlerList(), event.getHandlers());
    }


    @Test
    void testStarsRemoveEvent()
    {
        StarsRemoveEvent event = new StarsRemoveEvent(this.user, "island");

        assertEquals(this.user, event.getUser());
        assertEquals("island", event.getIslandId());

        event.setUser(this.other);
        event.setIslandId("other");

        assertEquals(this.other, event.getUser());
        assertEquals("other", event.getIslandId());
        assertSame(StarsRemoveEvent.getHandlerList(), event.getHandlers());
    }
}
