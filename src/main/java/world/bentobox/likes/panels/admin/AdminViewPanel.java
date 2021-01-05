///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.panels.admin;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.panels.user.LikesManagePanel;
import world.bentobox.likes.panels.util.SelectUserPanel;
import world.bentobox.likes.utils.Constants;


/**
 * This gui shows all information about Likes Object.
 */
public class AdminViewPanel extends CommonPanel
{
    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param parent Parent Panel
     * @param island Island which View panel must be opened.
     */
    private AdminViewPanel(@NonNull CommonPanel parent,
        @NonNull Island island)
    {
        super(parent);

        this.island = island;

        // Create new object if it does not exist as admin is editing it.
        this.likesObject = this.addon.getAddonManager().getIslandLikes(island.getUniqueId(), this.world);

        switch (this.getMode())
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
                this.likeRank = this.addon.getAddonManager().getIslandRankByLikes(this.world, this.likesObject);
                break;
            case LIKES_DISLIKES:
                this.likeRank = this.addon.getAddonManager().getIslandRankByLikes(this.world, this.likesObject);
                this.dislikeRank = this.addon.getAddonManager().getIslandRankByDislikes(this.world, this.likesObject);
                this.overallRank = this.addon.getAddonManager().getIslandRankByRank(this.world, this.likesObject);
                break;
            case STARS:
                this.likeRank = this.addon.getAddonManager().getIslandRankByStars(this.world, this.likesObject);
                break;
        }
    }


    @Override
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user);

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "edit-view",
                    Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes")));

                this.buildLikesPanel(panelBuilder);
                break;
            case LIKES_DISLIKES:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "edit-view",
                    Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes")));

                this.buildLikesDislikesPanel(panelBuilder);
                break;
            case STARS:
                panelBuilder.name(this.user.getTranslation(Constants.TITLES + "edit-view",
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
     * This method builds Likes admin panel.
     *
     * @param panelBuilder PanelBuilder that need to be populated.
     */
    private void buildLikesPanel(PanelBuilder panelBuilder)
    {
        GuiUtils.fillBorder(panelBuilder, 4, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(2, this.createButton(Button.ADD_LIKE_USER));
        panelBuilder.item(3, this.createButton(Button.REMOVE_LIKE_USER));

        panelBuilder.item(10, this.createButton(Icon.LIKE));
        panelBuilder.item(11, this.createButton(Icon.LIKE_RANK));

        this.populateLikers(panelBuilder);

        panelBuilder.item(35, this.returnButton);
    }


    /**
     * This method builds Likes and Dislikes admin panel.
     *
     * @param panelBuilder PanelBuilder that need to be populated.
     */
    private void buildLikesDislikesPanel(PanelBuilder panelBuilder)
    {
        GuiUtils.fillBorder(panelBuilder, 6, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(2, this.createButton(Button.ADD_LIKE_USER));
        panelBuilder.item(3, this.createButton(Button.REMOVE_LIKE_USER));

        panelBuilder.item(5, this.createButton(Button.ADD_DISLIKE_USER));
        panelBuilder.item(6, this.createButton(Button.REMOVE_DISLIKE_USER));

        panelBuilder.item(10, this.createButton(Icon.LIKE));
        panelBuilder.item(11, this.createButton(Icon.LIKE_RANK));

        panelBuilder.item(15, this.createButton(Icon.OVERALL));
        panelBuilder.item(16, this.createButton(Icon.OVERALL_RANK));

        this.populateLikers(panelBuilder);

        panelBuilder.item(28, this.createButton(Icon.DISLIKE));
        panelBuilder.item(29, this.createButton(Icon.DISLIKE_RANK));

        this.populateDislikers(panelBuilder);

        panelBuilder.item(53, this.returnButton);
    }


    /**
     * This method builds Stars admin panel.
     *
     * @param panelBuilder PanelBuilder that need to be populated.
     */
    private void buildStarsPanel(PanelBuilder panelBuilder)
    {
        GuiUtils.fillBorder(panelBuilder, 4, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(2, this.createButton(Button.ADD_STAR_USER));
        panelBuilder.item(3, this.createButton(Button.REMOVE_STAR_USER));

        panelBuilder.item(10, this.createButton(Icon.STARS));
        panelBuilder.item(11, this.createButton(Icon.STARS_RANK));

        this.populateStars(panelBuilder);

        panelBuilder.item(35, this.returnButton);
    }


    /**
     * This method creates PanelItem button based on given button type.
     *
     * @param button Button that must be created.
     * @return PanelItem object that represents given button.
     */
    private PanelItem createButton(Icon button)
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
     * This method creates PanelItem button based on given button type.
     *
     * @param button Button that must be created.
     * @return PanelItem object that represents given button.
     */
    private PanelItem createButton(Button button)
    {
        ItemStack icon;
        PanelItem.ClickHandler clickHandler;

        final String reference = Constants.BUTTONS + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");

        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(reference + ".description"));

        switch (button)
        {
            case ADD_LIKE_USER:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));

                icon = new ItemStack(Material.GOLD_NUGGET);

                clickHandler = (panel, user, clickType, slot) ->
                {
                    SelectUserPanel.open(user,
                        this.getUserList(),
                        this.convertToUserList(this.likesObject.getLikedBy()),
                        player ->
                        {
                            if (player != null)
                            {
                                if (this.likesObject.hasDisliked(player.getUniqueId()))
                                {
                                    this.dislikedByUsers.remove(player.getName());
                                }

                                this.likesObject.addLike(player.getUniqueId());
                                this.likedByUsers.add(player.getName());
                            }

                            this.build();
                        });
                    return true;
                };

                break;
            }
            case REMOVE_LIKE_USER:
            {
                final boolean canRemove = !this.likesObject.getLikedBy().isEmpty();

                // Add tooltip
                if (canRemove)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    icon = new ItemStack(Material.LAVA_BUCKET);
                }
                else
                {
                    icon = new ItemStack(Material.BARRIER);
                }

                clickHandler = (panel, user, clickType, slot) ->
                {
                    // Open GUI only if someone has liked this island
                    if (canRemove)
                    {
                        SelectUserPanel.open(user,
                            this.convertToUserList(this.likesObject.getLikedBy()),
                            player ->
                            {
                                if (player != null)
                                {
                                    this.likesObject.removeLike(player.getUniqueId());
                                    this.likedByUsers.remove(player.getName());
                                }

                                this.build();
                            });
                    }
                    return true;
                };

                break;
            }
            case ADD_DISLIKE_USER:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));

                icon = new ItemStack(Material.IRON_NUGGET);

                clickHandler = (panel, user, clickType, slot) ->
                {
                    SelectUserPanel.open(user,
                        this.getUserList(),
                        this.convertToUserList(this.likesObject.getDislikedBy()),
                        player ->
                        {
                            if (player != null)
                            {
                                if (this.likesObject.hasLiked(player.getUniqueId()))
                                {
                                    this.likedByUsers.remove(player.getName());
                                }

                                this.likesObject.addDislike(player.getUniqueId());
                                this.dislikedByUsers.add(player.getName());
                            }

                            this.build();
                        });
                    return true;
                };

                break;
            }
            case REMOVE_DISLIKE_USER:
            {
                final boolean canRemove = !this.likesObject.getDislikedBy().isEmpty();

                // Add tooltip
                if (canRemove)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    icon = new ItemStack(Material.LAVA_BUCKET);
                }
                else
                {
                    icon = new ItemStack(Material.BARRIER);
                }

                clickHandler = (panel, user, clickType, slot) ->
                {
                    if (canRemove)
                    {
                        SelectUserPanel.open(user,
                            this.convertToUserList(this.likesObject.getDislikedBy()),
                            player ->
                            {
                                if (player != null)
                                {
                                    this.likesObject.removeDislike(player.getUniqueId());
                                    this.dislikedByUsers.remove(player.getName());
                                }

                                this.build();
                            });
                    }

                    return true;
                };

                break;
            }
            case ADD_STAR_USER:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));

                icon = new ItemStack(Material.NETHER_STAR);

                clickHandler = (panel, user, clickType, slot) ->
                {
                    SelectUserPanel.open(user,
                        this.getUserList(),
                        this.convertToUserList(this.likesObject.getStarredBy().keySet()),
                        player ->
                        {
                            if (player != null)
                            {
                                LikesManagePanel.openPanel(this, player, this.island);
                                this.likedByUsers.add(player.getName());
                            }
                            else
                            {
                                this.build();
                            }
                        });
                    return true;
                };

                break;
            }
            case REMOVE_STAR_USER:
            {
                final boolean canRemove = !this.likesObject.getStarredBy().isEmpty();

                // Add tooltip
                if (canRemove)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    icon = new ItemStack(Material.LAVA_BUCKET);
                }
                else
                {
                    icon = new ItemStack(Material.BARRIER);
                }

                clickHandler = (panel, user, clickType, slot) ->
                {
                    if (canRemove)
                    {
                        SelectUserPanel.open(user,
                            this.convertToUserList(this.likesObject.getStarredBy().keySet()),
                            player ->
                            {
                                if (player != null)
                                {
                                    this.likesObject.removeStars(player.getUniqueId());
                                    this.likedByUsers.remove(player.getName());
                                }

                                this.build();
                            });
                    }
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
            glow(false).
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
     * This method returns list that contains all online users.
     *
     * @return Online User List.
     */
    private List<User> getUserList()
    {
        return this.addon.getPlayers().getPlayers().stream().
            map(players -> User.getInstance(players.getPlayerUUID())).
            collect(Collectors.toList());
    }


    /**
     * This method converts and returns given set with UUIDs to User list.
     *
     * @param userSet Set that must be converted.
     * @return Converted User List.
     */
    private List<User> convertToUserList(Set<UUID> userSet)
    {
        return userSet.stream().map(User::getInstance).collect(Collectors.toList());
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param parent Parent Panel
     * @param island Island which View panel must be opened.
     */
    public static void openPanel(@NonNull CommonPanel parent,
        @NonNull Island island)
    {
        new AdminViewPanel(parent, island).build();
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum holds all icon buttons that can be added in current gui.
     */
    private enum Icon
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
     * This enum holds all clickable buttons that can be added in current gui.
     */
    private enum Button
    {
        ADD_LIKE_USER,
        REMOVE_LIKE_USER,
        ADD_DISLIKE_USER,
        REMOVE_DISLIKE_USER,
        ADD_STAR_USER,
        REMOVE_STAR_USER
    }


    /**
     * This enum holds all action buttons that can be added in current gui.
     */
    private enum Action
    {
        NEXT,
        PREVIOUS
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This variable holds likes object that is view by user in current gui.
     */
    private final LikesObject likesObject;

    /**
     * This variable holds targeted island by current panel.
     */
    private final Island island;

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
}
