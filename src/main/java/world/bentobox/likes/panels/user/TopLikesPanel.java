///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
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

        this.iconPermission = permissionPrefix + "likes.icon";
        this.viewMode = mode;

        this.topPlayerList = new ArrayList<>(10);

        this.hundredsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
        this.hundredsFormat.applyPattern("###.##");
    }


    /**
     * Build method manages current panel opening. It uses BentoBox PanelAPI that is easy to use and users can get nice
     * panels.
     */
    private void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            name(this.user.getTranslation(Constants.TITLES + "top",
                Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + this.viewMode.name().toLowerCase()))).
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

        PanelUtils.fillBorder(panelBuilder, rowCount, mainMaterial);

        if (this.addon.getSettings().getMode().equals(Settings.LikeMode.LIKES_DISLIKES))
        {
            panelBuilder.item(8, this.createViewModeButton());
        }

        this.populatePlayerButtons(panelBuilder);
        this.createViewerButton(panelBuilder, rowCount * 9 - 5);

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
        builder.name(this.user.getTranslation(Constants.BUTTONS + "view_mode.name"));

        List<String> description = new ArrayList<>(5);
        description.add(this.user.getTranslation(Constants.BUTTONS + "view_mode.description"));

        VIEW_MODE[] values = new VIEW_MODE[3];
        values[0] = VIEW_MODE.LIKES;
        values[1] = VIEW_MODE.DISLIKES;
        values[2] = VIEW_MODE.RANK;

        for (VIEW_MODE mode : values)
        {
            if (this.viewMode.equals(mode))
            {
                description.add(this.user.getTranslation(Constants.BUTTONS + "view_mode.active",
                    Constants.PARAMETER_TYPE,
                    this.user.getTranslation(Constants.TYPES + mode.name().toLowerCase())));
            }
            else
            {
                description.add(this.user.getTranslation(Constants.BUTTONS + "view_mode.inactive",
                    Constants.PARAMETER_TYPE,
                    this.user.getTranslation(Constants.TYPES + mode.name().toLowerCase())));
            }
        }

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

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

        // We can modify PanelItem icon.

        // And even add lore to it.
        // We can do the same as in button name, to change its lore in lang file without
        // changing it in code.
        builder.description(description);

        // Click handler allows to define action what will happen when player clicks on
        // this PanelItem.
        builder.clickHandler((panel, user, clickType, slot) ->
        {
            if (clickType.isRightClick())
            {
                this.viewMode = Utils.getPreviousValue(values, this.viewMode);
            }
            else
            {
                this.viewMode = Utils.getNextValue(values, this.viewMode);
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
        Optional<Island> optionalIsland = this.addon.getIslands().getIslandById(likesObject.getUniqueId());

        if (!optionalIsland.isPresent())
        {
            // Return empty panel item, as something definitely went wrong.
            return PanelItem.empty();
        }

        Island island = optionalIsland.get();
        User owner = User.getInstance(island.getOwner());

        final String reference = Constants.BUTTONS + "island.";

        // Get Island Name
        String nameText;

        if (island.getName() == null || island.getName().isEmpty())
        {
            nameText = this.user.getTranslation(reference + "owners-island",
                Constants.PARAMETER_PLAYER,
                owner == null ? this.user.getTranslation(reference + "unknown") : owner.getName());
        }
        else
        {
            nameText = island.getName();
        }

        nameText = this.user.getTranslation(reference + "name",
            Constants.PARAMETER_NAME, nameText);

        // Get Owner Name
        String ownerText = this.user.getTranslation(reference + "owner",
            Constants.PARAMETER_PLAYER,
            owner == null ? this.user.getTranslation(reference + "unknown") : owner.getName());

        // Get Members Text
        String memberText;

        if (island.getMemberSet().size() > 1)
        {
            StringBuilder memberBuilder = new StringBuilder(
                this.user.getTranslationOrNothing(Constants.BUTTONS + "island.members-title"));

            for (UUID uuid : island.getMemberSet())
            {
                User user = User.getInstance(uuid);

                if (memberBuilder.length() > 0)
                {
                    memberBuilder.append("\n");
                }

                if (user != null)
                {
                    memberBuilder.append(
                        this.user.getTranslationOrNothing(Constants.BUTTONS + "island.member",
                            Constants.PARAMETER_PLAYER, user.getName()));
                }
            }

            memberText = memberBuilder.toString();
        }
        else
        {
            memberText = "";
        }

        // Get Numbers Text
        String numbersText;

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                numbersText = this.user.getTranslation(reference + "numbers_likes",
                    Constants.PARAMETER_LIKES, String.valueOf(likesObject.getLikes()));
                break;
            case LIKES_DISLIKES:
                numbersText = this.user.getTranslation(reference + "numbers_likes_dislikes",
                    Constants.PARAMETER_LIKES, String.valueOf(likesObject.getLikes()),
                    Constants.PARAMETER_DISLIKES, String.valueOf(likesObject.getDislikes()),
                    Constants.PARAMETER_RANK, String.valueOf(likesObject.getRank()));
                break;
            case STARS:
                numbersText = this.user.getTranslation(reference + "numbers_stars",
                    Constants.PARAMETER_STARS, this.hundredsFormat.format(likesObject.getStarsValue()),
                    Constants.PARAMETER_NUMBER, String.valueOf(likesObject.numberOfStars()));

                break;
            default:
                numbersText = "";
        }

        // Now combine everything.
        String descriptionText = this.user.getTranslation(reference + "description",
            Constants.PARAMETER_OWNER, ownerText,
            Constants.PARAMETER_MEMBERS, memberText,
            Constants.PARAMETER_PLACE, "",
            Constants.PARAMETER_NUMBERS, numbersText);
        List<String> description = Arrays.stream(descriptionText.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
            split("\n")).
            collect(Collectors.toList());

        // Warp Function
        PanelItem.ClickHandler clickHandler;

        if (island.getOwner() != null &&
            this.addon.getWarpHook() != null &&
            this.addon.getWarpHook().getWarpSignsManager().hasWarp(this.world, island.getOwner()))
        {
            clickHandler = (panel, user, clickType, slot) ->
            {
                this.user.closeInventory();
                this.addon.getWarpHook().getWarpSignsManager().warpPlayer(this.world, this.user, island.getOwner());
                return true;
            };

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-warp"));
        }
        else
        {
            clickHandler = null;
        }

        String permissionIcon = Utils.getPermissionValue(owner, this.iconPermission, null);

        Material material;

        if (permissionIcon != null && !permissionIcon.equals("*"))
        {
            material = Material.matchMaterial(permissionIcon);
        }
        else
        {
            material = island.getMetaData()
                    .map(map -> map.get(Constants.METADATA_ICON))
                    .map(metaDataValue -> Material.matchMaterial(metaDataValue.asString()))
                    .orElseGet(() -> this.addon.getSettings().getDefaultIcon());
        }

        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(nameText).
            description(description).
            amount(rank).
            clickHandler(clickHandler);

        if (material == null || material.equals(Material.PLAYER_HEAD))
        {
            if (owner == null)
            {
                itemBuilder.icon(Material.PLAYER_HEAD);
            }
            else
            {
                itemBuilder.icon(owner.getName());
            }
        }
        else
        {
            itemBuilder.icon(material);
        }

        return itemBuilder.build();
    }


    /**
     * Create viewer button panel item.
     *
     * @param panelBuilder the panel builder
     * @param slotIndex the slot
     */
    private void createViewerButton(PanelBuilder panelBuilder, int slotIndex)
    {
        Island island = this.addon.getIslands().getIsland(this.world, this.user);

        if (island == null)
        {
            // Player do not have an island.
            return;
        }

        LikesObject likesObject =
            this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId());

        if (likesObject == null)
        {
            // Island do not have any valid data. It is empty. Do not show.
            return;
        }

        User owner = User.getInstance(island.getOwner());

        final String reference = Constants.BUTTONS + "island.";

        // Get Island Name
        String nameText;

        if (island.getName() == null || island.getName().isEmpty())
        {
            nameText = this.user.getTranslation(reference + "owners-island",
                Constants.PARAMETER_PLAYER,
                owner == null ? this.user.getTranslation(reference + "unknown") : owner.getName());
        }
        else
        {
            nameText = island.getName();
        }

        nameText = this.user.getTranslation(reference + "name",
            Constants.PARAMETER_NAME, nameText);

        // Get Owner Name
        String ownerText = this.user.getTranslation(reference + "owner",
            Constants.PARAMETER_PLAYER,
            owner == null ? this.user.getTranslation(reference + "unknown") : owner.getName());

        // Get Members Text
        String memberText;

        if (island.getMemberSet().size() > 1)
        {
            StringBuilder memberBuilder = new StringBuilder(
                this.user.getTranslationOrNothing(Constants.BUTTONS + "island.members-title"));

            for (UUID uuid : island.getMemberSet())
            {
                User user = User.getInstance(uuid);

                if (memberBuilder.length() > 0)
                {
                    memberBuilder.append("\n");
                }

                if (user != null)
                {
                    memberBuilder.append(
                        this.user.getTranslationOrNothing(Constants.BUTTONS + "island.member",
                            Constants.PARAMETER_PLAYER, user.getName()));
                }
            }

            memberText = memberBuilder.toString();
        }
        else
        {
            memberText = "";
        }

        // Get Place Text
        String placeText = "";

        int rank = -1;

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                rank = this.addon.getAddonManager().getIslandRankByLikes(this.world, likesObject);

                if (rank != -1)
                {
                    placeText = this.user.getTranslation(reference + "place",
                        Constants.PARAMETER_NUMBER, String.valueOf(rank),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes"));
                }

                break;
            case LIKES_DISLIKES:

                int byLikes = this.addon.getAddonManager().getIslandRankByLikes(this.world, likesObject);
                int byDislikes = this.addon.getAddonManager().getIslandRankByDislikes(this.world, likesObject);
                int byRank = this.addon.getAddonManager().getIslandRankByRank(this.world, likesObject);

                switch (this.viewMode)
                {
                    case LIKES:
                        rank = byLikes;
                        break;
                    case DISLIKES:
                        rank = byDislikes;
                        break;
                    case RANK:
                        rank = byRank;
                        break;
                }

                if (byLikes != -1)
                {
                    placeText = this.user.getTranslation(reference + "place",
                        Constants.PARAMETER_NUMBER, String.valueOf(byLikes),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes"));
                }

                if (byDislikes != -1)
                {
                    placeText += "\n" + this.user.getTranslation(reference + "place",
                        Constants.PARAMETER_NUMBER, String.valueOf(byDislikes),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "dislikes"));
                }

                if (byRank != -1)
                {
                    placeText += "\n" + this.user.getTranslation(reference + "place",
                        Constants.PARAMETER_NUMBER, String.valueOf(byRank),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "rank"));
                }
                break;
            case STARS:
                rank = this.addon.getAddonManager().getIslandRankByStars(this.world, likesObject);

                if (rank != -1)
                {
                    placeText = this.user.getTranslation(reference + "place",
                        Constants.PARAMETER_NUMBER, String.valueOf(rank),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "stars"));
                }
                break;
        }

        // Get Numbers Text
        String numbersText;

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                numbersText = this.user.getTranslation(reference + "numbers_likes",
                    Constants.PARAMETER_LIKES, String.valueOf(likesObject.getLikes()));
                break;
            case LIKES_DISLIKES:
                numbersText = this.user.getTranslation(reference + "numbers_likes_dislikes",
                    Constants.PARAMETER_LIKES, String.valueOf(likesObject.getLikes()),
                    Constants.PARAMETER_DISLIKES, String.valueOf(likesObject.getDislikes()),
                    Constants.PARAMETER_RANK, String.valueOf(likesObject.getRank()));
                break;
            case STARS:
                numbersText = this.user.getTranslation(reference + "numbers_stars",
                    Constants.PARAMETER_STARS, this.hundredsFormat.format(likesObject.getStarsValue()),
                    Constants.PARAMETER_NUMBER, String.valueOf(likesObject.numberOfStars()));
                break;
            default:
                numbersText = "";
        }

        // Now combine everything.
        String descriptionText = this.user.getTranslation(reference + "description",
            Constants.PARAMETER_OWNER, ownerText,
            Constants.PARAMETER_MEMBERS, memberText,
            Constants.PARAMETER_PLACE, placeText,
            Constants.PARAMETER_NUMBERS, numbersText);
        List<String> description = Arrays.stream(descriptionText.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
            split("\n")).
            collect(Collectors.toList());

        // Detailed view Function
        PanelItem.ClickHandler clickHandler = (panel, user, clickType, slot) ->
        {
            LikesViewPanel.openPanel(this.addon, this.user, this.world, this.iconPermission, island);
            return true;
        };

        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(nameText).
            description(description).
            amount(rank < 1 || rank > 64 ? 1 : rank).
            clickHandler(clickHandler);

        String permissionIcon = Utils.getPermissionValue(owner, this.iconPermission, null);

        Material material;

        if (permissionIcon != null && !permissionIcon.equals("*"))
        {
            material = Material.matchMaterial(permissionIcon);
        }
        else
        {
            material = island.getMetaData()
                    .map(map -> map.get(Constants.METADATA_ICON))
                    .map(metaDataValue -> Material.matchMaterial(metaDataValue.asString()))
                    .orElseGet(() -> this.addon.getSettings().getDefaultIcon());
        }

        if (material == null || material.equals(Material.PLAYER_HEAD))
        {
            if (owner == null)
            {
                itemBuilder.icon(Material.PLAYER_HEAD);
            }
            else
            {
                itemBuilder.icon(owner.getName());
            }
        }
        else
        {
            itemBuilder.icon(material);
        }

        // At last add item to the panel builder
        panelBuilder.item(slotIndex, itemBuilder.build());
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

    /**
     * Stores decimal format object for two digit after separator.
     */
    private final DecimalFormat hundredsFormat;

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
