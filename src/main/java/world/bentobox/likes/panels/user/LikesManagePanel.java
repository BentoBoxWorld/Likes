///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import java.io.File;
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
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This class creates GUI that allows to like and dislike island.
 */
public class LikesManagePanel extends CommonPanel
{
    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param addon Likes object.
     * @param user User who opens Panel.
     * @param world World where gui is opened
     * @param permissionPrefix Permission Prefix
     * @param island The id of island which likes should be managed.
     */
    private LikesManagePanel(LikesAddon addon, User user, World world, String permissionPrefix, Island island)
    {
        super(addon, user, world, permissionPrefix);
        this.settings = addon.getSettings();
        this.target = user;

        this.island = island;
    }


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param parent Parent GUI
     * @param target User who adds like (equals user unless admin forces different)
     * @param island The id of island which likes should be managed.
     */
    private LikesManagePanel(CommonPanel parent, User target, Island island)
    {
        super(parent);
        this.settings = this.addon.getSettings();
        this.target = target;

        this.island = island;
    }


    /**
     * Method that must be added in all panels.
     */
    public void build()
    {
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        panelBuilder.user(this.user);
        panelBuilder.world(this.world);

        // Set main template.
        switch (this.settings.getMode())
        {
            case LIKES -> {
                panelBuilder.template("manage_likes_panel", new File(this.addon.getDataFolder(), "panels"));

                panelBuilder.registerTypeBuilder("ADD_LIKE", this::createLikeButton);
            }
            case LIKES_DISLIKES -> {
                panelBuilder.template("manage_likes_dislikes_panel", new File(this.addon.getDataFolder(), "panels"));

                panelBuilder.registerTypeBuilder("ADD_LIKE", this::createLikeButton);
                panelBuilder.registerTypeBuilder("ADD_DISLIKE", this::createDislikeButton);
            }
            case STARS -> {
                panelBuilder.template("manage_stars_panel", new File(this.addon.getDataFolder(), "panels"));

                final int starCount = this.addon.getAddonManager().getStarred(
                    this.target.getUniqueId(),
                    this.island.getUniqueId(),
                    this.world);

                panelBuilder.registerTypeBuilder("ADD_STAR",
                    (template, itemSlot) -> this.createStarButton(template, itemSlot, starCount));
            }
        }

        // Register unknown type builder.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    private PanelItem createLikeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        final boolean hasLiked =
            this.addon.getAddonManager().hasLiked(this.target.getUniqueId(), this.island.getUniqueId(), this.world);

        final String reference = Constants.BUTTONS + "add_like.";

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            builder.icon(Material.GOLD_INGOT);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }
        else
        {
            builder.name(this.user.getTranslation(reference + "name"));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.user.getTranslationOrNothing(reference + "description"));

            if (this.addon.isEconomyProvided())
            {
                if (hasLiked && this.settings.getLikeRemoveCost() > 0)
                {
                    builder.description(this.user.getTranslation(reference + "cost",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeRemoveCost())));
                }
                else if (!hasLiked && this.settings.getLikeAddCost() > 0)
                {
                    builder.description(this.user.getTranslation(reference + "cost",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeAddCost())));
                }
            }
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            if (hasLiked)
            {
                if (this.hasPaid(this.settings.getLikeRemoveCost()))
                {
                    this.addon.getAddonManager().removeLike(this.target, this.island, this.world);
                }
            }
            else
            {
                if (this.hasPaid(this.settings.getLikeAddCost()))
                {
                    this.addon.getAddonManager().addLike(this.target, this.island, this.world);
                }
            }

            if (this.parent != null)
            {
                this.parent.build();
            }
            else
            {
                user.closeInventory();
            }

            // Always return true.
            return true;
        });

        builder.glow(hasLiked);

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
        else
        {
            builder.description("");

            if (hasLiked)
            {
                builder.description(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
            }
            else
            {
                builder.description(this.user.getTranslation(Constants.TIPS + "click-to-add"));
            }
        }

        return builder.build();
    }



    private PanelItem createDislikeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        final boolean hasDisliked =
            this.addon.getAddonManager().hasDisliked(this.target.getUniqueId(), this.island.getUniqueId(), this.world);

        final String reference = Constants.BUTTONS + "add_dislike.";

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            builder.icon(Material.IRON_INGOT);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }
        else
        {
            builder.name(this.user.getTranslation(reference + "name"));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.user.getTranslationOrNothing(reference + "description"));

            if (this.addon.isEconomyProvided())
            {
                if (hasDisliked && this.settings.getDislikeRemoveCost() > 0)
                {
                    builder.description(this.user.getTranslation(reference + "cost",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getDislikeRemoveCost())));
                }
                else if (!hasDisliked && this.settings.getDislikeAddCost() > 0)
                {
                    builder.description(this.user.getTranslation(reference + "cost",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getDislikeAddCost())));
                }
            }
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            if (hasDisliked)
            {
                if (this.hasPaid(this.settings.getDislikeRemoveCost()))
                {
                    this.addon.getAddonManager().removeDislike(this.target, this.island, this.world);
                }
            }
            else
            {
                if (this.hasPaid(this.settings.getDislikeAddCost()))
                {
                    this.addon.getAddonManager().addDislike(this.target, this.island, this.world);
                }
            }

            if (this.parent != null)
            {
                this.parent.build();
            }
            else
            {
                user.closeInventory();
            }

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        builder.glow(hasDisliked);

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }
        else
        {
            builder.description("");

            if (hasDisliked)
            {
                builder.description(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
            }
            else
            {
                builder.description(this.user.getTranslation(Constants.TIPS + "click-to-add"));
            }
        }

        return builder.build();
    }


    private PanelItem createStarButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot, int value)
    {
        final boolean hasStarred = value > 0 && value < 6;

        final String reference = Constants.BUTTONS + "add_star.";

        PanelItemBuilder builder = new PanelItemBuilder();

        int starsValue = (int) template.dataMap().getOrDefault("value", 1);

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            builder.icon(Material.NETHER_STAR);
            builder.amount(starsValue);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                "[number]", String.valueOf(starsValue)));
        }
        else
        {
            builder.name(this.user.getTranslation(reference + "name",
                "[number]", String.valueOf(starsValue)));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.user.getTranslationOrNothing(reference + "description"));

            if (this.addon.isEconomyProvided())
            {
                if (hasStarred && this.settings.getLikeAddCost() > 0)
                {
                    builder.description(this.user.getTranslation(reference + "cost",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeAddCost())));
                }
                else if (!hasStarred && this.settings.getLikeRemoveCost() > 0)
                {
                    builder.description(this.user.getTranslation(reference + "cost",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeRemoveCost())));
                }
            }
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            if (hasStarred)
            {
                if (value == starsValue)
                {
                    if (this.hasPaid(this.settings.getLikeRemoveCost()))
                    {
                        this.addon.getAddonManager().removeStars(this.target, this.island, this.world);
                    }

                    if (this.parent != null)
                    {
                        this.parent.build();
                    }
                    else
                    {
                        user.closeInventory();
                    }
                }
            }
            else
            {
                if (this.hasPaid(this.settings.getLikeAddCost()))
                {
                    this.addon.getAddonManager().addStars(this.target, starsValue, this.island, this.world);
                }

                if (this.parent != null)
                {
                    this.parent.build();
                }
                else
                {
                    user.closeInventory();
                }
            }

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        builder.glow(hasStarred && value >= starsValue);

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }
        else if (hasStarred)
        {
            if (value == starsValue)
            {
                builder.description("");
                builder.description(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
            }
        }
        else
        {
            builder.description("");
            builder.description(this.user.getTranslation(Constants.TIPS + "click-to-set"));
        }

        return builder.build();
    }


    /**
     * This method process payment for likes and dislikes adding and removing.
     *
     * @param cost Cost of operation.
     * @return {@code true} if operation was successful, {@code false} - otherwise.
     */
    private boolean hasPaid(double cost)
    {
        if (this.user.isOp() ||
            !this.addon.isEconomyProvided() ||
            this.user.hasPermission(this.permissionPrefix + "likes.bypass-cost"))
        {
            return true;
        }

        if (this.addon.getVaultHook().has(this.user, cost))
        {
            this.addon.getVaultHook().withdraw(this.user, cost);
            return true;
        }
        else
        {
            Utils.sendMessage(this.user,
                this.user.getTranslation(Constants.ERRORS + "not-enough-money"));
            return false;
        }
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param addon Likes Addon object
     * @param user User who opens panel
     * @param world World where gui is opened
     * @param permissionPrefix Permission Prefix
     * @param islandId The id of island which likes should be managed.
     */
    public static void openPanel(LikesAddon addon, User user, World world, String permissionPrefix, Island islandId)
    {
        new LikesManagePanel(addon, user, world, permissionPrefix, islandId).build();
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param target User who adds like (equals user unless admin forces different)
     * @param islandId The id of island which likes should be managed.
     */
    public static void openPanel(CommonPanel parent, User target, Island islandId)
    {
        new LikesManagePanel(parent, target, islandId).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This variable allows to access addon settings object.
     */
    private final Settings settings;

    /**
     * This variable holds user who adds like. Different than user, if admin forces someone to add like/dislike/star.
     */
    private final User target;

    /**
     * This variable holds a likes object that need to be managed.
     */
    private final Island island;
}
