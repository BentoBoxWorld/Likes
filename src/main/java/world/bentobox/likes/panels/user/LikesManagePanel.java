///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import java.io.File;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
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
    private static final String MANAGE_PANELS = "manage_panels";
    private static final String PANELS = "panels";
    private static final String ADD_LIKE = "ADD_LIKE";
    private static final String DESCRIPTION = "description";
    private static final String CLICK_TO_REMOVE = "click-to-remove";
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
        this.settings = Objects.requireNonNull(this.addon, "addon").getSettings();
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
                panelBuilder.template("likes", MANAGE_PANELS, new File(this.addon.getDataFolder(), PANELS));

                panelBuilder.registerTypeBuilder(ADD_LIKE, this::createLikeButton);
            }
            case LIKES_DISLIKES -> {
                panelBuilder.template("likes_dislikes", MANAGE_PANELS, new File(this.addon.getDataFolder(), PANELS));

                panelBuilder.registerTypeBuilder(ADD_LIKE, this::createLikeButton);
                panelBuilder.registerTypeBuilder("ADD_DISLIKE", this::createDislikeButton);
            }
            case STARS -> {
                panelBuilder.template("stars", MANAGE_PANELS,  new File(this.addon.getDataFolder(), PANELS));

                final int starCount = this.addon.getAddonManager().getStarred(
                    this.target.getUniqueId(),
                    this.island.getUniqueId(),
                    this.world);

                panelBuilder.registerTypeBuilder("ADD_STAR",
                    (template, itemSlot) -> this.createStarButton(template, starCount));
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

        return this.createVoteButton(template,
            Constants.BUTTONS + "add_like.",
            Material.GOLD_INGOT,
            hasLiked,
            new VoteAction(ADD_LIKE,
                this.settings::getLikeAddCost,
                () -> this.addon.getAddonManager().addLike(this.target, this.island, this.world)),
            new VoteAction("REMOVE_LIKE",
                this.settings::getLikeRemoveCost,
                () -> this.addon.getAddonManager().removeLike(this.target, this.island, this.world)));
    }


    private PanelItem createDislikeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        final boolean hasDisliked =
            this.addon.getAddonManager().hasDisliked(this.target.getUniqueId(), this.island.getUniqueId(), this.world);

        return this.createVoteButton(template,
            Constants.BUTTONS + "add_dislike.",
            Material.IRON_INGOT,
            hasDisliked,
            new VoteAction("ADD_DISLIKE",
                this.settings::getDislikeAddCost,
                () -> this.addon.getAddonManager().addDislike(this.target, this.island, this.world)),
            new VoteAction("REMOVE_DISLIKE",
                this.settings::getDislikeRemoveCost,
                () -> this.addon.getAddonManager().removeDislike(this.target, this.island, this.world)));
    }


    /**
     * This method creates like or dislike button.
     *
     * @param template Template of the button.
     * @param reference Translation reference of the button.
     * @param defaultIcon Icon that is used if template does not have one.
     * @param hasVoted Indicates that target user already has liked or disliked the island.
     * @param add Action that adds the vote.
     * @param remove Action that removes the vote.
     * @return PanelItem object that represents the button.
     */
    private PanelItem createVoteButton(ItemTemplateRecord template,
        String reference,
        Material defaultIcon,
        boolean hasVoted,
        VoteAction add,
        VoteAction remove)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            builder.icon(defaultIcon);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }
        else
        {
            builder.name(this.user.getTranslation(reference + "name"));
        }

        this.addDescription(builder, template, reference);
        this.addCost(builder, reference, hasVoted ? remove.cost().getAsDouble() : add.cost().getAsDouble());

        List<ItemTemplateRecord.ActionRecords> activeActions =
            this.getActiveActions(template, add.type(), remove.type(), hasVoted);

        // Add ClickHandler
        builder.clickHandler((panel, clicker, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : activeActions)
            {
                if (clickType == action.clickType() || action.clickType() == ClickType.UNKNOWN)
                {
                    VoteAction vote = hasVoted ? remove : add;

                    if (this.hasPaid(vote.cost().getAsDouble()))
                    {
                        vote.action().run();
                    }

                    this.returnToParent(clicker);
                }
            }

            // Always return true.
            return true;
        });

        builder.glow(hasVoted);

        if (!this.addTooltips(builder, activeActions, template.actions().size()))
        {
            builder.description("");
            builder.description(this.user.getTranslation(Constants.TIPS +
                (hasVoted ? CLICK_TO_REMOVE : "click-to-add")));
        }

        return builder.build();
    }


    private PanelItem createStarButton(ItemTemplateRecord template, int value)
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
                Constants.PARAMETER_NUMBER, String.valueOf(starsValue)));
        }
        else
        {
            builder.name(this.user.getTranslation(reference + "name",
                Constants.PARAMETER_NUMBER, String.valueOf(starsValue)));
        }

        this.addDescription(builder, template, reference);
        // Displayed costs are kept as they always were: add cost for starred islands, remove cost otherwise.
        this.addCost(builder,
            reference,
            hasStarred ? this.settings.getLikeAddCost() : this.settings.getLikeRemoveCost());

        List<ItemTemplateRecord.ActionRecords> activeActions =
            this.getActiveActions(template, "ADD_STAR", "REMOVE_STAR", hasStarred);

        // Stars can be changed only by removing the current value first.
        final boolean canClick = !hasStarred || value == starsValue;

        // Add ClickHandler
        builder.clickHandler((panel, clicker, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : activeActions)
            {
                if (canClick && (clickType == action.clickType() || action.clickType() == ClickType.UNKNOWN))
                {
                    this.changeStars(hasStarred, starsValue);
                    this.returnToParent(clicker);
                }
            }

            // Always return true.
            return true;
        });

        builder.glow(hasStarred && value >= starsValue);

        if (!this.addTooltips(builder, activeActions, template.actions().size()) && canClick)
        {
            builder.description("");
            builder.description(this.user.getTranslation(Constants.TIPS +
                (hasStarred ? CLICK_TO_REMOVE : "click-to-set")));
        }

        return builder.build();
    }


    /**
     * This method removes stars of the target user, or adds given amount of them.
     *
     * @param hasStarred Indicates that target user already has starred the island.
     * @param starsValue Amount of stars to add.
     */
    private void changeStars(boolean hasStarred, int starsValue)
    {
        if (hasStarred)
        {
            if (this.hasPaid(this.settings.getLikeRemoveCost()))
            {
                this.addon.getAddonManager().removeStars(this.target, this.island, this.world);
            }
        }
        else if (this.hasPaid(this.settings.getLikeAddCost()))
        {
            this.addon.getAddonManager().addStars(this.target, starsValue, this.island, this.world);
        }
    }


    /**
     * This method adds description from the template or from the default translation.
     */
    private void addDescription(PanelItemBuilder builder, ItemTemplateRecord template, String reference)
    {
        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.user.getTranslationOrNothing(reference + DESCRIPTION));
        }
    }


    /**
     * This method adds cost to the description, if economy is enabled and action is not free.
     */
    private void addCost(PanelItemBuilder builder, String reference, double cost)
    {
        if (this.addon.isEconomyProvided() && cost > 0)
        {
            builder.description(this.user.getTranslation(reference + "cost",
                Constants.PARAMETER_NUMBER, String.valueOf(cost)));
        }
    }


    /**
     * This method returns template actions without the one that is not possible in the current state.
     */
    private List<ItemTemplateRecord.ActionRecords> getActiveActions(ItemTemplateRecord template,
        String addAction,
        String removeAction,
        boolean hasVoted)
    {
        // Get only possible actions, by removing all inactive ones.
        List<ItemTemplateRecord.ActionRecords> activeActions = new ArrayList<>(template.actions());
        final String inactiveAction = hasVoted ? addAction : removeAction;

        activeActions.removeIf(action -> inactiveAction.equals(action.actionType().toUpperCase()));

        return activeActions;
    }


    /**
     * This method adds tooltips of the active actions to the description.
     *
     * @return {@code true} if any tooltip was added, {@code false} otherwise.
     */
    private boolean addTooltips(PanelItemBuilder builder,
        List<ItemTemplateRecord.ActionRecords> activeActions,
        int actionCount)
    {
        List<String> tooltips = activeActions.stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(actionCount)));

        if (tooltips.isEmpty())
        {
            return false;
        }

        // Empty line and tooltips.
        builder.description("");
        builder.description(tooltips);

        return true;
    }


    /**
     * This method returns to the parent panel, or closes inventory if there is no parent.
     */
    private void returnToParent(User clicker)
    {
        if (this.parent != null)
        {
            this.parent.build();
        }
        else
        {
            clicker.closeInventory();
        }
    }


    /**
     * This record stores what happens when a like or a dislike is added or removed.
     *
     * @param type Type of the action in the panel template.
     * @param cost Cost of the action.
     * @param action Code that changes the vote.
     */
    private record VoteAction(String type, DoubleSupplier cost, Runnable action)
    {
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
