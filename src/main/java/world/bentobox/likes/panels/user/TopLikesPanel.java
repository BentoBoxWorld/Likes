///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.panels.user;


import com.google.common.collect.ImmutableSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.config.Settings.VIEW_MODE;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This panel opens top likes panel
 */
public class TopLikesPanel
{
// ---------------------------------------------------------------------
// Section: Internal Constructor
// ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param addon Likes object.
     * @param user User who opens Panel.
     * @param world World where gui is opened
     * @param permissionPrefix Permission Prefix
     * @param mode Top Ten list.
     */
    private TopLikesPanel(LikesAddon addon, User user, World world, String permissionPrefix, VIEW_MODE mode)
    {
        this.addon = addon;
        this.user = user;
        this.world = world;

        /*
         * Permission prefix
         */
        String permissionPrefix1 = permissionPrefix;

        this.iconPermission = permissionPrefix1 + "likes.icon";
        this.viewMode = mode;

        this.topPlayerList = new ArrayList<>(10);
    }


    /**
     * Build method manages current panel opening. It uses BentoBox PanelAPI that is easy to use and users can get nice
     * panels.
     */
    private void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            // Each panel must have panel name.
                name(this.user.getTranslation(Constants.TITLE + "top",
                "[type]", this.user.getTranslation(Constants.TYPES + this.viewMode.name().toLowerCase()))).
            // Each panel must have target user who opens it.
                user(this.user);

        // Clear list each build time.
        this.topPlayerList.clear();
        Material mainMaterial;

        switch (this.viewMode)
        {
            case LIKES:
                this.topPlayerList.addAll(this.addon.getAddonManager().getTopByLikes(this.world));
                mainMaterial = Material.GREEN_STAINED_GLASS_PANE;
                break;
            case DISLIKES:
                this.topPlayerList.addAll(this.addon.getAddonManager().getTopByDislikes(this.world));
                mainMaterial = Material.RED_STAINED_GLASS_PANE;
                break;
            case RANK:
                this.topPlayerList.addAll(this.addon.getAddonManager().getTopByRank(this.world));
                mainMaterial = Material.MAGENTA_STAINED_GLASS_PANE;
                break;
            case STARS:
                this.topPlayerList.addAll(this.addon.getAddonManager().getTopByStars(this.world));
                mainMaterial = Material.BLUE_STAINED_GLASS_PANE;
                break;
            default:
                // This should never happen!
                this.user.closeInventory();
                return;
        }

        final int topPlayerCount = this.topPlayerList.size();
        int rowCount;

        // Fill only rows that can be used.

        if (topPlayerCount == 0)
        {
            this.user.sendMessage(this.user.getTranslation(Constants.ERRORS + "top-is-empty"));
            this.user.closeInventory();
            return;
        }
        else if (topPlayerCount == 1)
        {
            rowCount = 3;
        }
        else if (topPlayerCount < 4)
        {
            rowCount = 4;
        }
        else if (topPlayerCount < 7)
        {
            rowCount = 5;
        }
        else
        {
            rowCount = 6;
        }

        GuiUtils.fillBorder(panelBuilder, rowCount, mainMaterial);

        if (this.addon.getSettings().getMode().equals(Settings.LikeMode.LIKES_DISLIKES))
        {
            panelBuilder.item(8, this.createViewModeButton());
        }

        this.populatePlayerButtons(panelBuilder);

        // At the end we just call build method that creates and opens panel.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method creates and returns button that allows to switch between likes/dislikes/rank tops.
     *
     * @return PanelItem object.
     */
    private PanelItem createViewModeButton()
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        // To get button name in different languages we can use user object to get correct
        // translation string.
        builder.name(this.user.getTranslation(Constants.BUTTON + "view-mode"));

        List<String> description = new ArrayList<>(5);
        description.add(this.user.getTranslation(Constants.DESCRIPTION + "view-mode"));

        List<VIEW_MODE> values = new ArrayList<>(3);
        values.add(VIEW_MODE.LIKES);
        values.add(VIEW_MODE.DISLIKES);
        values.add(VIEW_MODE.RANK);

        values.stream().map(value -> (this.viewMode.equals(value) ? "&2" : "&c") +
            this.user.getTranslation(Constants.DESCRIPTION + "mode",
                "[type]", this.user.getTranslation(Constants.TYPES + value.name().toLowerCase()))).
            forEach(description::add);

        switch (this.viewMode)
        {
            case LIKES:
                builder.icon(Material.DIAMOND);
                break;
            case DISLIKES:
                builder.icon(Material.IRON_INGOT);
                break;
            case RANK:
                builder.icon(Material.GOLD_INGOT);
                break;
        }

        // We can modify PanelItem icon.

        // And even add lore to it.
        // We can do the same as in button name, to change its lore in lang file without
        // changing it in code.
        builder.description(GuiUtils.stringSplit(description, 999));

        // Click handler allows to define action what will happen when player clicks on
        // this PanelItem.
        builder.clickHandler((panel, user, clickType, slot) -> {
            if (clickType.isRightClick())
            {
                this.viewMode = Utils.getPreviousValue(VIEW_MODE.values(), this.viewMode);
            }
            else
            {
                this.viewMode = Utils.getNextValue(VIEW_MODE.values(), this.viewMode);
            }

            // Rebuild just this icon
            this.build();

            return true;
        });

        // At the end we build our button.
        return builder.build();
    }


    /**
     * This method populates panel with player tops.
     *
     * @param panelBuilder PanelBuilder that must be populated.
     */
    private void populatePlayerButtons(PanelBuilder panelBuilder)
    {
        // Assuming that top 10 always will contain 10 elements.

        for (int index = 0, size = this.topPlayerList.size(); index < size; index++)
        {
            panelBuilder.item(PLACEMENTS[index], this.createPlayerButton(this.topPlayerList.get(index), index + 1));
        }
    }


    /**
     * This method creates player icon with warp functionality.
     *
     * @param likesObject Likes object that holds all data.
     * @param rank Rank in list
     * @return PanelItem for PanelBuilder.
     */
    private PanelItem createPlayerButton(LikesObject likesObject, int rank)
    {
        Optional<Island> island = this.addon.getIslands().getIslandById(likesObject.getUniqueId());

        Material icon;
        String name;
        List<String> description = new ArrayList<>();

        switch (this.viewMode)
        {
            case LIKES:
                description.add(this.user
                    .getTranslation(Constants.DESCRIPTION + "top-value." + this.viewMode.name().toLowerCase(),
                        "[rank]", rank + "", "[value]", Long.toString(likesObject.getLikes())));
                break;
            case DISLIKES:
                description.add(this.user
                    .getTranslation(Constants.DESCRIPTION + "top-value." + this.viewMode.name().toLowerCase(),
                        "[rank]", rank + "", "[value]", Long.toString(likesObject.getDislikes())));
                break;
            case RANK:
                description.add(this.user
                    .getTranslation(Constants.DESCRIPTION + "top-value." + this.viewMode.name().toLowerCase(),
                        "[rank]", rank + "", "[value]", Long.toString(likesObject.getRank())));
                break;
            case STARS:
                description.add(this.user
                    .getTranslation(Constants.DESCRIPTION + "top-value." + this.viewMode.name().toLowerCase(),
                        "[rank]", rank + "", "[value]", Double.toString(likesObject.getStarsValue())));
                break;
        }

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                description.add(this.user.getTranslation(Constants.DESCRIPTION + "likes-values",
                    "[likes]", "" + likesObject.getLikes()));
                break;
            case LIKES_DISLIKES:
                description.add(this.user.getTranslation(Constants.DESCRIPTION + "likes-dislikes-values",
                    "[likes]", "" + likesObject.getLikes(),
                    "[dislikes]", "" + likesObject.getDislikes(),
                    "[rank]", "" + likesObject.getRank()));
                break;
            case STARS:
                description.add(this.user.getTranslation(Constants.DESCRIPTION + "stars-values",
                    "[stars]", "" + likesObject.getStarsValue(),
                    "[votes]", "" + likesObject.numberOfStars()));
                break;
        }

        PanelItem.ClickHandler clickHandler;

        if (island.isPresent() && island.get().getOwner() != null)
        {
            UUID ownerId = island.get().getOwner();

            icon = Material.matchMaterial(Utils.getPermissionValue(User.getInstance(ownerId),
                this.iconPermission,
                this.addon.getSettings().getDefaultIcon().name()));

            name = this.addon.getPlayers().getName(ownerId);

            ImmutableSet<UUID> members = island.get().getMemberSet();

            if (members.size() > 1)
            {
                members.forEach(uuid -> {
                    if (uuid != ownerId)
                    {
                        description.add(ChatColor.AQUA + this.addon.getPlayers().getName(uuid));
                    }
                });
            }

            // Warp Function
            if (this.addon.getWarpHook() != null &&
                this.addon.getWarpHook().getWarpSignsManager().hasWarp(this.world, ownerId))
            {
                clickHandler = (
                    (panel, user, clickType, slot) -> {
                        this.user.closeInventory();
                        this.addon.getWarpHook().getWarpSignsManager().warpPlayer(this.world, this.user, ownerId);
                        return true;
                    });
            }
            else
            {
                clickHandler = null;
            }
        }
        else
        {
            icon = this.addon.getSettings().getDefaultIcon();
            name = this.user.getTranslation(Constants.DESCRIPTION + "unknown");
            clickHandler = null;
        }

        PanelItem panelItem;

        if (icon == null || icon.equals(Material.PLAYER_HEAD))
        {
            panelItem = new PanelItemBuilder().
                name(this.user.getTranslation(Constants.BUTTON + "name", "[name]", name)).
                icon(name).
                description(GuiUtils.stringSplit(description, 999)).
                clickHandler(clickHandler).
                build();
        }
        else
        {
            panelItem = new PanelItemBuilder().
                name(this.user.getTranslation(Constants.BUTTON + "name", "[name]", name)).
                icon(icon).
                description(GuiUtils.stringSplit(description, 999)).
                clickHandler(clickHandler).
                build();
        }

        // Set rank amount
        panelItem.getItem().setAmount(rank);

        return panelItem;
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param addon Likes Addon object
     * @param user User who opens panel
     * @param world World where gui is opened
     * @param permissionPrefix Permission Prefix
     * @param mode Top Ten list.
     */
    public static void openPanel(LikesAddon addon, User user, World world, String permissionPrefix, VIEW_MODE mode)
    {
        new TopLikesPanel(addon, user, world, permissionPrefix, mode).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This variable allows to access addon object.
     */
    private final LikesAddon addon;

    /**
     * This variable holds user who opens panel. Without it panel cannot be opened.
     */
    private final User user;

    /**
     * This variable holds a world to which gui referee.
     */
    private final World world;

    /**
     * Location to icon permission.
     */
    private final String iconPermission;

    /**
     * This variable holds which top should be showed.
     */
    private VIEW_MODE viewMode;

    /**
     * This list contains all currently displayed top players.
     */
    private final List<LikesObject> topPlayerList;

// ---------------------------------------------------------------------
// Section: Instance Constants
// ---------------------------------------------------------------------

    /**
     * Array that holds all valid spots for player icons to be placed.
     */
    private static final int[] PLACEMENTS = new int[10];

    /*
     * Populate button indexes
     */
    static
    {
        PLACEMENTS[0] = 13;
        PLACEMENTS[1] = 21;
        PLACEMENTS[2] = 23;
        PLACEMENTS[3] = 29;
        PLACEMENTS[4] = 31;
        PLACEMENTS[5] = 33;
        PLACEMENTS[6] = 37;
        PLACEMENTS[7] = 39;
        PLACEMENTS[8] = 41;
        PLACEMENTS[9] = 43;
    }
}
