package world.bentobox.likes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.likes.config.Settings.LikeMode;
import world.bentobox.likes.listeners.ResetListener;
import world.bentobox.likes.placeholders.LikesAddonPlaceholderType;
import world.bentobox.visit.VisitAddon;
import world.bentobox.warps.Warp;

/**
 * Tests for {@link LikesAddon}.
 */
class LikesAddonTest extends CommonTestSetup
{
    private static final List<String> RESOURCES = List.of("config.yml",
        "panels/view_panels.yml",
        "panels/top_panel.yml",
        "panels/manage_panels.yml");

    @Mock
    private AddonsManager am;

    @Mock
    private GameModeAddon gameModeAddon;

    private MockedStatic<DatabaseSetup> mockDb;

    private LikesAddon addon;


    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        // Database
        AbstractDatabaseHandler<Object> handler = mock(AbstractDatabaseHandler.class);
        this.mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        this.mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(handler);
        when(handler.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        // Managers
        CommandsManager commandsManager = mock(CommandsManager.class);
        when(this.plugin.getCommandsManager()).thenReturn(commandsManager);
        when(this.plugin.getAddonsManager()).thenReturn(this.am);
        when(this.plugin.getFlagsManager()).thenReturn(this.fm);
        when(this.fm.getFlags()).thenReturn(Collections.emptyList());
        when(this.plugin.isEnabled()).thenReturn(true);
        when(this.plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(this.plugin.getVault()).thenReturn(Optional.empty());

        // GameMode without commands
        AddonDescription gameModeDescription = new AddonDescription.Builder("main", "BSkyBlock", "1.0").build();
        when(this.gameModeAddon.getDescription()).thenReturn(gameModeDescription);
        when(this.gameModeAddon.getPlayerCommand()).thenReturn(Optional.empty());
        when(this.gameModeAddon.getAdminCommand()).thenReturn(Optional.empty());
        when(this.am.getGameModeAddons()).thenReturn(List.of(this.gameModeAddon));
        when(this.am.getAddonByName(anyString())).thenReturn(Optional.empty());

        // Addon with the real resources packed into the jar.
        this.addon = new LikesAddon();
        File jarFile = new File("addon.jar");

        try (JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile)))
        {
            for (String resource : RESOURCES)
            {
                jar.putNextEntry(new JarEntry(resource));
                jar.write(Files.readAllBytes(Path.of("src/main/resources", resource)));
                jar.closeEntry();
            }
        }

        this.addon.setDataFolder(new File("addons/Likes"));
        this.addon.setFile(jarFile);
        this.addon.setDescription(new AddonDescription.Builder("bentobox", "Likes", "1.0.0").
            description("test").authors("BONNe").build());
    }


    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        this.mockDb.closeOnDemand();
        super.tearDown();
        Files.deleteIfExists(Path.of("addon.jar"));
        deleteAll(new File("addons"));
    }


    private void loadAndEnable()
    {
        this.addon.onLoad();
        this.addon.setState(State.LOADED);
        this.addon.onEnable();
    }


    // ---------------------------------------------------------------------
    // Section: Loading
    // ---------------------------------------------------------------------


    @Test
    void testGetSettingsNullBeforeLoad()
    {
        assertNull(this.addon.getSettings());
        assertNull(this.addon.getAddonManager());
    }


    @Test
    void testOnLoad()
    {
        this.addon.onLoad();

        assertNotNull(this.addon.getSettings());
        RESOURCES.forEach(resource ->
            assertTrue(new File("addons/Likes", resource).exists(), resource + " is not saved"));
    }


    @Test
    void testOnLoadSettingsDefaults()
    {
        this.addon.onLoad();

        assertEquals(Material.PLAYER_HEAD, this.addon.getSettings().getDefaultIcon());
        assertEquals(LikeMode.LIKES_DISLIKES, this.addon.getSettings().getMode());
        assertTrue(this.addon.getSettings().isInformPlayers());
        assertTrue(this.addon.getSettings().isResetLikes());
        assertTrue(this.addon.getSettings().isAllowUnowned());
        assertFalse(this.addon.getSettings().isLogHistory());
        assertEquals(0, this.addon.getSettings().getLikeAddCost());
        assertTrue(this.addon.getSettings().getDisabledGameModes().isEmpty());
    }


    @Test
    void testOnReload()
    {
        this.addon.onLoad();
        this.addon.onReload();

        assertNotNull(this.addon.getSettings());
    }


    @Test
    void testSaveSettings()
    {
        // Nothing to save before load.
        this.addon.saveSettings();
        assertFalse(new File("addons/Likes", "config.yml").exists());

        this.addon.onLoad();
        this.addon.getSettings().setMode(LikeMode.STARS);

        assertDoesNotThrow(() -> this.addon.saveSettings());
        assertEquals(LikeMode.STARS, this.addon.getSettings().getMode());
    }


    // ---------------------------------------------------------------------
    // Section: Enabling
    // ---------------------------------------------------------------------


    @Test
    void testOnEnable()
    {
        this.loadAndEnable();

        assertNotNull(this.addon.getAddonManager());
        assertEquals(State.LOADED, this.addon.getState());
        // All placeholders are registered for the game mode.
        verify(this.phm, times(LikesAddonPlaceholderType.values().length)).
            registerPlaceholder(eq(this.gameModeAddon), anyString(), any());
        verify(this.am).registerListener(eq(this.addon), any(ResetListener.class));
        // Request handlers answer to their labels.
        assertEquals(Map.of(), this.addon.request("island-likes", new HashMap<>()));
        assertEquals(Map.of(), this.addon.request("top-ten-likes", new HashMap<>()));
        assertNull(this.addon.request("unknown", new HashMap<>()));
    }


    @Test
    void testOnEnableSkipsRegisteredPlaceholders()
    {
        when(this.phm.isPlaceholder(any(), anyString())).thenReturn(true);

        this.loadAndEnable();

        verify(this.phm, never()).registerPlaceholder(any(GameModeAddon.class), anyString(), any());
    }


    @Test
    void testOnEnablePluginDisabled()
    {
        when(this.plugin.isEnabled()).thenReturn(false);

        this.loadAndEnable();

        assertEquals(State.DISABLED, this.addon.getState());
        assertNull(this.addon.getAddonManager());
    }


    @Test
    void testOnEnableAddonDisabled()
    {
        this.addon.onLoad();
        this.addon.setState(State.DISABLED);
        this.addon.onEnable();

        assertNull(this.addon.getAddonManager());
    }


    @Test
    void testOnEnableNoGameModes()
    {
        when(this.am.getGameModeAddons()).thenReturn(List.of());

        this.loadAndEnable();

        assertEquals(State.DISABLED, this.addon.getState());
        verify(this.am, never()).registerListener(any(), any());
    }


    @Test
    void testOnEnableDisabledGameMode()
    {
        this.addon.onLoad();
        this.addon.getSettings().setDisabledGameModes(Set.of("BSkyBlock"));
        this.addon.setState(State.LOADED);
        this.addon.onEnable();

        assertEquals(State.DISABLED, this.addon.getState());
        verify(this.phm, never()).registerPlaceholder(any(GameModeAddon.class), anyString(), any());
    }


    @Test
    void testOnDisable()
    {
        this.loadAndEnable();
        this.addon.onDisable();

        assertNotNull(this.addon.getSettings());
    }


    // ---------------------------------------------------------------------
    // Section: Hooks
    // ---------------------------------------------------------------------


    @Test
    void testAllLoadedWithoutHooks()
    {
        this.loadAndEnable();
        this.addon.allLoaded();

        assertNull(this.addon.getBankHook());
        assertNull(this.addon.getVisitHook());
        assertNull(this.addon.getWarpHook());
        assertNull(this.addon.getVaultHook());
        assertFalse(this.addon.isEconomyProvided());
    }


    @Test
    void testAllLoadedWithHooks()
    {
        Bank bank = mock(Bank.class);
        VisitAddon visit = mock(VisitAddon.class);
        Warp warp = mock(Warp.class);
        VaultHook vault = mock(VaultHook.class);
        when(vault.hook()).thenReturn(true);
        when(this.am.getAddonByName("Bank")).thenReturn(Optional.of(bank));
        when(this.am.getAddonByName("Visit")).thenReturn(Optional.of(visit));
        when(this.am.getAddonByName("Warps")).thenReturn(Optional.of(warp));
        when(this.plugin.getVault()).thenReturn(Optional.of(vault));

        this.loadAndEnable();
        this.addon.allLoaded();

        assertSame(bank, this.addon.getBankHook());
        assertSame(visit, this.addon.getVisitHook());
        assertSame(warp, this.addon.getWarpHook());
        assertSame(vault, this.addon.getVaultHook());
        assertTrue(this.addon.isEconomyProvided());
    }


    @Test
    void testAllLoadedVaultWithoutEconomy()
    {
        VaultHook vault = mock(VaultHook.class);
        when(vault.hook()).thenReturn(false);
        when(this.plugin.getVault()).thenReturn(Optional.of(vault));

        this.loadAndEnable();
        this.addon.allLoaded();

        assertSame(vault, this.addon.getVaultHook());
        assertFalse(this.addon.isEconomyProvided());
    }


    @Test
    void testAllLoadedNotHooked()
    {
        when(this.am.getGameModeAddons()).thenReturn(List.of());
        when(this.am.getAddonByName("Bank")).thenReturn(Optional.of(mock(Bank.class)));

        this.loadAndEnable();
        this.addon.allLoaded();

        assertNull(this.addon.getBankHook());
    }
}
