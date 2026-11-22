package world.bentobox.likes.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.CommonTestSetup;

/**
 * Tests for {@link Utils}.
 */
class UtilsTest extends CommonTestSetup
{
    @Mock
    private User user;

    @Mock
    private Island secondIsland;

    @Mock
    private GameModeAddon gameModeAddon;


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        when(this.user.getUniqueId()).thenReturn(this.uuid);
        when(this.user.isPlayer()).thenReturn(true);
        when(this.user.getLocation()).thenReturn(this.location);
        when(this.user.getTranslation(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.user.getTranslationOrNothing(anyString())).thenReturn("");

        // Player has two islands. The first one is the primary island.
        when(this.island.getWorld()).thenReturn(this.world);
        when(this.secondIsland.getWorld()).thenReturn(this.world);
        when(this.secondIsland.getMemberSet()).thenReturn(ImmutableSet.of(this.uuid));
        when(this.im.getIsland(this.world, this.uuid)).thenReturn(this.island);
        when(this.im.getIslandAt(any())).thenReturn(Optional.empty());

        AddonDescription description = new AddonDescription.Builder("main", "BSkyBlock", "1.0").build();
        when(this.gameModeAddon.getDescription()).thenReturn(description);
    }


    // ---------------------------------------------------------------------
    // Section: Multi-island lookup
    // ---------------------------------------------------------------------


    @Test
    void testGetIslandReturnsPrimaryWhenNotOnIsland()
    {
        assertSame(this.island, Utils.getIsland(this.im, this.world, this.user));
    }


    @Test
    void testGetIslandPrefersOwnIslandAtLocation()
    {
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.secondIsland));

        assertSame(this.secondIsland, Utils.getIsland(this.im, this.world, this.user));
        // Primary island does not need to be looked up.
        verify(this.im, never()).getIsland(any(World.class), any(UUID.class));
    }


    @Test
    void testGetIslandIgnoresIslandOfOtherPlayer()
    {
        when(this.secondIsland.getMemberSet()).thenReturn(ImmutableSet.of(UUID.randomUUID()));
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.secondIsland));

        assertSame(this.island, Utils.getIsland(this.im, this.world, this.user));
    }


    @Test
    void testGetIslandIgnoresIslandInOtherGameMode()
    {
        when(this.secondIsland.getWorld()).thenReturn(mock(World.class));
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.secondIsland));

        assertSame(this.island, Utils.getIsland(this.im, this.world, this.user));
    }


    @Test
    void testGetIslandNonPlayerUsesPrimary()
    {
        when(this.user.isPlayer()).thenReturn(false);
        when(this.im.getIslandAt(any())).thenReturn(Optional.of(this.secondIsland));

        assertSame(this.island, Utils.getIsland(this.im, this.world, this.user));
        verify(this.user, never()).getLocation();
    }


    @Test
    void testGetIslandNoIsland()
    {
        when(this.im.getIsland(this.world, this.uuid)).thenReturn(null);

        assertNull(Utils.getIsland(this.im, this.world, this.user));
    }


    @Test
    void testGetIslandOfOtherPlayerAtViewerLocation()
    {
        // Viewer stands on the second island of the target player.
        Location viewerLocation = mock(Location.class);
        when(this.im.getIslandAt(viewerLocation)).thenReturn(Optional.of(this.secondIsland));

        assertSame(this.secondIsland, Utils.getIsland(this.im, this.world, this.uuid, viewerLocation));
        assertSame(this.island, Utils.getIsland(this.im, this.world, this.uuid, null));
    }


    // ---------------------------------------------------------------------
    // Section: GameMode
    // ---------------------------------------------------------------------


    @Test
    void testGetGameModeFromWorld()
    {
        when(this.iwm.getAddon(this.world)).thenReturn(Optional.of(this.gameModeAddon));

        assertEquals("BSkyBlock", Utils.getGameMode(this.world));
    }


    @Test
    void testGetGameModeNotGameWorld()
    {
        assertNull(Utils.getGameMode(this.world));
    }


    @Test
    void testGetGameModeFromAddon()
    {
        assertEquals("BSkyBlock", Utils.getGameMode(this.gameModeAddon));
    }


    // ---------------------------------------------------------------------
    // Section: Values
    // ---------------------------------------------------------------------


    @Test
    void testGetNextValue()
    {
        String[] values = {"a", "b", "c"};

        assertEquals("b", Utils.getNextValue(values, "a"));
        assertEquals("a", Utils.getNextValue(values, "c"));
        assertEquals("x", Utils.getNextValue(values, "x"));
    }


    @Test
    void testGetPreviousValue()
    {
        String[] values = {"a", "b", "c"};

        assertEquals("a", Utils.getPreviousValue(values, "b"));
        assertEquals("c", Utils.getPreviousValue(values, "a"));
        assertEquals("x", Utils.getPreviousValue(values, "x"));
    }


    // ---------------------------------------------------------------------
    // Section: Permissions
    // ---------------------------------------------------------------------


    private static PermissionAttachmentInfo permission(String name)
    {
        PermissionAttachmentInfo info = mock(PermissionAttachmentInfo.class);
        when(info.getPermission()).thenReturn(name);
        return info;
    }


    @Test
    void testGetPermissionValue()
    {
        Set<PermissionAttachmentInfo> permissions =
            Set.of(permission("bskyblock.likes.icon.STONE"), permission("bskyblock.other"));
        when(this.user.getEffectivePermissions()).thenReturn(permissions);

        assertEquals("STONE", Utils.getPermissionValue(this.user, "bskyblock.likes.icon", null));
        assertEquals("STONE", Utils.getPermissionValue(this.user, "bskyblock.likes.icon.", null));
    }


    @Test
    void testGetPermissionValueWildcardIsSkipped()
    {
        Set<PermissionAttachmentInfo> permissions = Set.of(permission("bskyblock.likes.icon.*"));
        when(this.user.getEffectivePermissions()).thenReturn(permissions);

        assertEquals("default", Utils.getPermissionValue(this.user, "bskyblock.likes.icon", "default"));
    }


    @Test
    void testGetPermissionValueNoPermission()
    {
        when(this.user.getEffectivePermissions()).thenReturn(Set.of());

        assertEquals("default", Utils.getPermissionValue(this.user, "bskyblock.likes.icon", "default"));
    }


    @Test
    void testGetPermissionValueNotPlayer()
    {
        when(this.user.isPlayer()).thenReturn(false);

        assertEquals("default", Utils.getPermissionValue(this.user, "bskyblock.likes.icon", "default"));
        assertEquals("default", Utils.getPermissionValue(null, "bskyblock.likes.icon", "default"));
    }


    // ---------------------------------------------------------------------
    // Section: Text
    // ---------------------------------------------------------------------


    @Test
    void testSendMessageAddsPrefix()
    {
        Utils.sendMessage(this.user, "hello");

        verify(this.user).sendMessage(Constants.CONVERSATIONS + "prefix" + "hello");
    }


    @Test
    void testPrettifyObjectNull()
    {
        assertEquals("", Utils.prettifyObject(null, this.user));
        assertEquals("", Utils.prettifyDescription(null, this.user));
    }


    @Test
    void testPrettifyObjectAddonNameTranslation()
    {
        when(this.user.getTranslationOrNothing(Constants.MATERIALS + "stone.name")).thenReturn("Rock");

        assertEquals("Rock", Utils.prettifyObject(Material.STONE, this.user));
    }


    @Test
    void testPrettifyObjectAddonTranslation()
    {
        when(this.user.getTranslationOrNothing(Constants.MATERIALS + "stone")).thenReturn("Rock");

        assertEquals("Rock", Utils.prettifyObject(Material.STONE, this.user));
    }


    @Test
    void testPrettifyObjectGeneralTranslation()
    {
        when(this.user.getTranslationOrNothing("materials.stone")).thenReturn("Rock");

        assertEquals("Rock", Utils.prettifyObject(Material.STONE, this.user));
    }


    @Test
    void testPrettifyDescription()
    {
        assertEquals("", Utils.prettifyDescription(Material.STONE, this.user));

        when(this.user.getTranslationOrNothing(Constants.MATERIALS + "stone.description")).thenReturn("Hard");

        assertEquals("Hard", Utils.prettifyDescription(Material.STONE, this.user));
    }
}
