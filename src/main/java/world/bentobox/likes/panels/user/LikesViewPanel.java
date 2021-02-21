///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This gui shows all information about Likes Object.
 */
public class LikesViewPanel
{
    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param addon Likes object.
     * @param user User who opens Panel.
     * @param world World where gui is opened
     * @param permissionPrefix Permission Prefix
     * @param likesObject LikeObject that will be viewed.
     */
    private LikesViewPanel(@NonNull LikesAddon addon,
        @NonNull User user,
        @NonNull World world,
        String permissionPrefix,
        @NonNull LikesObject likesObject)
    {
        this.addon = addon;
        this.user = user;

        this.likesObject = likesObject;

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                this.likedByUsers = this.likesObject.getLikedBy().stream().
                    map(uuid -> this.addon.getPlayers().getName(uuid)).
                    sorted(String::compareToIgnoreCase).
                    collect(Collectors.toList());
                break;
            case LIKES_DISLIKES:
                this.likedByUsers = this.likesObject.getLikedBy().stream().
                    map(uuid -> this.addon.getPlayers().getName(uuid)).
                    sorted(String::compareToIgnoreCase).
                    collect(Collectors.toList());

                this.dislikedByUsers = this.likesObject.getDislikedBy().stream().
                    map(uuid -> this.addon.getPlayers().getName(uuid)).
                    sorted(String::compareToIgnoreCase).
                    collect(Collectors.toList());
                break;
            case STARS:
                this.likedByUsers = this.likesObject.getStarredBy().keySet().stream().
                    map(uuid -> this.addon.getPlayers().getName(uuid)).
                    sorted(String::compareToIgnoreCase).
                    collect(Collectors.toList());
                break;
        }

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                this.likeRank = this.addon.getAddonManager().getIslandRankByLikes(world, likesObject);
                break;
            case LIKES_DISLIKES:
                this.likeRank = this.addon.getAddonManager().getIslandRankByLikes(world, likesObject);
                this.dislikeRank = this.addon.getAddonManager().getIslandRankByDislikes(world, likesObject);
                this.overallRank = this.addon.getAddonManager().getIslandRankByRank(world, likesObject);
                break;
            case STARS:
                this.likeRank = this.addon.getAddonManager().getIslandRankByStars(world, likesObject);
                break;
        }

        this.hundredsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
        this.hundredsFormat.applyPattern("###.##");
    }


    private void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user);

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "view",
                    Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes")));
                this.buildLikesPanel(panelBuilder);
                break;
            case LIKES_DISLIKES:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "view",
                    Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes")));
                this.buildLikesDislikesPanel(panelBuilder);
                break;
            case STARS:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "view",
                    Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "stars")));
                this.buildStarsPanel(panelBuilder);
                break;
        }

        // At the end we just call build method that creates and opens panel.
        panelBuilder.build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method builds panel with only Likes in it.
     *
     * @param panelBuilder PanelBuilder that need to be populated.
     */
    private void buildLikesPanel(PanelBuilder panelBuilder)
    {
        GuiUtils.fillBorder(panelBuilder, 4, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(10, this.createButton(Button.LIKE));
        panelBuilder.item(11, this.createButton(Button.LIKE_RANK));

        this.populateLikers(panelBuilder);

        panelBuilder.item(35, this.createButton(Action.RETURN, true));
    }


    /**
     * This method builds panel with Likes, Dislikes and Rank in it.
     *
     * @param panelBuilder PanelBuilder that need to be populated.
     */
    private void buildLikesDislikesPanel(PanelBuilder panelBuilder)
    {
        GuiUtils.fillBorder(panelBuilder, 6, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(10, this.createButton(Button.LIKE));
        panelBuilder.item(11, this.createButton(Button.LIKE_RANK));

        panelBuilder.item(15, this.createButton(Button.OVERALL));
        panelBuilder.item(16, this.createButton(Button.OVERALL_RANK));

        this.populateLikers(panelBuilder);

        panelBuilder.item(28, this.createButton(Button.DISLIKE));
        panelBuilder.item(29, this.createButton(Button.DISLIKE_RANK));

        this.populateDislikers(panelBuilder);

        panelBuilder.item(53, this.createButton(Action.RETURN, true));
    }


    /**
     * This method builds panel with only Stars in it.
     *
     * @param panelBuilder PanelBuilder that need to be populated.
     */
    private void buildStarsPanel(PanelBuilder panelBuilder)
    {
        GuiUtils.fillBorder(panelBuilder, 4, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(10, this.createButton(Button.STARS));
        panelBuilder.item(11, this.createButton(Button.STARS_RANK));

        this.populateStars(panelBuilder);

        panelBuilder.item(35, this.createButton(Action.RETURN, true));
    }


    /**
     * This method creates PanelItem button based on given button type.
     *
     * @param button Button that must be created.
     * @return PanelItem object that represents given button.
     */
    private PanelItem createButton(Button button)
    {
        ItemStack icon;
        final String reference = Constants.BUTTONS + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");

        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(reference + ".description"));

        switch (button)
        {
            case LIKE:
            {
                icon = new ItemStack(Material.GOLD_INGOT);
                description.add(this.user.getTranslation(reference + ".likes",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.likesObject.getLikes())));
                break;
            }
            case LIKE_RANK:
            {
                icon = new ItemStack(Material.GOLD_BLOCK);

                if (this.likesObject.getLikes() == 0 && this.likesObject.getDislikes() == 0)
                {
                    description.add(this.user.getTranslation(reference + ".not-ranked"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".rank",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.likeRank)));
                }

                break;
            }
            case DISLIKE:
            {
                icon = new ItemStack(Material.IRON_INGOT);
                description.add(this.user.getTranslation(reference + ".dislikes",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.likesObject.getDislikes())));
                break;
            }
            case DISLIKE_RANK:
            {
                icon = new ItemStack(Material.IRON_BLOCK);

                if (this.likesObject.getLikes() == 0 && this.likesObject.getDislikes() == 0)
                {
                    description.add(this.user.getTranslation(reference + ".not-ranked"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".rank",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.dislikeRank)));
                }
                break;
            }
            case OVERALL:
            {
                icon = new ItemStack(Material.DIAMOND);
                description.add(this.user.getTranslation(reference + ".value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.likesObject.getRank())));
                break;
            }
            case OVERALL_RANK:
            {
                icon = new ItemStack(Material.DIAMOND_BLOCK);

                if (this.likesObject.getLikes() == 0 && this.likesObject.getDislikes() == 0)
                {
                    description.add(this.user.getTranslation(reference + ".not-ranked"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".rank",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.overallRank)));
                }
                break;
            }
            case STARS:
            {
                icon = new ItemStack(Material.NETHER_STAR);
                description.add(this.user.getTranslation(reference + ".stars",
                    Constants.PARAMETER_NUMBER, this.hundredsFormat.format(this.likesObject.getStarsValue())));
                break;
            }
            case STARS_RANK:
            {
                icon = new ItemStack(Material.BEACON);

                if (this.likesObject.getStars() == 0)
                {
                    description.add(this.user.getTranslation(reference + ".not-ranked"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".rank",
                        Constants.PARAMETER_NUMBER, String.valueOf(this.likeRank)));
                }

                break;
            }
            default:
                return null;
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            glow(false).
            build();
    }


    /**
     * This method creates PanelItem button based on given action type.
     *
     * @param button Action that must be created.
     * @param byLike Indicates if likes/dislikes number must be adjusted
     * @return PanelItem object that represents given action.
     */
    private PanelItem createButton(Action button, boolean byLike)
    {
        ItemStack icon;
        PanelItem.ClickHandler clickHandler;

        final String reference = Constants.BUTTONS + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");

        List<String> description = new ArrayList<>(3);
        int count;

        switch (button)
        {
            case NEXT:
            {
                count = byLike ? this.likeOffset + 2 : this.dislikeOffset + 2;

                icon = new ItemStack(Material.TIPPED_ARROW);
                description.add(this.user.getTranslationOrNothing(reference + ".description",
                    Constants.PARAMETER_NUMBER, String.valueOf(count)));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-next"));

                clickHandler = (panel, user, clickType, slot) ->
                {
                    if (byLike)
                    {
                        this.likeOffset++;
                    }
                    else
                    {
                        this.dislikeOffset++;
                    }
                    this.build();
                    return true;
                };

                break;
            }
            case PREVIOUS:
            {
                count = byLike ? this.likeOffset : this.dislikeOffset;

                icon = new ItemStack(Material.TIPPED_ARROW);
                description.add(this.user.getTranslationOrNothing(reference + ".description",
                    Constants.PARAMETER_NUMBER, String.valueOf(count)));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-previous"));

                clickHandler = (panel, user, clickType, slot) ->
                {
                    if (byLike)
                    {
                        this.likeOffset--;
                    }
                    else
                    {
                        this.dislikeOffset--;
                    }

                    this.build();
                    return true;
                };

                break;
            }
            case RETURN:
            {
                count = 1;

                icon = new ItemStack(Material.OAK_DOOR);
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-quit"));

                clickHandler = (panel, user, clickType, slot) -> {
                    user.closeInventory();
                    return true;
                };

                break;
            }
            default:
                return null;
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            amount(count).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method populates all likers into given panel builder.
     *
     * @param panelBuilder PanelBuilder object.
     */
    private void populateLikers(PanelBuilder panelBuilder)
    {
        if (this.likeOffset > 0)
        {
            panelBuilder.item(18, this.createButton(Action.PREVIOUS, true));
        }

        if ((this.likeOffset + 1) * 7 < this.likesObject.getLikes())
        {
            panelBuilder.item(26, this.createButton(Action.NEXT, true));
        }

        final int startIndex = this.likeOffset * 7;

        for (int index = 0; index < 7 && startIndex + index < this.likesObject.getLikes(); index++)
        {
            String userName = this.likedByUsers.get(startIndex + index);
            panelBuilder.item(19 + index, this.createUserButton(userName));
        }
    }


    /**
     * This method populates all dislikers into given panel builder.
     *
     * @param panelBuilder PanelBuilder object.
     */
    private void populateDislikers(PanelBuilder panelBuilder)
    {
        if (this.dislikeOffset > 0)
        {
            panelBuilder.item(36, this.createButton(Action.PREVIOUS, false));
        }

        if ((this.dislikeOffset + 1) * 7 < this.likesObject.getDislikes())
        {
            panelBuilder.item(44, this.createButton(Action.NEXT, false));
        }

        final int startIndex = this.dislikeOffset * 7;

        for (int index = 0; index < 7 && startIndex + index < this.likesObject.getDislikes(); index++)
        {
            String userName = this.dislikedByUsers.get(startIndex + index);

            panelBuilder.item(37 + index, this.createUserButton(userName));
        }
    }


    /**
     * This method populates all stars into given panel builder.
     *
     * @param panelBuilder PanelBuilder object.
     */
    private void populateStars(PanelBuilder panelBuilder)
    {
        if (this.likeOffset > 0)
        {
            panelBuilder.item(18, this.createButton(Action.PREVIOUS, true));
        }

        if ((this.likeOffset + 1) * 7 < this.likesObject.numberOfStars())
        {
            panelBuilder.item(26, this.createButton(Action.NEXT, true));
        }

        final int startIndex = this.likeOffset * 7;

        for (int index = 0, size = this.likesObject.numberOfStars();
            index < 7 && startIndex + index < size;
            index++)
        {
            String userName = this.likedByUsers.get(startIndex + index);

            panelBuilder.item(19 + index,
                this.createUserButton(userName,
                    this.likesObject.getStarredBy().get(this.addon.getPlayers().getUUID(userName))));
        }
    }


    /**
     * This method creates User Button.
     * @param userName Username who button must be created.
     * @return instance PanelItem
     */
    private PanelItem createUserButton(String userName)
    {
        return this.createUserButton(userName, 1);
    }


    /**
     * This method creates User Button.
     * @param userName Username who button must be created.
     * @param amount ItemStack amount.
     * @return instance PanelItem
     */
    private PanelItem createUserButton(String userName, int amount)
    {
        return new PanelItemBuilder().
            icon(userName).
            amount(amount).
            name(this.user.getTranslation(Constants.BUTTONS + "user.name", Constants.PARAMETER_NAME, userName)).
            build();
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param addon Likes Addon object
     * @param user User who opens panel
     * @param world World where gui is opened
     * @param permissionPrefix Permission Prefix
     * @param island Island which View panel must be opened.
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


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum holds all action buttons that can be added in current gui.
     */
    private enum Button
    {
        LIKE,
        LIKE_RANK,
        DISLIKE,
        DISLIKE_RANK,
        OVERALL,
        OVERALL_RANK,
        STARS,
        STARS_RANK,
    }


    /**
     * This enum holds action buttons in GUI.
     */
    private enum Action
    {
        NEXT,
        PREVIOUS,
        RETURN,
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
     * This variable holds likes object that is view by user in current gui.
     */
    private final LikesObject likesObject;

    /**
     * This variable stores index of current liker.
     */
    private int likeOffset;

    /**
     * This variable stores index of current disliker.
     */
    private int dislikeOffset;

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
