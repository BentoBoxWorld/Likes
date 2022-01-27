///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes;


import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.NonNull;
import java.util.Arrays;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.likes.commands.admin.AdminCommand;
import world.bentobox.likes.commands.user.PlayerCommand;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.listeners.ResetListener;
import world.bentobox.likes.managers.LikesManager;
import world.bentobox.likes.placeholders.LikesAddonPlaceholder;
import world.bentobox.likes.placeholders.LikesAddonPlaceholderType;
import world.bentobox.likes.requests.LikesRequestHandler;
import world.bentobox.likes.requests.TopTenRequestHandler;
import world.bentobox.visit.VisitAddon;
import world.bentobox.warps.Warp;


/**
 * This is main Addon class. It allows to load it into BentoBox hierarchy.
 */
public class LikesAddon extends Addon
{
    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * Executes code when loading the addon. This is called before {@link #onEnable()}. This <b>must</b> be used to
     * setup configuration, worlds and commands.
     */
    @Override
    public void onLoad()
    {
        super.onLoad();

        // in most of addons, onLoad we want to store default configuration if it does not
        // exist and load it.

        // Storing default configuration is simple. But be aware, you need
        // @StoreAt(filename="config.yml", path="addons/Likes") in header of your Config file.
        this.saveDefaultConfig();

        this.settings = new Config<>(this, Settings.class).loadConfigObject();

        if (this.settings == null)
        {
            // If we failed to load Settings then we should not enable addon.
            // We can log error and set state to DISABLED.

            this.logError("Likes settings could not load! Addon disabled.");
            this.setState(State.DISABLED);
        }

        this.saveResource("panels/view_likes.yml", false);
        this.saveResource("panels/view_likes_dislikes.yml", false);
        this.saveResource("panels/view_stars.yml", false);
        this.saveResource("panels/top_panel.yml", false);
    }


    /**
     * Executes code when enabling the addon. This is called after {@link #onLoad()}. <br/> Note that commands and
     * worlds registration <b>must</b> be done in {@link #onLoad()}, if need be. Failure to do so <b>will</b> result in
     * issues such as tab-completion not working for commands.
     */
    @Override
    public void onEnable()
    {
        // Check if it is enabled - it might be loaded, but not enabled.

        if (this.getPlugin() == null || !this.getPlugin().isEnabled())
        {
            Bukkit.getLogger().severe("BentoBox is not available or disabled!");
            this.setState(State.DISABLED);
            return;
        }

        // Check if addon is not disabled before.

        if (this.getState().equals(State.DISABLED))
        {
            Bukkit.getLogger().severe("Likes Addon is not available or disabled!");
            return;
        }

        // Initialize data manager
        this.manager = new LikesManager(this);

        // If your addon wants to hook into other GameModes, f.e. use flags, then you should
        // hook these flags into each GameMode.

        // Fortunately BentoBox provides ability to a list of all loaded GameModes.

        this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
            // In Settings (and config) we define DisabledGameModes, list of GameModes where
            // current Addon should not work.
            // This is where we do not hook current addon into GameMode addon.

            if (!this.settings.getDisabledGameModes().contains(gameModeAddon.getDescription().getName()))
            {
                // Each GameMode could have Player Command and Admin Command and we could
                // want to integrate our Example Command into these commands.
                // It provides ability to call command with GameMode command f.e. "/island example"

                // Of course we should check if these commands exists, as it is possible to
                // create GameMode without them.

                gameModeAddon.getPlayerCommand().ifPresent(
                    playerCommand -> new PlayerCommand(this, playerCommand));

                gameModeAddon.getAdminCommand().ifPresent(
                    adminCommand -> new AdminCommand(this, adminCommand));

                // Register all likes addon placeholders
                this.registerAddonPlaceholders(gameModeAddon);
            }
        });

        // BentoBox does not manage money, but it provides VaultHook that does it.
        // I suggest to do the same trick as with Level addon. Create local variable and
        // store if Vault is present there.

        Optional<VaultHook> vaultHook = this.getPlugin().getVault();

        // Even if Vault is installed, it does not mean that economy can be used. It is
        // necessary to check it via VaultHook#hook() method.

        if (!vaultHook.isPresent())
        {
            this.vaultHook = null;
            this.logWarning("Vault plugin not found. Economy will not work!");
        }
        else
        {
            this.vaultHook = vaultHook.get();
        }

        // Check if warps exist, so players could warp when they click on player icon.

        Optional<Addon> warps = this.getPlugin().getAddonsManager().getAddonByName("Warps");

        if (warps.isPresent())
        {
            this.warpHook = (Warp) warps.get();
        }
        else
        {
            this.warpHook = null;
            this.logWarning("Warps addon not found by Likes Addon!");
        }

        // Check if warps exist, so players could warp when they click on player icon.

        Optional<Addon> visits = this.getPlugin().getAddonsManager().getAddonByName("Visit");

        if (visits.isPresent())
        {
            this.visitHook = (VisitAddon) visits.get();
        }
        else
        {
            this.visitHook = null;
            this.logWarning("Visit addon not found by Likes Addon!");
        }

        // Register Listener
        this.registerListener(new ResetListener(this));

        // Register Request Handlers
        this.registerRequestHandler(new LikesRequestHandler(this));
        this.registerRequestHandler(new TopTenRequestHandler(this));
    }


    /**
     * Executes code when reloading the addon.
     */
    @Override
    public void onReload()
    {
        super.onReload();

        // onReload most of addons just need to reload configuration.
        // If flags, listeners and handlers were set up correctly via Addon.class then
        // they will be reloaded automatically.

        this.settings = new Config<>(this, Settings.class).loadConfigObject();

        if (this.settings == null)
        {
            // If we failed to load Settings then we should not enable addon.
            // We can log error and set state to DISABLED.

            this.logError("Likes settings could not load! Addon disabled.");
            this.setState(State.DISABLED);
        }
    }


    /**
     * Executes code when disabling the addon.
     */
    @Override
    public void onDisable()
    {
        // onDisable we would like to save exisitng settings. It is not necessary for
        // addons that does not have interface for settings editing!

        this.manager.save();
    }


    /**
     * This method saves settings file from memory.
     */
    public void saveSettings()
    {
        if (this.settings != null)
        {
            new Config<>(this, Settings.class).saveConfigObject(this.settings);
        }
    }


    /**
     * This is simple method that adds given event to plugin manager.
     *
     * @param event BentoBoxEvent that is triggered.
     */
    public void callEvent(BentoBoxEvent event)
    {
        Bukkit.getServer().getPluginManager().callEvent(event);
    }


    /**
     * Registers LikesAddon placeholders for this gameMode Addon.
     *
     * @param gameModeAddon the gameMode Addon to register the LikesAddon placeholders.
     */
    public void registerAddonPlaceholders(@NonNull GameModeAddon gameModeAddon)
    {
        final PlaceholdersManager mgr = this.getPlugin().getPlaceholdersManager();

        Arrays.stream(LikesAddonPlaceholderType.values()).
            filter(placeholder -> !mgr.isPlaceholder(gameModeAddon, placeholder.getPlaceholder())).
            forEach(placeholder -> mgr.registerPlaceholder(gameModeAddon,
                placeholder.getPlaceholder(),
                new LikesAddonPlaceholder(this, gameModeAddon, placeholder)));
    }


    // ---------------------------------------------------------------------
    // Section: Getters
    // ---------------------------------------------------------------------


    /**
     * @return economyProvided variable.
     */
    public boolean isEconomyProvided()
    {
        return this.vaultHook != null && this.vaultHook.hook();
    }


    /**
     * This getter will allow to access to VaultHook. It is written so that it could return null, if Vault is not
     * present.
     *
     * @return {@code VaultHook} if it is present, {@code null} otherwise.
     */
    public VaultHook getVaultHook()
    {
        return this.vaultHook;
    }


    /**
     * Gets visit hook.
     *
     * @return the visit hook
     */
    public VisitAddon getVisitHook()
    {
        return this.visitHook;
    }


    /**
     * Method LikesAddon#getWarpHook returns the warpHook of this object.
     *
     * @return {@code Warp} of this object, {@code null} otherwise.
     */
    public Warp getWarpHook()
    {
        return this.warpHook;
    }


    /**
     * Method LikesAddon#getSettings returns the settings of this object.
     *
     * @return the settings (type Settings) of this object.
     */
    public Settings getSettings()
    {
        return this.settings;
    }


    /**
     * Method LikesAddon#getManager returns the manager of this object.
     *
     * @return the manager (type LikesManager) of this object.
     */
    public LikesManager getAddonManager()
    {
        return this.manager;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Settings object contains
     */
    private Settings settings;

    /**
     * Likes addon manager.
     */
    private LikesManager manager;

    /**
     * Local variable that stores if vaultHook is present.
     */
    private VaultHook vaultHook;

    /**
     * Local variable that stores if warpHook is present.
     */
    private Warp warpHook;

    /**
     * Local variable that stores if visitHook is present.
     */
    private VisitAddon visitHook;
}
