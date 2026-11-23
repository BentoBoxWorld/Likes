///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.panels.user;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.io.File;
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

        this.iconPermission = permissionPrefix + "likes.icon";
        this.viewMode = mode;
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

        this.setViewModeIcon(builder, template);
        this.setViewModeTitle(builder, template, reference);
        this.setViewModeDescription(builder, template, reference);

        VIEW_MODE[] values = new VIEW_MODE[3];
        values[0] = VIEW_MODE.LIKES;
        values[1] = VIEW_MODE.DISLIKES;
        values[2] = VIEW_MODE.RANK;

        builder.clickHandler((panel, clicker, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : template.actions())
            {
                if (clickType == action.clickType() || action.clickType() == ClickType.UNKNOWN)
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

        this.addViewModeTooltips(builder, template);

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


    /**
     * Set icon for view mode button.
     *
     * @param builder the builder
     * @param template the template
     */
    private void setViewModeIcon(PanelItemBuilder builder, ItemTemplateRecord template)
    {
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
                default -> {
                    // Stars are not shown with view mode button.
                }
            }
        }
    }


    /**
     * Set title for view mode button.
     *
     * @param builder the builder
     * @param template the template
     * @param reference the reference key
     */
    private void setViewModeTitle(PanelItemBuilder builder, ItemTemplateRecord template, String reference)
    {
        // Template specific title is always more important than biomesObject name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }
        else
        {
            builder.name(this.user.getTranslationOrNothing(reference + "name"));
        }
    }


    /**
     * Set description for view mode button.
     *
     * @param builder the builder
     * @param template the template
     * @param reference the reference key
     */
    private void setViewModeDescription(PanelItemBuilder builder, ItemTemplateRecord template, String reference)
    {
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
    }


    /**
     * Add tooltips to view mode button.
     *
     * @param builder the builder
     * @param template the template
     */
    private void addViewModeTooltips(PanelItemBuilder builder, ItemTemplateRecord template)
    {
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
    }


    /**
     * This method creates player icon with warp functionality.
     *
     * @return PanelItem for PanelBuilder.
     */
    private PanelItem createPlayerButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        int index = (int) template.dataMap().getOrDefault("index", 0);
        Material background = Material.matchMaterial((String) template.dataMap().getOrDefault("background", ""));

        if (index < 1)
        {
            return this.createFallback(background, template.fallback(), index);
        }

        LikesObject likesObject = this.topPlayerList.size() < index ? null : this.topPlayerList.exact(index - 1);

        if (likesObject == null)
        {
            return this.createFallback(background, template.fallback(), index);
        }

        Optional<Island> optionalIsland = this.addon.getIslands().getIslandById(likesObject.getUniqueId());

        if (optionalIsland.isEmpty())
        {
            return this.createFallback(background, template.fallback(), index);
        }

        // Get player island.
        Island island = optionalIsland.get();

        PanelItemBuilder builder = new PanelItemBuilder();

        this.populateIslandIcon(builder, template, island);
        this.populateIslandTitle(builder, template, island);
        this.populateIslandDescription(builder, template, island, likesObject, false);

        builder.amount(index);

        // Get only possible actions, by removing all inactive ones.
        List<ItemTemplateRecord.ActionRecords> activeActions = this.getActivePlayerActions(template, island);

        // Add Click handler
        builder.clickHandler((panel, clicker, clickType, i) ->
        {
            this.handlePlayerButtonClick(activeActions, island, clickType);
            return true;
        });

        this.addPlayerButtonTooltips(builder, activeActions, template);

        return builder.build();
    }


    /**
     * Get active player actions for the island.
     *
     * @param template the template
     * @param island the island
     * @return list of active actions
     */
    private List<ItemTemplateRecord.ActionRecords> getActivePlayerActions(ItemTemplateRecord template, Island island)
    {
        List<ItemTemplateRecord.ActionRecords> activeActions = new ArrayList<>(template.actions());

        activeActions.removeIf(action ->
        {
            String actionType = action.actionType().toUpperCase();
            if ("WARP".equals(actionType))
            {
                return !this.hasWarp(island);
            }
            else if ("VISIT".equals(actionType))
            {
                return island.getOwner() == null ||
                    this.addon.getVisitHook() == null ||
                    !this.addon.getVisitHook().getAddonManager().preprocessTeleportation(this.user, island, true);
            }
            return false;
        });

        return activeActions;
    }


    /**
     * Handle player button click.
     *
     * @param activeActions the list of active actions
     * @param island the island
     * @param clickType the click type
     */
    private void handlePlayerButtonClick(List<ItemTemplateRecord.ActionRecords> activeActions, Island island, ClickType clickType)
    {
        for (ItemTemplateRecord.ActionRecords action : activeActions)
        {
            if (clickType == action.clickType() || action.clickType() == ClickType.UNKNOWN)
            {
                switch (action.actionType().toUpperCase())
                {
                    case "WARP" -> this.warpToIsland(island);
                    case "VISIT" -> this.visitIsland(island);
                    default -> {
                        // Other actions do nothing.
                    }
                }
            }
        }
    }


    /**
     * Add tooltips to player button.
     *
     * @param builder the builder
     * @param activeActions the list of active actions
     * @param template the template
     */
    private void addPlayerButtonTooltips(PanelItemBuilder builder, List<ItemTemplateRecord.ActionRecords> activeActions, ItemTemplateRecord template)
    {
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
    }


    /**
     * This method checks if the given island has a warp sign. Warps are stored per player, but players can have
     * multiple islands, so the warp belongs only to the island where the sign is located.
     *
     * @param island Island that must be checked.
     * @return {@code true} if island owner has a warp sign on the given island, {@code false} otherwise.
     */
    private boolean hasWarp(Island island)
    {
        if (island.getOwner() == null || this.addon.getWarpHook() == null)
        {
            return false;
        }

        Location warpLocation =
            this.addon.getWarpHook().getWarpSignsManager().getWarpLocation(this.world, island.getOwner());

        return warpLocation != null && island.inIslandSpace(warpLocation);
    }


    /**
     * This method sends user to the warp sign of the given island owner.
     *
     * @param island Island which warp user wants to use.
     */
    private void warpToIsland(Island island)
    {
        UUID ownerId = island.getOwner();

        if (ownerId != null)
        {
            this.user.closeInventory();
            this.addon.getWarpHook().getWarpSignsManager().warpPlayer(this.world, this.user, ownerId);
        }
    }


    /**
     * This method sends user to the given island using the Visit addon.
     *
     * @param island Island that user wants to visit.
     */
    private void visitIsland(Island island)
    {
        UUID ownerId = island.getOwner();

        if (ownerId == null)
        {
            // Unowned islands cannot be visited.
            return;
        }

        Island primaryIsland = this.addon.getIslands().getIsland(this.world, ownerId);

        if (primaryIsland != null && primaryIsland.getUniqueId().equals(island.getUniqueId()))
        {
            // The visit command always leads to the primary island of the owner. So it can be used
            // for it, and it solves necessity to check for all visits options, like cool down and
            // confirmation in single go.
            this.addon.getPlugin().getIWM().getAddon(this.world).
                flatMap(GameModeAddon::getPlayerCommand).ifPresent(command ->
                {
                    String mainCommand =
                        this.addon.getVisitHook().getSettings().getPlayerMainCommand();

                    if (!mainCommand.isBlank())
                    {
                        this.user.closeInventory();
                        this.user.performCommand(command.getTopLabel() + " " + mainCommand + " " + ownerId);
                    }
                });
        }
        else if (this.addon.getVisitHook().getAddonManager().preprocessTeleportation(this.user, island))
        {
            // The visit command cannot target other islands of the same owner. Teleport to the
            // exact island directly.
            this.user.closeInventory();
            this.addon.getVisitHook().getAddonManager().processTeleportation(this.user, island, this.world);
        }
    }


    /**
     * Creates fallback based on template.
     *
     * @param background Fallback icon implemented without fallback.
     * @param template Template record for fallback button.
     * @param index Place of the fallback.
     * @return Fallback panel item.
     */
    private PanelItem createFallback(Material background, ItemTemplateRecord template, long index)
    {
        final String reference = "likes.gui.buttons.island.";

        if (background != null)
        {
            return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "empty", NAME_PARAMETER, String.valueOf(index))).
                icon(background).
                build();
        }

        if (template == null)
        {
            return null;
        }

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                NAME_PARAMETER, String.valueOf(index)));
        }
        else
        {
            builder.name(this.user.getTranslation(this.world, reference,
                NAME_PARAMETER, String.valueOf(index)));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                "[number]", String.valueOf(index)));
        }

        builder.amount(index == 0 ? 1 : (int) index);

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
        String nameText = this.getIslandNameText(reference, island);

        // Template specific title is always more important than custom one.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                NAME_PARAMETER, nameText,
                "[owner]", this.addon.getPlayers().getName(island.getOwner())));
        }
        else
        {
            builder.name(this.user.getTranslation(reference + "name", Constants.PARAMETER_NAME, nameText));
        }
    }


    /**
     * Get island name text.
     *
     * @param reference the reference key
     * @param island the island
     * @return island name text
     */
    private String getIslandNameText(String reference, Island island)
    {
        if (island.getName() == null || island.getName().isEmpty())
        {
            if (island.isSpawn())
            {
                return this.user.getTranslation(reference + "spawn");
            }
            else
            {
                return this.user.getTranslation(reference + "owners-island",
                    "[player]",
                    island.getOwner() == null ?
                        this.user.getTranslation(reference + "unknown") :
                        this.addon.getPlayers().getName(island.getOwner()));
            }
        }
        else
        {
            return island.getName();
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
        UUID ownerId = island.getOwner();
        User owner = ownerId == null ? null : User.getInstance(ownerId);

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
            ItemStack icon = template.icon().clone();

            if (icon.getType().equals(Material.PLAYER_HEAD))
            {
                SkullMeta meta = (SkullMeta) icon.getItemMeta();

                if (meta != null && meta.hasOwner() || owner == null)
                {
                    builder.icon(icon);
                }
                else
                {
                    builder.icon(owner.getName());
                }
            }
            else
            {
                builder.icon(icon);
            }
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

        String ownerText = this.generateOwnerText(reference, island);
        String memberText = this.generateMemberText(reference, island);
        String numbersText = this.generateNumbersText(reference, likesObject);
        String placeText = generatePlaceText ? this.generatePlaceText(reference, likesObject) : "";

        // Template specific description is always more important than custom one.
        if (template.description() != null && !template.description().isBlank())
        {
            builder.description(this.user.getTranslationOrNothing(template.description(),
                    Constants.PARAMETER_OWNER, ownerText,
                    Constants.PARAMETER_MEMBERS, memberText,
                    Constants.PARAMETER_PLACE, placeText,
                    Constants.PARAMETER_NUMBERS, numbersText).
                replaceAll("(?m)^[ \\t]*\\r?\\n", "").
                replaceAll("(?<!\\\\)\\|", "\n").
                replace("\\|", "|"));
        }
        else
        {
            // Now combine everything.
            String descriptionText = this.user.getTranslation(reference + "description",
                Constants.PARAMETER_OWNER, ownerText,
                Constants.PARAMETER_MEMBERS, memberText,
                Constants.PARAMETER_PLACE, placeText,
                Constants.PARAMETER_NUMBERS, numbersText);

            builder.description(descriptionText.
                replaceAll("(?m)^[ \\t]*\\r?\\n", "").
                replaceAll("(?<!\\\\)\\|", "\n").
                replace("\\|", "|"));
        }
    }


    /**
     * Generate owner text for description.
     *
     * @param reference the reference key
     * @param island the island
     * @return owner text
     */
    private String generateOwnerText(String reference, Island island)
    {
        if (island.isSpawn())
        {
            return "";
        }
        else
        {
            return this.user.getTranslation(reference + "owner",
                Constants.PARAMETER_PLAYER,
                island.getOwner() == null ?
                    this.user.getTranslation(reference + "unknown") :
                    this.addon.getPlayers().getName(island.getOwner()));
        }
    }


    /**
     * Generate member text for description.
     *
     * @param reference the reference key
     * @param island the island
     * @return member text
     */
    private String generateMemberText(String reference, Island island)
    {
        if (island.getMemberSet().size() > 1)
        {
            StringBuilder memberBuilder = new StringBuilder(
                this.user.getTranslationOrNothing(reference + "members-title"));

            for (UUID uuid : island.getMemberSet())
            {
                String name = this.addon.getPlayers().getName(uuid);

                if (!memberBuilder.isEmpty())
                {
                    memberBuilder.append("\n");
                }

                memberBuilder.append(
                    this.user.getTranslationOrNothing(reference + "member",
                        Constants.PARAMETER_PLAYER, name));
            }

            return memberBuilder.toString();
        }
        else
        {
            return "";
        }
    }


    /**
     * Generate numbers text for description.
     *
     * @param reference the reference key
     * @param likesObject the likes object
     * @return numbers text
     */
    private String generateNumbersText(String reference, LikesObject likesObject)
    {
        return switch (this.addon.getSettings().getMode()) {
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
    }


    /**
     * Generate place text for description.
     *
     * @param reference the reference key
     * @param likesObject the likes object
     * @return place text
     */
    private String generatePlaceText(String reference, LikesObject likesObject)
    {
        String placeText = "";

        switch (this.addon.getSettings().getMode())
        {
            case LIKES -> {
                int rank = this.addon.getAddonManager().getIslandRankByLikes(this.world, likesObject);

                if (rank != -1)
                {
                    placeText = this.user.getTranslation(reference + PLACE_BUTTON_KEY,
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
                    placeText = this.user.getTranslation(reference + PLACE_BUTTON_KEY,
                        Constants.PARAMETER_NUMBER, String.valueOf(byLikes),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes"));
                }

                if (byDislikes != -1)
                {
                    placeText += "\n" + this.user.getTranslation(reference + PLACE_BUTTON_KEY,
                        Constants.PARAMETER_NUMBER, String.valueOf(byDislikes),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "dislikes"));
                }

                if (byRank != -1)
                {
                    placeText += "\n" + this.user.getTranslation(reference + PLACE_BUTTON_KEY,
                        Constants.PARAMETER_NUMBER, String.valueOf(byRank),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "rank"));
                }
            }
            case STARS -> {
                int rank = this.addon.getAddonManager().getIslandRankByStars(this.world, likesObject);

                if (rank != -1)
                {
                    placeText = this.user.getTranslation(reference + PLACE_BUTTON_KEY,
                        Constants.PARAMETER_NUMBER, String.valueOf(rank),
                        Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "stars"));
                }
            }
        }

        return placeText;
    }
    

    /**
     * Create viewer button panel item.
     *
     * @return PanelItem for PanelBuilder.
     */
    private PanelItem createViewerButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        // Multi-island: island where player stands, or their primary island.
        Island island = Utils.getIsland(this.addon.getIslands(), this.world, this.user);

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

        if (template.actions().size() == 1)
        {
            ItemTemplateRecord.ActionRecords action = template.actions().get(0);

            // Add Click handler
            builder.clickHandler((panel, user, clickType, i) ->
            {
                if (clickType == action.clickType() || action.clickType() == ClickType.UNKNOWN)
                {
                    LikesViewPanel.openPanel(this.addon, this.user, this.world, this.iconPermission, island);
                }

                return true;
            });

            String tooltips = this.user.getTranslation(this.world, action.tooltip());

            // Add tooltips.
            if (!tooltips.isEmpty())
            {
                // Empty line and tooltips.
                builder.description("");
                builder.description(tooltips);
            }
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
     * String constant for "[name]" parameter.
     */
    private static final String NAME_PARAMETER = "[name]";

    /**
     * String constant for "place" button key.
     */
    private static final String PLACE_BUTTON_KEY = "place";

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
}
