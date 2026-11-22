package world.bentobox.likes.panels;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings.VIEW_MODE;
import world.bentobox.likes.panels.user.TopLikesPanel;
import world.bentobox.visit.VisitAddon;
import world.bentobox.visit.managers.VisitAddonManager;
import world.bentobox.warps.Warp;
import world.bentobox.warps.managers.WarpSignsManager;

/**
 * Tests for island specific actions of {@link TopLikesPanel}: players can have multiple islands, and the top list
 * must lead to the exact island that is displayed.
 */
class TopLikesPanelTest extends CommonTestSetup
{
    @Mock
    private LikesAddon addon;

    @Mock
    private User user;

    @Mock
    private Island secondIsland;

    @Mock
    private Warp warp;

    @Mock
    private WarpSignsManager warpSignsManager;

    @Mock
    private VisitAddon visit;

    @Mock
    private VisitAddonManager visitManager;

    @Mock
    private world.bentobox.visit.configs.Settings visitSettings;

    @Mock
    private GameModeAddon gameModeAddon;

    @Mock
    private CompositeCommand playerCommand;

    @Mock
    private Location warpLocation;

    private TopLikesPanel panel;


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        when(this.user.getLocale()).thenReturn(Locale.US);
        when(this.user.getUniqueId()).thenReturn(this.uuid);

        when(this.addon.getPlugin()).thenReturn(this.plugin);
        when(this.addon.getIslands()).thenReturn(this.im);
        when(this.addon.getWarpHook()).thenReturn(this.warp);
        when(this.addon.getVisitHook()).thenReturn(this.visit);
        when(this.warp.getWarpSignsManager()).thenReturn(this.warpSignsManager);
        when(this.visit.getAddonManager()).thenReturn(this.visitManager);
        when(this.visit.getSettings()).thenReturn(this.visitSettings);
        when(this.visitSettings.getPlayerMainCommand()).thenReturn("visit");
        when(this.visitManager.preprocessTeleportation(any(), any())).thenReturn(true);
        when(this.visitManager.preprocessTeleportation(any(), any(), anyBoolean())).thenReturn(true);

        when(this.iwm.getAddon(this.world)).thenReturn(Optional.of(this.gameModeAddon));
        when(this.gameModeAddon.getPlayerCommand()).thenReturn(Optional.of(this.playerCommand));
        when(this.playerCommand.getTopLabel()).thenReturn("island");

        // Owner has two islands. First one is the primary island, and the warp sign is on it.
        when(this.island.getUniqueId()).thenReturn("island-1");
        when(this.secondIsland.getUniqueId()).thenReturn("island-2");
        when(this.secondIsland.getOwner()).thenReturn(this.uuid);
        when(this.im.getIsland(this.world, this.uuid)).thenReturn(this.island);
        when(this.warpSignsManager.getWarpLocation(this.world, this.uuid)).thenReturn(this.warpLocation);
        when(this.island.inIslandSpace(this.warpLocation)).thenReturn(true);
        when(this.secondIsland.inIslandSpace(this.warpLocation)).thenReturn(false);

        Constructor<TopLikesPanel> constructor = TopLikesPanel.class.getDeclaredConstructor(LikesAddon.class,
            User.class,
            World.class,
            String.class,
            VIEW_MODE.class);
        constructor.setAccessible(true);
        this.panel = constructor.newInstance(this.addon, this.user, this.world, "bskyblock.", VIEW_MODE.LIKES);
    }


    private boolean hasWarp(Island target) throws Exception
    {
        Method method = TopLikesPanel.class.getDeclaredMethod("hasWarp", Island.class);
        method.setAccessible(true);
        return (boolean) method.invoke(this.panel, target);
    }


    private void visitIsland(Island target) throws Exception
    {
        Method method = TopLikesPanel.class.getDeclaredMethod("visitIsland", Island.class);
        method.setAccessible(true);
        method.invoke(this.panel, target);
    }


    // ---------------------------------------------------------------------
    // Section: Warps
    // ---------------------------------------------------------------------


    @Test
    void testWarpBelongsToIslandWithSign() throws Exception
    {
        assertTrue(this.hasWarp(this.island));
    }


    @Test
    void testOtherIslandOfSameOwnerHasNoWarp() throws Exception
    {
        assertFalse(this.hasWarp(this.secondIsland));
    }


    @Test
    void testNoWarpSign() throws Exception
    {
        when(this.warpSignsManager.getWarpLocation(this.world, this.uuid)).thenReturn(null);

        assertFalse(this.hasWarp(this.island));
    }


    @Test
    void testNoWarpForUnownedIsland() throws Exception
    {
        when(this.island.getOwner()).thenReturn(null);

        assertFalse(this.hasWarp(this.island));
    }


    @Test
    void testNoWarpWithoutWarpsAddon() throws Exception
    {
        when(this.addon.getWarpHook()).thenReturn(null);

        assertFalse(this.hasWarp(this.island));
    }


    // ---------------------------------------------------------------------
    // Section: Visits
    // ---------------------------------------------------------------------


    @Test
    void testVisitPrimaryIslandUsesVisitCommand() throws Exception
    {
        this.visitIsland(this.island);

        verify(this.user).closeInventory();
        verify(this.user).performCommand("island visit " + this.uuid);
        verify(this.visitManager, never()).processTeleportation(any(), any(), any());
    }


    @Test
    void testVisitPrimaryIslandWithoutVisitCommand() throws Exception
    {
        when(this.visitSettings.getPlayerMainCommand()).thenReturn("");

        this.visitIsland(this.island);

        verify(this.user, never()).performCommand(anyString());
    }


    @Test
    void testVisitOtherIslandTeleportsToExactIsland() throws Exception
    {
        this.visitIsland(this.secondIsland);

        // The visit command would lead to the primary island.
        verify(this.user, never()).performCommand(anyString());
        verify(this.user).closeInventory();
        verify(this.visitManager).processTeleportation(this.user, this.secondIsland, this.world);
    }


    @Test
    void testVisitOtherIslandNotAllowed() throws Exception
    {
        when(this.visitManager.preprocessTeleportation(this.user, this.secondIsland)).thenReturn(false);

        this.visitIsland(this.secondIsland);

        verify(this.user, never()).closeInventory();
        verify(this.visitManager, never()).processTeleportation(any(), any(), any());
    }


    @Test
    void testVisitWhenOwnerHasNoPrimaryIsland() throws Exception
    {
        when(this.im.getIsland(this.world, this.uuid)).thenReturn(null);

        this.visitIsland(this.secondIsland);

        verify(this.visitManager).processTeleportation(this.user, this.secondIsland, this.world);
    }
}
