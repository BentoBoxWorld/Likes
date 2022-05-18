///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.config.Settings.VIEW_MODE;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;
import world.bentobox.likes.utils.collections.IndexedTreeSet;


/**
 * This panel opens top likes panel
 */
public class TopLikesPanel extends CommonPanel
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
        super(addon, user, world, permissionPrefix);
        this.addon = addon;
        this.user = user;
        this.world = world;

        this.iconPermission = permissionPrefix + "likes.icon";
        this.viewMode = mode;

        this.hundredsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
        this.hundredsFormat.applyPattern("###.##");
    }


    /**
     * Build method manages current panel opening. It uses BentoBox PanelAPI that is easy to use and users can get nice
     * panels.
     */
    public void build()
    {
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        panelBuilder.user(this.user);
        panelBuilder.world(this.world);

        panelBuilder.template("top_panel", new File(this.addon.getDataFolder(), "panels"));

        // Set main template.
        switch (this.viewMode)
        {
            case LIKES -> this.topPlayerList = this.addon.getAddonManager().getSortedLikes(this.world);
            case DISLIKES -> this.topPlayerList = this.addon.getAddonManager().getSortedDislikes(this.world);
            case RANK -> this.topPlayerList = this.addon.getAddonManager().getSortedRank(this.world);
            case STARS -> this.topPlayerList = this.addon.getAddonManager().getSortedStars(this.world);
        }

        panelBuilder.registerTypeBuilder("VIEW", this::createViewerButton);
        panelBuilder.registerTypeBuilder("TOP", this::createPlayerButton);

        if (this.addon.getSettings().getMode().equals(Settings.LikeMode.LIKES_DISLIKES))
        {
            panelBuilder.registerTypeBuilder("RANKING_TYPE", this::createViewModeButton);
        }

        // Register unknown type builder.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method creates and returns button that allows to switch between likes/dislikes/rank tops.
     *
     * @param template the template
     * @param itemSlot the item slot
     * @return PanelItem object.
     */
    private PanelItem createViewModeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        final String reference = Constants.BUTTONS + "view_mode.";

        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            switch (this.viewMode)
            {
                case LIKES -> builder.icon(Material.DIAMOND);
                case DISLIKES -> builder.icon(Material.IRON_INGOT);
                case RANK -> builder.icon(Material.GOLD_INGOT);
            }
        }

        // Template specific title is always more important than biomesObject name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }
        else
        {
            builder.name(this.user.getTranslationOrNothing(reference + "name"));
        }

        VIEW_MODE[] values = new VIEW_MODE[3];
        values[0] = VIEW_MODE.LIKES;
        values[1] = VIEW_MODE.DISLIKES;
        values[2] = VIEW_MODE.RANK;

        if (template.description() != null && !template.description().isBlank())
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.user.getTranslation(reference + "description"));

            for (VIEW_MODE mode : values)
            {
                if (this.viewMode.equals(mode))
                {
                    builder.description(this.user.getTranslation(reference + "active",
                        Constants.PARAMETER_TYPE,
                        this.user.getTranslation(Constants.TYPES + mode.name().toLowerCase())));
                }
                else
                {
                    builder.description(this.user.getTranslation(reference + "inactive",
                        Constants.PARAMETER_TYPE,
                        this.user.getTranslation(Constants.TYPES + mode.name().toLowerCase())));
                }
            }
        }

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : template.actions())
            {
                if (clickType == action.clickType())
                {
                    if ("UP".equalsIgnoreCase(action.actionType()))
                    {
                        this.viewMode = Utils.getPreviousValue(values, this.viewMode);
                        this.build();
                    }
                    else if ("DOWN".equalsIgnoreCase(action.actionType()))
                    {
                        this.viewMode = Utils.getNextValue(values, this.viewMode);
                        this.build();
                    }
                }
            }

            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


    /**
     * This method creates player icon with warp functionality.
     *
     * @return PanelItem for PanelBuilder.
     */
    private PanelItem createPlayerButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        final String reference = Constants.BUTTONS + "island.";

        int index = (int) template.dataMap().getOrDefault("index", 0);
        Material background = Material.matchMaterial((String) template.dataMap().getOrDefault("background", ""));

        if (index < 1)
        {
            return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "empty", "[name]", String.valueOf(index))).
                icon(background).
                build();
        }

        LikesObject likesObject = this.topPlayerList.size() < index ? null : this.topPlayerList.exact(index - 1);

        if (likesObject == null)
        {
            return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "empty", "[name]", String.valueOf(index))).
                icon(background).
                amount(index).
                build();
        }

        Optional<Island> optionalIsland = this.addon.getIslands().getIslandById(likesObject.getUniqueId());

        if (optionalIsland.isEmpty())
        {
            return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "empty", "[name]", String.valueOf(index))).
                icon(background).
                amount(index).
                build();
        }

        // Get player island.
        Island island = optionalIsland.get();

        PanelItemBuilder builder = new PanelItemBuilder();

        this.populateIslandIcon(builder, template, island);        
        this.populateIslandTitle(builder, template, island);
        this.populateIslandDescription(builder, template, island, likesObject, false);

        builder.amount(index);

        // Get only possible actions, by removing all inactive ones.
        List<ItemTemplateRecord.ActionRecords> activeActions = new ArrayList<>(template.actions());

        activeActions.removeIf(action ->
        {
            switch (action.actionType().toUpperCase())
            {
                case "WARP" -> {
                    return island.getOwner() == null ||
                        this.addon.getWarpHook() == null ||
                        !this.addon.getWarpHook().getWarpSignsManager().hasWarp(this.world, island.getOwner());
                }
                case "VISIT" -> {
                    return island.getOwner() == null ||
                        this.addon.getVisitHook() == null ||
                        !this.addon.getVisitHook().getAddonManager().preprocessTeleportation(this.user, island);
                }
                default -> {
                    return false;
                }
            }
        });

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : activeActions)
            {
                if (clickType == action.clickType())
                {
                    switch (action.actionType().toUpperCase())
                    {
                        case "WARP" -> {
                            this.user.closeInventory();
                            this.addon.getWarpHook().getWarpSignsManager().warpPlayer(this.world, this.user, island.getOwner());
                        }
                        case "VISIT" -> {

                            this.addon.getPlugin().getIWM().getAddon(this.world).
                                flatMap(GameModeAddon::getPlayerCommand).ifPresent(command ->
                                {
                                    this.user.performCommand(command.getTopLabel() + " visit " + island.getOwner());
                                    this.user.closeInventory();
                                });
                        }
                    }
                }
            }

            return true;
        });

        // Collect tooltips.
        List<String> tooltips = activeActions.stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * Populate given panel item builder name with values from template and island objects.
     *
     * @param builder the builder
     * @param template the template
     * @param island the island
     */
    private void populateIslandTitle(PanelItemBuilder builder, 
        ItemTemplateRecord template, 
        Island island)
    {
        final String reference = Constants.BUTTONS + "island.";
        
        // Template specific title is always more important than custom one.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }
        else
        {
            // Get Island Name
            String nameText;

            if (island.getName() == null || island.getName().isEmpty())
            {
                if (island.isSpawn())
                {
                    nameText = this.user.getTranslation(reference + "spawn");
                }
                else
                {
                    nameText = this.user.getTranslation(reference + "owners-island",
                        Constants.PARAMETER_PLAYER,
                        island.getOwner() == null ?
                            this.user.getTranslation(reference + "unknown") :
                            this.addon.getPlayers().getName(island.getOwner()));
                }
            }
            else
            {
                nameText = island.getName();
            }

            builder.name(this.user.getTranslation(reference + "name", Constants.PARAMETER_NAME, nameText));
        }
    }


    /**
     * Populate given panel item builder icon with values from template and island objects.
     *
     * @param builder the builder
     * @param template the template
     * @param island the island
     */
    private void populateIslandIcon(PanelItemBuilder builder,
        ItemTemplateRecord template,
        Island island)
    {
        User owner = island.getOwner() == null ? null : User.getInstance(island.getOwner());
        
        // Get permission or island icon
        String permissionIcon = Utils.getPermissionValue(owner, this.iconPermission, null);

        Material material;

        if (permissionIcon != null && !permissionIcon.equals("*"))
        {
            material = Material.matchMaterial(permissionIcon);
        }
        else
        {
            material = island.getMetaData().
                map(map -> map.get(Constants.METADATA_ICON)).
                map(metaDataValue -> Material.matchMaterial(metaDataValue.asString())).
                orElseGet(() -> this.addon.getSettings().getDefaultIcon());
        }

        if (material != null)
        {
            if (owner == null || !material.equals(Material.PLAYER_HEAD))
            {
                builder.icon(material);
            }
            else
            {
                builder.icon(owner.getName());
            }
        }
        else if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else if (owner != null)
        {
            builder.icon(owner.getName());
        }
        else
        {
            builder.icon(Material.PLAYER_HEAD);
        }
    }


    /**
     * Populate given panel item builder description with values from template and island objects.
     *
     * @param builder the builder
     * @param template the template
     * @param island the island
     * @param likesObject the likes object
     * @param generatePlaceText the value that force to generate place text
     */
    private void populateIslandDescription(PanelItemBuilder builder, 
        ItemTemplateRecord template,
        Island island, 
        LikesObject likesObject,
        boolean generatePlaceText)
    {
        final String reference = Constants.BUTTONS + "island.";

        // Template specific description is always more important than custom one.
        if (template.description() != null && !template.description().isBlank())
        {
            builder.name(this.user.getTranslationOrNothing(template.description()));
        }
        else
        {
            // Get Owner Name
            String ownerText;

            if (island.isSpawn())
            {
                ownerText = "";
            }
            else
            {
                ownerText = this.user.getTranslation(reference + "owner",
                    Constants.PARAMETER_PLAYER,
                    island.getOwner() == null ?
                        this.user.getTranslation(reference + "unknown") :
                        this.addon.getPlayers().getName(island.getOwner()));
            }

            // Get Members Text
            String memberText;

            if (island.getMemberSet().size() > 1)
            {
                StringBuilder memberBuilder = new StringBuilder(
                    this.user.getTranslationOrNothing(reference + "members-title"));

                for (UUID uuid : island.getMemberSet())
                {
                    String name = this.addon.getPlayers().getName(uuid);

                    if (memberBuilder.length() > 0)
                    {
                        memberBuilder.append("\n");
                    }

                    memberBuilder.append(
                        this.user.getTranslationOrNothing(reference + "member",
                            Constants.PARAMETER_PLAYER, name));
                }

                memberText = memberBuilder.toString();
            }
            else
            {
                memberText = "";
            }

            // Get Numbers Text
            String numbersText = switch (this.addon.getSettings().getMode()) {
                case LIKES -> this.user.getTranslation(reference + "numbers_likes",
                    Constants.PARAMETER_LIKES, String.valueOf(likesObject.getLikes()));
                case LIKES_DISLIKES -> this.user.getTranslation(reference + "numbers_likes_dislikes",
                    Constants.PARAMETER_LIKES, String.valueOf(likesObject.getLikes()),
                    Constants.PARAMETER_DISLIKES, String.valueOf(likesObject.getDislikes()),
                    Constants.PARAMETER_RANK, String.valueOf(likesObject.getRank()));
                case STARS -> this.user.getTranslation(reference + "numbers_stars",
                    Constants.PARAMETER_STARS, this.hundredsFormat.format(likesObject.getStarsValue()),
                    Constants.PARAMETER_NUMBER, String.valueOf(likesObject.numberOfStars()));
            };

            String placeText = "";

            // Generate place text
            if (generatePlaceText)
            {
                switch (this.addon.getSettings().getMode())
                {
                    case LIKES -> {
                        int rank = this.addon.getAddonManager().getIslandRankByLikes(this.world, likesObject);
                        
                        if (rank != -1)
                        {
                            placeText = this.user.getTranslation(reference + "place",
                                Constants.PARAMETER_NUMBER, String.valueOf(rank),
                                Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes"));
                        }
                    }
                    case LIKES_DISLIKES -> {
                        int byLikes = this.addon.getAddonManager().getIslandRankByLikes(this.world, likesObject);
                        int byDislikes = this.addon.getAddonManager().getIslandRankByDislikes(this.world, likesObject);
                        int byRank = this.addon.getAddonManager().getIslandRankByRank(this.world, likesObject);
                        
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
                    }
                    case STARS -> {
                        int rank = this.addon.getAddonManager().getIslandRankByStars(this.world, likesObject);
                        
                        if (rank != -1)
                        {
                            placeText = this.user.getTranslation(reference + "place",
                                Constants.PARAMETER_NUMBER, String.valueOf(rank),
                                Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "stars"));
                        }
                    }
                }
            }
            
            // Now combine everything.
            String descriptionText = this.user.getTranslation(reference + "description",
                Constants.PARAMETER_OWNER, ownerText,
                Constants.PARAMETER_MEMBERS, memberText,
                Constants.PARAMETER_PLACE, placeText,
                Constants.PARAMETER_NUMBERS, numbersText);

            builder.description(descriptionText.
                replaceAll("(?m)^[ \\t]*\\r?\\n", "").
                replaceAll("(?<!\\\\)\\|", "\n").
                replaceAll("\\\\\\|", "|"));
        }
    }
    

    /**
     * Create viewer button panel item.
     *
     * @return PanelItem for PanelBuilder.
     */
    private PanelItem createViewerButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        Island island = this.addon.getIslands().getIsland(this.world, this.user);

        if (island == null || island.getOwner() == null)
        {
            // Player do not have an island.
            return null;
        }

        LikesObject likesObject =
            this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId());

        if (likesObject == null)
        {
            // Island do not have any valid data. It is empty. Do not show.
            return null;
        }
        
        PanelItemBuilder builder = new PanelItemBuilder();
        
        this.populateIslandIcon(builder, template, island);
        this.populateIslandTitle(builder, template, island);
        this.populateIslandDescription(builder, template, island, likesObject, true);

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            LikesViewPanel.openPanel(this.addon, this.user, this.world, this.iconPermission, island);
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }
        
        return builder.build();
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
    private IndexedTreeSet<LikesObject> topPlayerList;

    /**
     * Stores decimal format object for two digit after separator.
     */
    private final DecimalFormat hundredsFormat;
}
