package world.bentobox.likes.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableSet;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.commands.admin.AdminCommand;
import world.bentobox.likes.commands.admin.EditSettingsCommand;
import world.bentobox.likes.commands.user.PlayerCommand;
import world.bentobox.likes.commands.user.PlayerTopCommand;
import world.bentobox.likes.commands.user.PlayerViewCommand;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.config.Settings.LikeMode;
import world.bentobox.likes.config.Settings.VIEW_MODE;
import world.bentobox.likes.panels.admin.AdminPanel;
import world.bentobox.likes.panels.admin.EditSettingsPanel;
import world.bentobox.likes.panels.user.LikesManagePanel;
import world.bentobox.likes.panels.user.LikesViewPanel;
import world.bentobox.likes.panels.user.TopLikesPanel;
import world.bentobox.likes.utils.Constants;

/**
 * Tests for Likes addon commands.
 */
class PlayerCommandsTest extends CommonTestSetup
{
    private static final String PERMISSION_PREFIX = "bskyblock.";

    @Mock
    private LikesAddon addon;

    @Mock
    private CompositeCommand parent;

    @Mock
    private PlayersManager playersManager;

    @Mock
    private User user;

    @Mock
    private Island secondIsland;

    private Settings settings;

    private MockedStatic<LikesViewPanel> viewPanel;

    private MockedStatic<LikesManagePanel> managePanel;

    private MockedStatic<TopLikesPanel> topPanel;

    private MockedStatic<AdminPanel> adminPanel;

    private MockedStatic<EditSettingsPanel> settingsPanel;

    private final UUID targetUuid = UUID.randomUUID();


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        // Command manager
        CommandsManager commandsManager = mock(CommandsManager.class);
        when(this.plugin.getCommandsManager()).thenReturn(commandsManager);

        // Parent command
        when(this.parent.getWorld()).thenReturn(this.world);
        when(this.parent.getPermissionPrefix()).thenReturn(PERMISSION_PREFIX);
        when(this.parent.getTopLabel()).thenReturn("island");
        when(this.parent.getLabel()).thenReturn("island");
        when(this.parent.getUsage()).thenReturn("/island");
        when(this.parent.getAddon()).thenReturn(this.addon);

        // Addon
        this.settings = new Settings();
        when(this.addon.getSettings()).thenReturn(this.settings);
        when(this.addon.getIslands()).thenReturn(this.im);
        when(this.addon.getPlayers()).thenReturn(this.playersManager);
        when(this.addon.getPlugin()).thenReturn(this.plugin);

        // User
        when(this.user.getUniqueId()).thenReturn(this.uuid);
        when(this.user.isPlayer()).thenReturn(true);
        when(this.user.getLocation()).thenReturn(this.location);
        when(this.user.hasPermission(anyString())).thenReturn(true);
        when(this.user.getTranslation(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.user.getTranslation(anyString(), any(String[].class))).
            thenAnswer(invocation -> invocation.getArgument(0));

        // Player has two islands. First one is the primary one.
        when(this.island.getWorld()).thenReturn(this.world);
        when(this.island.isOwned()).thenReturn(true);
        when(this.secondIsland.getWorld()).thenReturn(this.world);
        when(this.secondIsland.isOwned()).thenReturn(true);
        when(this.secondIsland.getOwner()).thenReturn(this.uuid);
        when(this.secondIsland.getMemberSet()).thenReturn(ImmutableSet.of(this.uuid));
        when(this.im.getIsland(this.world, this.uuid)).thenReturn(this.island);
        when(this.im.getIslandAt(any())).thenReturn(Optional.empty());

        // Panels are not opened in command tests.
        this.viewPanel = Mockito.mockStatic(LikesViewPanel.class);
        this.managePanel = Mockito.mockStatic(LikesManagePanel.class);
        this.topPanel = Mockito.mockStatic(TopLikesPanel.class);
        this.adminPanel = Mockito.mockStatic(AdminPanel.class);
        this.settingsPanel = Mockito.mockStatic(EditSettingsPanel.class);
    }


    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        this.viewPanel.closeOnDemand();
        this.managePanel.closeOnDemand();
        this.topPanel.closeOnDemand();
        this.adminPanel.closeOnDemand();
        this.settingsPanel.closeOnDemand();
        super.tearDown();
    }


    // ---------------------------------------------------------------------
    // Section: Main player command
    // ---------------------------------------------------------------------


    @Test
    void testPlayerCommandSetup()
    {
        PlayerCommand command = new PlayerCommand(this.addon, this.parent);

        assertEquals("likes", command.getLabel());
        assertEquals(PERMISSION_PREFIX + "likes", command.getPermission());
        assertTrue(command.isOnlyPlayer());
        assertTrue(command.getSubCommand("top").isPresent());
        assertTrue(command.getSubCommand("view").isPresent());
    }


    @Test
    void testPlayerCommandNotOnIsland()
    {
        PlayerCommand command = new PlayerCommand(this.addon, this.parent);

        assertFalse(command.canExecute(this.user, "likes", List.of()));
        verify(this.user).sendMessage(contains(Constants.ERRORS + "not-on-island"));
    }


    @Test
    void testPlayerCommandUnownedIslandNotAllowed()
    {
        when(this.island.isOwned()).thenReturn(false);
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.island));
        PlayerCommand command = new PlayerCommand(this.addon, this.parent);

        assertFalse(command.canExecute(this.user, "likes", List.of()));
        verify(this.user).sendMessage(contains(Constants.ERRORS + "island-not-owned"));
    }


    @Test
    void testPlayerCommandUnownedIslandAllowed()
    {
        this.settings.setAllowUnowned(true);
        when(this.island.isOwned()).thenReturn(false);
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.island));
        PlayerCommand command = new PlayerCommand(this.addon, this.parent);

        assertTrue(command.canExecute(this.user, "likes", List.of()));
    }


    @Test
    void testPlayerCommandOnOwnIslandOpensViewPanel()
    {
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.secondIsland));
        PlayerCommand command = new PlayerCommand(this.addon, this.parent);

        assertTrue(command.canExecute(this.user, "likes", List.of()));
        assertTrue(command.execute(this.user, "likes", List.of()));

        // The island where the player stands is used, not the primary one.
        this.viewPanel.verify(() -> LikesViewPanel.openPanel(this.addon,
            this.user,
            this.world,
            PERMISSION_PREFIX,
            this.secondIsland));
        this.managePanel.verifyNoInteractions();
    }


    @Test
    void testPlayerCommandOnOtherIslandOpensManagePanel()
    {
        when(this.secondIsland.getMemberSet()).thenReturn(ImmutableSet.of(UUID.randomUUID()));
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.secondIsland));
        PlayerCommand command = new PlayerCommand(this.addon, this.parent);

        assertTrue(command.execute(this.user, "likes", List.of()));

        this.managePanel.verify(() -> LikesManagePanel.openPanel(this.addon,
            this.user,
            this.world,
            PERMISSION_PREFIX,
            this.secondIsland));
        this.viewPanel.verifyNoInteractions();
    }


    @Test
    void testPlayerCommandExecuteNotOnIsland()
    {
        PlayerCommand command = new PlayerCommand(this.addon, this.parent);

        assertTrue(command.execute(this.user, "likes", List.of()));

        this.viewPanel.verifyNoInteractions();
        this.managePanel.verifyNoInteractions();
    }


    // ---------------------------------------------------------------------
    // Section: View command
    // ---------------------------------------------------------------------


    @Test
    void testViewCommandSetup()
    {
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertEquals("view", command.getLabel());
        assertEquals(PERMISSION_PREFIX + "likes.view", command.getPermission());
        assertTrue(command.isOnlyPlayer());
    }


    @Test
    void testViewCommandPrimaryIsland()
    {
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertTrue(command.canExecute(this.user, "view", List.of()));
        assertTrue(command.execute(this.user, "view", List.of()));

        this.viewPanel.verify(() -> LikesViewPanel.openPanel(this.addon,
            this.user,
            this.world,
            PERMISSION_PREFIX,
            this.island));
    }


    @Test
    void testViewCommandOwnIslandWherePlayerStands()
    {
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.secondIsland));
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertTrue(command.canExecute(this.user, "view", List.of()));
        assertTrue(command.execute(this.user, "view", List.of()));

        this.viewPanel.verify(() -> LikesViewPanel.openPanel(this.addon,
            this.user,
            this.world,
            PERMISSION_PREFIX,
            this.secondIsland));
    }


    @Test
    void testViewCommandNoIsland()
    {
        when(this.im.getIsland(this.world, this.uuid)).thenReturn(null);
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertFalse(command.canExecute(this.user, "view", List.of()));
        verify(this.user).sendMessage(contains(Constants.ERRORS + "not-on-island"));
    }


    @Test
    void testViewCommandOthersNoPermission()
    {
        when(this.user.hasPermission(anyString())).thenReturn(false);
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertFalse(command.canExecute(this.user, "view", List.of("target")));
        verify(this.user).sendMessage(contains("general.errors.no-permission"));
        verify(this.playersManager, never()).getUUID(anyString());
    }


    @Test
    void testViewCommandUnknownPlayer()
    {
        when(this.playersManager.getUUID("target")).thenReturn(null);
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertFalse(command.canExecute(this.user, "view", List.of("target")));
        verify(this.user).sendMessage(contains("general.errors.unknown-player"));
    }


    @Test
    void testViewCommandOtherPlayerPrimaryIsland()
    {
        Island targetIsland = mock(Island.class);
        when(this.playersManager.getUUID("target")).thenReturn(this.targetUuid);
        when(this.im.getIsland(this.world, this.targetUuid)).thenReturn(targetIsland);
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertTrue(command.canExecute(this.user, "view", List.of("target")));
        assertTrue(command.execute(this.user, "view", List.of("target")));

        this.viewPanel.verify(() -> LikesViewPanel.openPanel(this.addon,
            this.user,
            this.world,
            PERMISSION_PREFIX,
            targetIsland));
    }


    @Test
    void testViewCommandOtherPlayerIslandWhereViewerStands()
    {
        // Target has two islands, and viewer stands on the non-primary one.
        Island primary = mock(Island.class);
        Island visited = mock(Island.class);
        when(visited.getWorld()).thenReturn(this.world);
        when(visited.getMemberSet()).thenReturn(ImmutableSet.of(this.targetUuid));
        when(this.playersManager.getUUID("target")).thenReturn(this.targetUuid);
        when(this.im.getIsland(this.world, this.targetUuid)).thenReturn(primary);
        when(this.im.getIslandAt(any(Location.class))).thenReturn(Optional.of(visited));
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertTrue(command.canExecute(this.user, "view", List.of("target")));
        assertTrue(command.execute(this.user, "view", List.of("target")));

        this.viewPanel.verify(() -> LikesViewPanel.openPanel(this.addon,
            this.user,
            this.world,
            PERMISSION_PREFIX,
            visited));
    }


    @Test
    void testViewCommandOtherPlayerNoIsland()
    {
        when(this.playersManager.getUUID("target")).thenReturn(this.targetUuid);
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertFalse(command.canExecute(this.user, "view", List.of("target")));
    }


    @Test
    void testViewCommandTabCompleteNeedsLetter()
    {
        PlayerViewCommand command = new PlayerViewCommand(this.addon, this.parent);

        assertTrue(command.tabComplete(this.user, "view", List.of()).isEmpty());
    }


    // ---------------------------------------------------------------------
    // Section: Top command
    // ---------------------------------------------------------------------


    private void verifyTop(List<String> args, VIEW_MODE expected)
    {
        PlayerTopCommand command = new PlayerTopCommand(this.addon, this.parent);

        assertTrue(command.canExecute(this.user, "top", args));
        assertTrue(command.execute(this.user, "top", args));

        this.topPanel.verify(() -> TopLikesPanel.openPanel(this.addon,
            this.user,
            this.world,
            PERMISSION_PREFIX,
            expected));
    }


    @Test
    void testTopCommandSetup()
    {
        PlayerTopCommand command = new PlayerTopCommand(this.addon, this.parent);

        assertEquals("top", command.getLabel());
        assertEquals(PERMISSION_PREFIX + "likes.top", command.getPermission());
    }


    @Test
    void testTopCommandLikesMode()
    {
        this.settings.setMode(LikeMode.LIKES);
        this.verifyTop(List.of("dislikes"), VIEW_MODE.LIKES);
    }


    @Test
    void testTopCommandStarsMode()
    {
        this.settings.setMode(LikeMode.STARS);
        this.verifyTop(List.of(), VIEW_MODE.STARS);
    }


    @Test
    void testTopCommandLikesDislikesDefault()
    {
        this.settings.setMode(LikeMode.LIKES_DISLIKES);
        this.verifyTop(List.of(), VIEW_MODE.LIKES);
    }


    @Test
    void testTopCommandLikesDislikesByDislikes()
    {
        this.settings.setMode(LikeMode.LIKES_DISLIKES);
        this.verifyTop(List.of("dislikes"), VIEW_MODE.DISLIKES);
    }


    @Test
    void testTopCommandLikesDislikesByRank()
    {
        this.settings.setMode(LikeMode.LIKES_DISLIKES);
        this.verifyTop(List.of("rank"), VIEW_MODE.RANK);
    }


    @Test
    void testTopCommandLikesDislikesInvalidTypes()
    {
        this.settings.setMode(LikeMode.LIKES_DISLIKES);
        this.verifyTop(List.of("stars"), VIEW_MODE.LIKES);
    }


    @Test
    void testTopCommandLikesDislikesUnknownType()
    {
        this.settings.setMode(LikeMode.LIKES_DISLIKES);
        this.verifyTop(List.of("unknown"), VIEW_MODE.LIKES);
    }


    @Test
    void testTopCommandTabComplete()
    {
        this.settings.setMode(LikeMode.LIKES_DISLIKES);
        PlayerTopCommand command = new PlayerTopCommand(this.addon, this.parent);

        assertEquals(Optional.of(List.of("likes", "dislikes", "rank")),
            command.tabComplete(this.user, "top", List.of("")));
        assertEquals(Optional.of(List.of("dislikes")), command.tabComplete(this.user, "top", List.of("d")));

        this.settings.setMode(LikeMode.STARS);
        assertEquals(Optional.of(List.of()), command.tabComplete(this.user, "top", List.of("")));

        this.settings.setMode(LikeMode.LIKES);
        assertEquals(Optional.of(List.of()), command.tabComplete(this.user, "top", List.of("")));
    }


    // ---------------------------------------------------------------------
    // Section: Admin commands
    // ---------------------------------------------------------------------


    @Test
    void testAdminCommand()
    {
        AdminCommand command = new AdminCommand(this.addon, this.parent);

        assertEquals("likes", command.getLabel());
        assertEquals(PERMISSION_PREFIX + "likes.admin", command.getPermission());
        assertTrue(command.isOnlyPlayer());
        assertTrue(command.getSubCommand("view").isPresent());
        assertTrue(command.getSubCommand("settings").isPresent());
        assertTrue(command.canExecute(this.user, "likes", List.of()));
        assertTrue(command.execute(this.user, "likes", List.of()));

        this.adminPanel.verify(() -> AdminPanel.openPanel(eq(this.addon),
            eq(this.user),
            eq(this.world),
            eq(PERMISSION_PREFIX)));
    }


    @Test
    void testEditSettingsCommand()
    {
        EditSettingsCommand command = new EditSettingsCommand(this.addon, this.parent);

        assertEquals("settings", command.getLabel());
        assertEquals(PERMISSION_PREFIX + "likes.admin.settings", command.getPermission());
        assertTrue(command.isOnlyPlayer());
        assertTrue(command.canExecute(this.user, "settings", List.of()));
        assertTrue(command.execute(this.user, "settings", List.of()));

        this.settingsPanel.verify(() -> EditSettingsPanel.openPanel(eq(this.addon),
            eq(this.user),
            eq(this.world),
            eq(PERMISSION_PREFIX)));
    }
}
