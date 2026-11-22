package world.bentobox.likes;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import world.bentobox.bentobox.api.addons.Addon;

/**
 * Tests for {@link LikesPladdon}.
 */
class LikesPladdonTest
{
    @Test
    void testGetAddon()
    {
        LikesPladdon pladdon = Mockito.mock(LikesPladdon.class, Mockito.CALLS_REAL_METHODS);

        Addon addon = pladdon.getAddon();

        assertInstanceOf(LikesAddon.class, addon);
        // Addon is created only once.
        assertSame(addon, pladdon.getAddon());
    }
}
