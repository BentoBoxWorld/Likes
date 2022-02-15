//
// Created by BONNe
// Copyright - 2022
//


package world.bentobox.likes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This gui shows all information about Likes Object.
 */
public class LikesViewPanel extends CommonPanel
{
    /**
     * Instantiates a new Likes templated view panel.
     *
     * @param addon the addon
     * @param user the user
     * @param world the world
     * @param permissionPrefix the permission prefix
     * @param likesObject the likes object
     */
    private LikesViewPanel(@NonNull LikesAddon addon,
        @NonNull User user,
        @NonNull World world,
        String permissionPrefix,
        LikesObject likesObject)
    {
        super(addon, user, world, permissionPrefix);

        this.likesObject = likesObject;

        switch (this.addon.getSettings().getMode())
        {
            case LIKES -> {
                this.likedByUsers = this.likesObject.getLikedBy().stream().
                    map(uuid -> this.addon.getPlayers().getName(uuid)).
                    sorted(String::compareToIgnoreCase).
                    collect(Collectors.toList());
                // Collect Rank
                this.likeRank = this.addon.getAddonManager().getIslandRankByLikes(world, likesObject);
            }
            case LIKES_DISLIKES -> {
                this.likedByUsers = this.likesObject.getLikedBy().stream().
                    map(uuid -> this.addon.getPlayers().getName(uuid)).
                    sorted(String::compareToIgnoreCase).
                    collect(Collectors.toList());
                this.dislikedByUsers = this.likesObject.getDislikedBy().stream().
                    map(uuid -> this.addon.getPlayers().getName(uuid)).
                    sorted(String::compareToIgnoreCase).
                    collect(Collectors.toList());
                // Collect Rank
                this.likeRank = this.addon.getAddonManager().getIslandRankByLikes(world, likesObject);
                this.dislikeRank = this.addon.getAddonManager().getIslandRankByDislikes(world, likesObject);
                this.overallRank = this.addon.getAddonManager().getIslandRankByRank(world, likesObject);
            }
            case STARS -> {
                this.likedByUsers = this.likesObject.getStarredBy().keySet().stream().
                    map(uuid -> this.addon.getPlayers().getName(uuid)).
                    sorted(String::compareToIgnoreCase).
                    collect(Collectors.toList());
                // Collect Rank
                this.likeRank = this.addon.getAddonManager().getIslandRankByStars(world, likesObject);
            }
        }

        this.hundredsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
        this.hundredsFormat.applyPattern("###.##");
    }


    /**
     * Opens new Likes panel that shows who and how much added likes/dislikes/stars.
     *
     * @param addon the addon
     * @param user the user
     * @param world the world
     * @param permissionPrefix the permission prefix
     * @param island the island
     */
    public static void openPanel(@NonNull LikesAddon addon,
        @NonNull User user,
        @NonNull World world,
        String permissionPrefix,
        @NonNull Island island)
    {
        LikesObject likesObject = addon.getAddonManager().getExistingIslandLikes(island.getUniqueId());

        if (likesObject == null)
        {
            if (island.getMemberSet().contains(user.getUniqueId()))
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "no-data-about-your-island"));
            }
            else
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "no-data-about-island"));
            }

            // Do not open gui if there is no data.
            return;
        }

        new LikesViewPanel(addon, user, world, permissionPrefix, likesObject).build();
    }


    /**
     * This method builds given panel for a user.
     */
    @Override
    public void build()
    {
        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        panelBuilder.user(this.user);
        panelBuilder.world(this.world);

        // Set main template.
        switch (this.addon.getSettings().getMode())
        {
            case LIKES -> {
                panelBuilder.template("likes",
                    "view_panels",
                    new File(this.addon.getDataFolder(), "panels"));
                // Register Top Buttons
                panelBuilder.registerTypeBuilder("LIKE",
                    (record, itemSlot) -> this.createButton(record, Button.LIKE));
                panelBuilder.registerTypeBuilder("LIKE_RANK",
                    (record, itemSlot) -> this.createButton(record, Button.LIKE_RANK));
                // Register Player Button
                panelBuilder.registerTypeBuilder("LIKE_PLAYER", this::createLikePlayer);
            }
            case LIKES_DISLIKES -> {
                panelBuilder.template("likes_dislikes",
                    "view_panels",
                    new File(this.addon.getDataFolder(), "panels"));
                // Register Top Buttons
                panelBuilder.registerTypeBuilder("LIKE",
                    (record, itemSlot) -> this.createButton(record, Button.LIKE));
                panelBuilder.registerTypeBuilder("LIKE_RANK",
                    (record, itemSlot) -> this.createButton(record, Button.LIKE_RANK));
                panelBuilder.registerTypeBuilder("OVERALL",
                    (record, itemSlot) -> this.createButton(record, Button.OVERALL));
                panelBuilder.registerTypeBuilder("OVERALL_RANK",
                    (record, itemSlot) -> this.createButton(record, Button.OVERALL_RANK));
                panelBuilder.registerTypeBuilder("DISLIKE",
                    (record, itemSlot) -> this.createButton(record, Button.DISLIKE));
                panelBuilder.registerTypeBuilder("DISLIKE_RANK",
                    (record, itemSlot) -> this.createButton(record, Button.DISLIKE_RANK));
                // Register Player Button
                panelBuilder.registerTypeBuilder("LIKE_PLAYER", this::createLikePlayer);
                panelBuilder.registerTypeBuilder("DISLIKE_PLAYER", this::createDislikePlayer);

            }
            case STARS -> {
                panelBuilder.template("stars",
                    "view_panels",
                    new File(this.addon.getDataFolder(), "panels"));
                // Register Top Button
                panelBuilder.registerTypeBuilder("STARS",
                    (record, itemSlot) -> this.createButton(record, Button.STARS));
                panelBuilder.registerTypeBuilder("STARS_RANK",
                    (record, itemSlot) -> this.createButton(record, Button.STARS_RANK));
                // Register Player Button
                panelBuilder.registerTypeBuilder("STARS_PLAYER", this::createStarsPlayer);
            }
        }

        panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
        panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    @Nullable
    private PanelItem createLikePlayer(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.likedByUsers.isEmpty())
        {
            return null;
        }

        String player;

        int index = this.likeIndex * slot.amountMap().getOrDefault("LIKE_PLAYER", 1) + slot.slot();

        if (index >= this.likedByUsers.size())
        {
            // Out of index.
            return null;
        }

        player = this.likedByUsers.get(index);

        return this.createUserButton(template, player);
    }


    @Nullable
    private PanelItem createDislikePlayer(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.dislikedByUsers.isEmpty())
        {
            return null;
        }

        String player;

        int index = this.dislikeIndex * slot.amountMap().getOrDefault("DISLIKE_PLAYER", 1) + slot.slot();

        if (index >= this.dislikedByUsers.size())
        {
            // Out of index.
            return null;
        }

        player = this.dislikedByUsers.get(index);

        return this.createUserButton(template, player);
    }


    @Nullable
    private PanelItem createStarsPlayer(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.likedByUsers.isEmpty())
        {
            return null;
        }

        String player;

        int index = this.likeIndex * slot.amountMap().getOrDefault("STARS_PLAYER", 1) + slot.slot();

        if (index >= this.likedByUsers.size())
        {
            // Out of index.
            return null;
        }

        player = this.likedByUsers.get(index);

        return this.createUserButton(template,
            player,
            this.likesObject.getStarredBy().get(this.addon.getPlayers().getUUID(player)));
    }


    /**
     * This method creates User Button.
     * @param userName Username who button must be created.
     * @param template Template of the button
     * @return instance PanelItem
     */
    private PanelItem createUserButton(ItemTemplateRecord template, String userName)
    {
        return this.createUserButton(template, userName, 1);
    }


    /**
     * This method creates User Button.
     * @param userName Username who button must be created.
     * @param amount ItemStack amount.
     * @param template Template of the button
     * @return instance PanelItem
     */
    private PanelItem createUserButton(ItemTemplateRecord template, String userName, int amount)
    {
        final String reference = Constants.BUTTONS + "user.";

        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.

        if (template.icon() == null)
        {
            builder.icon(userName);
        }
        else
        {
            if (template.icon().getType().equals(Material.PLAYER_HEAD))
            {
                builder.icon(userName);
            }
            else
            {
                builder.icon(template.icon().clone());
            }
        }

        // Template specific title is always more important than biomesObject name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(Util.translateColorCodes(
                this.user.getTranslation(this.world, template.title(),
                    Constants.PARAMETER_NAME, userName)));
        }
        else
        {
            builder.name(this.user.getTranslation(this.world, reference + "name",
                Constants.PARAMETER_NAME, userName));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            builder.description(Util.translateColorCodes(
                this.user.getTranslation(this.world, template.description())));
        }
        else
        {
            builder.description(this.user.getTranslationOrNothing(reference + "description"));
        }

        builder.amount(amount);

        return builder.build();
    }


    @Nullable
    private PanelItem createButton(@NonNull ItemTemplateRecord template, Button button)
    {
        final String reference = Constants.BUTTONS + button.name().toLowerCase() + ".";

        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.

        if (template.icon() == null)
        {
            switch (button)
            {
                case LIKE -> builder.icon(Material.GOLD_INGOT);
                case LIKE_RANK -> builder.icon(Material.GOLD_BLOCK);
                case DISLIKE -> builder.icon(Material.IRON_INGOT);
                case DISLIKE_RANK -> builder.icon(Material.IRON_BLOCK);
                case OVERALL -> builder.icon(Material.DIAMOND);
                case OVERALL_RANK -> builder.icon(Material.DIAMOND_BLOCK);
                case STARS -> builder.icon(Material.NETHER_STAR);
                case STARS_RANK -> builder.icon(Material.BEACON);
            }
        }
        else
        {
            builder.icon(template.icon().clone());
        }

        // Template specific title is always more important than biomesObject name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(Util.translateColorCodes(
                this.user.getTranslation(this.world, template.title())));
        }
        else
        {
            builder.name(this.user.getTranslation(this.world, reference + "name"));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            builder.description(Util.translateColorCodes(
                this.user.getTranslation(this.world, template.description())));
        }
        else
        {
            builder.description(this.user.getTranslationOrNothing(reference + "description"));
        }

        switch (button)
        {
            case LIKE -> {
                builder.description(this.user.getTranslation(reference + "likes",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.likesObject.getLikes())));
            }
            case LIKE_RANK -> {
                if (this.likesObject.getLikes() == 0 && this.likesObject.getDislikes() == 0)
                {
                    builder.description(this.user.getTranslation(reference + "not-ranked"));
                }
                else
                {
                    builder.description(this.user.getTranslation(reference + "rank",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.likeRank)));
                }
            }
            case DISLIKE -> {
                builder.description(this.user.getTranslation(reference + "dislikes",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.likesObject.getDislikes())));
            }
            case DISLIKE_RANK -> {
                if (this.likesObject.getLikes() == 0 && this.likesObject.getDislikes() == 0)
                {
                    builder.description(this.user.getTranslation(reference + "not-ranked"));
                }
                else
                {
                    builder.description(this.user.getTranslation(reference + "rank",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.dislikeRank)));
                }
            }
            case OVERALL -> {
                builder.description(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.likesObject.getRank())));
            }
            case OVERALL_RANK -> {
                if (this.likesObject.getLikes() == 0 && this.likesObject.getDislikes() == 0)
                {
                    builder.description(this.user.getTranslation(reference + "not-ranked"));
                }
                else
                {
                    builder.description(this.user.getTranslation(reference + "rank",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.overallRank)));
                }
            }
            case STARS -> {
                builder.description(this.user.getTranslation(reference + "stars",
                    Constants.PARAMETER_NUMBER, this.hundredsFormat.format(this.likesObject.getStarsValue())));
            }
            case STARS_RANK -> {
                if (this.likesObject.getStars() == 0)
                {
                    builder.description(this.user.getTranslation(reference + "not-ranked"));
                }
                else
                {
                    builder.description(this.user.getTranslation(reference + "rank",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.likeRank)));
                }
            }
        }

        return builder.build();
    }


    @Nullable
    private PanelItem createNextButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        String target = template.dataMap().getOrDefault("target", "").toString().toUpperCase();

        int nextPageIndex;

        switch (target)
        {
            case "LIKE" -> {
                int size = this.likedByUsers.size();

                if (size <= slot.amountMap().getOrDefault("LIKE_PLAYER", 1) ||
                    1.0 * size / slot.amountMap().getOrDefault("LIKE_PLAYER", 1) <= this.likeIndex + 1)
                {
                    // There are no next elements
                    return null;
                }

                nextPageIndex = this.likeIndex + 2;
            }
            case "STARS" -> {
                int size = this.likedByUsers.size();

                if (size <= slot.amountMap().getOrDefault("STARS_PLAYER", 1) ||
                    1.0 * size / slot.amountMap().getOrDefault("STARS_PLAYER", 1) <= this.likeIndex + 1)
                {
                    // There are no next elements
                    return null;
                }

                nextPageIndex = this.likeIndex + 2;
            }
            case "DISLIKE" -> {
                int size = this.dislikedByUsers.size();

                if (size <= slot.amountMap().getOrDefault("DISLIKE_PLAYER", 1) ||
                    1.0 * size / slot.amountMap().getOrDefault("DISLIKE_PLAYER", 1) <= this.dislikeIndex + 1)
                {
                    // There are no next elements
                    return null;
                }

                nextPageIndex = this.dislikeIndex + 2;
            }
            default -> {
                // If not assigned to any type, return null.
                return null;
            }
        }

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(nextPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(nextPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            switch (target)
            {
                case "LIKE", "STARS" -> this.likeIndex++;
                case "DISLIKE" -> this.dislikeIndex++;
            }

            this.build();

            // Always return true.
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


    @Nullable
    private PanelItem createPreviousButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        String target = template.dataMap().getOrDefault("target", "").toString().toUpperCase();

        int previousPageIndex;

        if ("LIKE".equals(target) || "STARS".equals(target))
        {
            if (this.likeIndex == 0)
            {
                // There are no next elements
                return null;
            }

            previousPageIndex = this.likeIndex;
        }
        else if ("DISLIKE".equals(target))
        {
            if (this.dislikeIndex == 0)
            {
                // There are no next elements
                return null;
            }

            previousPageIndex = this.dislikeIndex;
        }
        else
        {
            // If not assigned to any type, return null.
            return null;
        }

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(previousPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(previousPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            switch (target)
            {
                case "LIKE", "STARS" -> this.likeIndex--;
                case "DISLIKE" -> this.dislikeIndex--;
            }

            this.build();

            // Always return true.
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


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


    /**
     * This enum holds all action buttons that can be added in current gui.
     */
    private enum Button
    {
        /**
         * Like button.
         */
        LIKE,
        /**
         * Like rank button.
         */
        LIKE_RANK,
        /**
         * Dislike button.
         */
        DISLIKE,
        /**
         * Dislike rank button.
         */
        DISLIKE_RANK,
        /**
         * Overall button.
         */
        OVERALL,
        /**
         * Overall rank button.
         */
        OVERALL_RANK,
        /**
         * Stars button.
         */
        STARS,
        /**
         * Stars rank button.
         */
        STARS_RANK,
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This variable holds likes object that is view by user in current gui.
     */
    private final LikesObject likesObject;

    /**
     * This variable stores index of current liker.
     */
    private int likeIndex;

    /**
     * This variable stores index of current disliker.
     */
    private int dislikeIndex;

    /**
     * This list contains player names that liked current island.
     */
    private List<String> likedByUsers;

    /**
     * This list contains player names that disliked current island.
     */
    private List<String> dislikedByUsers;

    /**
     * This variable holds island rank by likes.
     */
    private long likeRank;

    /**
     * This variable holds island rank by dislikes.
     */
    private long dislikeRank;

    /**
     * This variable holds island rank by rank.
     */
    private long overallRank;

    /**
     * Stores decimal format object for two digit after separator.
     */
    private final DecimalFormat hundredsFormat;
}
