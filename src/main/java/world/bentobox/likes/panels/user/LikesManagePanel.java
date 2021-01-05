///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
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
        PanelBuilder panelBuilder = new PanelBuilder().
            type(Panel.Type.HOPPER).
            user(this.user);

        switch (this.settings.getMode())
        {
            case LIKES:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "manage",
                    Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes")));

                panelBuilder.item(2, this.createLikeButton());
                break;
            case LIKES_DISLIKES:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "manage",
                    Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes")));

                panelBuilder.item(1, this.createLikeButton());
                panelBuilder.item(3, this.createDislikeButton());
                break;
            case STARS:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "manage",
                    Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "stars")));

                final int starCount = this.addon.getAddonManager().getStarred(
                    this.target.getUniqueId(),
                    this.island.getUniqueId(),
                    this.world);

                if (starCount == 0 || starCount > 5)
                {
                    panelBuilder.item(0, this.createStarsButton(1, false));
                    panelBuilder.item(1, this.createStarsButton(2, false));
                    panelBuilder.item(2, this.createStarsButton(3, false));
                    panelBuilder.item(3, this.createStarsButton(4, false));
                    panelBuilder.item(4, this.createStarsButton(5, false));
                }
                else
                {
                    panelBuilder.item(starCount - 1, this.createStarsButton(starCount, true));
                }
        }

        // At the end we just call build method that creates and opens panel.
        panelBuilder.build();
    }


    /**
     * This method creates Like Button.
     *
     * @return PanelItem that process like action.
     */
    private PanelItem createLikeButton()
    {
        final boolean hasLiked =
            this.addon.getAddonManager().hasLiked(this.target.getUniqueId(), this.island.getUniqueId(), this.world);

        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(Constants.BUTTONS + "add_like.description"));

        if (this.addon.isEconomyProvided())
        {
            if (hasLiked && this.settings.getLikeRemoveCost() > 0)
            {
                description.add(this.user.getTranslation(Constants.BUTTONS + "add_like.cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeRemoveCost())));
            }
            else if (!hasLiked && this.settings.getLikeAddCost() > 0)
            {
                description.add(this.user.getTranslation(Constants.BUTTONS + "add_like.cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeAddCost())));
            }
        }

        description.add("");

        if (hasLiked)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
        }

        return new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTONS + "add_like.name")).
            icon(Material.GOLD_INGOT).
            description(description).
            clickHandler((panel, user, clickType, slot) ->
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

                return true;
            }).
            glow(hasLiked).
            build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method creates dislike Button.
     *
     * @return PanelItem that process dislike action.
     */
    private PanelItem createDislikeButton()
    {
        final boolean hasDisliked =
            this.addon.getAddonManager().hasDisliked(this.target.getUniqueId(), this.island.getUniqueId(), this.world);

        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(Constants.BUTTONS + "add_dislike.description"));

        if (this.addon.isEconomyProvided())
        {
            if (hasDisliked && this.settings.getDislikeRemoveCost() > 0)
            {
                description.add(this.user.getTranslation(Constants.BUTTONS + "add_dislike.cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getDislikeRemoveCost())));
            }
            else if (!hasDisliked && this.settings.getDislikeAddCost() > 0)
            {
                description.add(this.user.getTranslation(Constants.BUTTONS + "add_dislike.cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getDislikeAddCost())));
            }
        }

        description.add("");

        if (hasDisliked)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
        }

        return new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTONS + "add_dislike.name")).
            icon(Material.IRON_INGOT).
            description(description).
            clickHandler((panel, user, clickType, slot) ->
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

                return true;
            }).
            glow(hasDisliked).
            build();
    }


    /**
     * This method creates Stars Button with given value.
     *
     * @param value value for stars button.
     * @param hasStarred indicates that player has added star to this island.
     * @return PanelItem that process stars action.
     */
    private PanelItem createStarsButton(int value, boolean hasStarred)
    {
        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(Constants.BUTTONS + "add_star.description"));

        if (this.addon.isEconomyProvided())
        {
            if (hasStarred && this.settings.getLikeAddCost() > 0)
            {
                description.add(this.user.getTranslation(Constants.BUTTONS + "add_star.cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeAddCost())));
            }
            else if (!hasStarred && this.settings.getLikeRemoveCost() > 0)
            {
                description.add(this.user.getTranslation(Constants.BUTTONS + "add_star.cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeRemoveCost())));
            }
        }

        description.add("");

        if (hasStarred)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-set"));
        }

        return new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTONS + "add_star.name",
                Constants.PARAMETER_NUMBER, String.valueOf(value))).
            icon(new ItemStack(Material.NETHER_STAR, value)).
            description(description).
            clickHandler((panel, user, clickType, slot) -> {

                if (hasStarred)
                {
                    if (this.hasPaid(this.settings.getLikeRemoveCost()))
                    {
                        this.addon.getAddonManager().removeStars(this.target, this.island, this.world);
                    }
                }
                else
                {
                    if (this.hasPaid(this.settings.getLikeAddCost()))
                    {
                        this.addon.getAddonManager().addStars(this.target, value, this.island, this.world);
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

                return true;
            }).
            glow(hasStarred).
            build();
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
