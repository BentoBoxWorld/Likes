package world.bentobox.likes.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.api.events.island.IslandUnregisteredEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.managers.LikesManager;

/**
 * Tests for {@link ResetListener}.
 */
class ResetListenerTest extends CommonTestSetup
{
    @Mock
    private LikesAddon addon;

    @Mock
    private LikesManager manager;

    @Mock
    private Settings settings;

    @Mock
    private Island secondIsland;

    private ResetListener listener;


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        when(this.addon.getAddonManager()).thenReturn(this.manager);
        when(this.addon.getSettings()).thenReturn(this.settings);
        when(this.settings.isResetLikes()).thenReturn(true);

        when(this.island.getUniqueId()).thenReturn("island-1");
        when(this.island.getWorld()).thenReturn(this.world);
        when(this.secondIsland.getUniqueId()).thenReturn("island-2");
        when(this.secondIsland.getWorld()).thenReturn(this.world);

        this.listener = new ResetListener(this.addon);
    }


    private Island oldIsland()
    {
        Island oldIsland = mock(Island.class);
        when(oldIsland.getCenter()).thenReturn(this.location);
        return oldIsland;
    }


    @Test
    void testIslandCreatedResetsLikes()
    {
        this.listener.onIslandCreated(new IslandCreatedEvent(this.island, this.uuid, false, this.location));

        verify(this.manager).resetLikes(this.uuid, "island-1", this.world);
    }


    @Test
    void testIslandCreatedIgnoresResetSetting()
    {
        when(this.settings.isResetLikes()).thenReturn(false);

        this.listener.onIslandCreated(new IslandCreatedEvent(this.island, this.uuid, false, this.location));

        verify(this.manager).resetLikes(this.uuid, "island-1", this.world);
    }


    @Test
    void testSecondIslandCreatedDoesNotTouchFirstIsland()
    {
        this.listener.onIslandCreated(
            new IslandCreatedEvent(this.secondIsland, this.uuid, false, this.location));

        verify(this.manager).resetLikes(this.uuid, "island-2", this.world);
        verify(this.manager, never()).resetLikes(this.uuid, "island-1", this.world);
    }


    @Test
    void testIslandResetted()
    {
        this.listener.onIslandCreated(
            new IslandResettedEvent(this.island, this.uuid, false, this.location, this.oldIsland()));

        verify(this.manager).resetLikes(this.uuid, "island-1", this.world);
    }


    @Test
    void testIslandResettedDisabled()
    {
        when(this.settings.isResetLikes()).thenReturn(false);

        this.listener.onIslandCreated(
            new IslandResettedEvent(this.island, this.uuid, false, this.location, this.oldIsland()));

        verify(this.manager, never()).resetLikes(any(), any(), any());
    }


    @Test
    void testIslandUnregistered()
    {
        this.listener.onIslandUnregister(new IslandUnregisteredEvent(this.island, this.uuid, false, this.location));

        verify(this.manager).resetLikes(this.uuid, "island-1", this.world);
    }


    @Test
    void testIslandUnregisteredDisabled()
    {
        when(this.settings.isResetLikes()).thenReturn(false);

        this.listener.onIslandUnregister(new IslandUnregisteredEvent(this.island, this.uuid, false, this.location));

        verify(this.manager, never()).resetLikes(any(), any(), any());
    }


    @Test
    void testIslandDeleteRemovesOnlyThatIsland()
    {
        this.listener.onIslandDelete(new IslandDeleteEvent(this.secondIsland, this.uuid, false, this.location));

        verify(this.manager).removeObject("island-2");
        verify(this.manager, never()).removeObject("island-1");
    }
}
