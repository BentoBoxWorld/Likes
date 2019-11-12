//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.panels.admin;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.panels.util.SelectUserGUI;
import world.bentobox.likes.utils.Constants;


/**
 * This gui shows all information about Likes Object.
 */
public class AdminViewPanel extends CommonPanel
{
    /**
     * This is internal constructor. It is used internally in current class to avoid
     * creating objects everywhere.
     * @param parent Parent Panel
     * @param island Island which View panel must be opened.
     */
    private AdminViewPanel(@NonNull CommonPanel parent,
            @NonNull Island island)
    {
        super(parent);

        // Create new object if it does not exist as admin is editing it.
        this.likesObject = this.addon.getManager().getIslandLikes(island.getUniqueId(), this.world);

        this.likedByUsers = this.likesObject.getLikedBy().stream().
                map(uuid -> this.addon.getPlayers().getName(uuid)).
                sorted(String::compareToIgnoreCase).
                collect(Collectors.toList());

        this.dislikedByUsers = this.likesObject.getDislikedBy().stream().
                map(uuid -> this.addon.getPlayers().getName(uuid)).
                sorted(String::compareToIgnoreCase).
                collect(Collectors.toList());

        this.likeRank = this.addon.getManager().getSortedLikes(this.world).entryIndex(this.likesObject) + 1L;
        this.dislikeRank = this.addon.getManager().getSortedDislikes(this.world).entryIndex(this.likesObject) + 1L;
        this.overallRank = this.addon.getManager().getSortedRank(this.world).entryIndex(this.likesObject) + 1L;
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier
     * to open panel with single method call then initializing new object.
     * @param parent Parent Panel
     * @param island Island which View panel must be opened.
     */
    public static void openPanel(@NonNull CommonPanel parent,
            @NonNull Island island)
    {
        new AdminViewPanel(parent, island).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
                name(this.user.getTranslation(Constants.TITLE + "edit-view")).
                user(this.user);

        GuiUtils.fillBorder(panelBuilder, 6, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(2, this.createButton(Button.ADD_LIKE_USER));
        panelBuilder.item(3, this.createButton(Button.REMOVE_LIKE_USER));

        panelBuilder.item(5, this.createButton(Button.ADD_DISLIKE_USER));
        panelBuilder.item(6, this.createButton(Button.REMOVE_DISLIKE_USER));

        panelBuilder.item(10, this.createButton(Button.LIKE));
        panelBuilder.item(11, this.createButton(Button.LIKE_RANK));

        panelBuilder.item(15, this.createButton(Button.OVERALL));
        panelBuilder.item(16, this.createButton(Button.OVERALL_RANK));

        this.populateLikers(panelBuilder);

        panelBuilder.item(28, this.createButton(Button.DISLIKE));
        panelBuilder.item(29, this.createButton(Button.DISLIKE_RANK));

        this.populateDislikers(panelBuilder);

        panelBuilder.item(53, this.returnButton);

        // At the end we just call build method that creates and opens panel.
        panelBuilder.build();
    }


    /**
     * This method creates PanelItem button based on given button type.
     * @param button Button that must be created.
     * @return PanelItem object that represents given button.
     */
    private PanelItem createButton(Button button)
    {
        ItemStack icon;
        String name;
        List<String> description;
        PanelItem.ClickHandler clickHandler;

        switch (button)
        {
        case LIKE:
        {
            icon = new ItemStack(Material.GOLD_INGOT);
            name = this.user.getTranslation(Constants.BUTTON + "like");

            description = new ArrayList<>(2);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "like"));
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
                    "[value]",
                    this.likesObject.getLikes() + ""));

            clickHandler = null;

            break;
        }
        case LIKE_RANK:
        {
            icon = new ItemStack(Material.GOLD_BLOCK);
            name = this.user.getTranslation(Constants.BUTTON + "like-rank");

            description = new ArrayList<>(2);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "like-rank"));
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
                    "[value]",
                    this.likeRank + ""));

            clickHandler = null;

            break;
        }
        case DISLIKE:
        {
            icon = new ItemStack(Material.IRON_INGOT);
            name = this.user.getTranslation(Constants.BUTTON + "dislike");

            description = new ArrayList<>(2);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike"));
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
                    "[value]",
                    this.likesObject.getDislikes() + ""));

            clickHandler = null;

            break;
        }
        case DISLIKE_RANK:
        {
            icon = new ItemStack(Material.IRON_BLOCK);
            name = this.user.getTranslation(Constants.BUTTON + "dislike-rank");

            description = new ArrayList<>(2);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike-rank"));
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
                    "[value]",
                    this.dislikeRank + ""));

            clickHandler = null;

            break;
        }
        case OVERALL:
        {
            icon = new ItemStack(Material.DIAMOND);
            name = this.user.getTranslation(Constants.BUTTON + "overall");

            description = new ArrayList<>(2);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "overall"));
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
                    "[value]",
                    this.likesObject.getRank() + ""));

            clickHandler = null;

            break;
        }
        case OVERALL_RANK:
        {
            icon = new ItemStack(Material.DIAMOND_BLOCK);
            name = this.user.getTranslation(Constants.BUTTON + "overall-rank");

            description = new ArrayList<>(2);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "overall-rank"));
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
                    "[value]",
                    this.overallRank + ""));

            clickHandler = null;

            break;
        }
        case NEXT_LIKE:
        {
            icon = new ItemStack(Material.OAK_SIGN);
            name = this.user.getTranslation(Constants.BUTTON + "next");
            description = new ArrayList<>(1);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "like-next"));

            clickHandler = (panel, user, clickType, slot) -> {
                this.likeOffset++;
                this.build();
                return true;
            };

            break;
        }
        case PREVIOUS_LIKE:
        {
            icon = new ItemStack(Material.OAK_SIGN);
            name = this.user.getTranslation(Constants.BUTTON + "previous");
            description = new ArrayList<>(1);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "like-previous"));

            clickHandler = (panel, user, clickType, slot) -> {
                this.likeOffset--;
                this.build();
                return true;
            };

            break;
        }
        case NEXT_DISLIKE:
        {
            icon = new ItemStack(Material.OAK_SIGN);
            name = this.user.getTranslation(Constants.BUTTON + "next");
            description = new ArrayList<>(1);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike-next"));

            clickHandler = (panel, user, clickType, slot) -> {
                this.dislikeOffset++;
                this.build();
                return true;
            };

            break;
        }
        case PREVIOUS_DISLIKE:
        {
            icon = new ItemStack(Material.OAK_SIGN);
            name = this.user.getTranslation(Constants.BUTTON + "previous");
            description = new ArrayList<>(1);
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike-previous"));

            clickHandler = (panel, user, clickType, slot) -> {
                this.dislikeOffset--;
                this.build();
                return true;
            };

            break;
        }
        case ADD_LIKE_USER:
        {
            icon = new ItemStack(Material.GOLD_NUGGET);
            name = this.user.getTranslation(Constants.BUTTON + "add-like-user");
            description = new ArrayList<>(1);

            clickHandler = (panel, user, clickType, slot) -> {
                SelectUserGUI.open(user,
                        this.getOnlineUserList(),
                        this.convertToUserList(this.likesObject.getLikedBy()),
                        player -> {
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
            icon = new ItemStack(Material.LAVA_BUCKET);
            name = this.user.getTranslation(Constants.BUTTON + "remove-like-user");
            description = new ArrayList<>(1);

            clickHandler = (panel, user, clickType, slot) -> {
                SelectUserGUI.open(user,
                        this.convertToUserList(this.likesObject.getLikedBy()),
                        player -> {
                            if (player != null)
                            {
                                this.likesObject.removeLike(player.getUniqueId());
                                this.likedByUsers.remove(player.getName());
                            }

                            this.build();
                        });
                return true;
            };

            break;
        }
        case ADD_DISLIKE_USER:
        {
            icon = new ItemStack(Material.IRON_NUGGET);
            name = this.user.getTranslation(Constants.BUTTON + "add-dislike-user");
            description = new ArrayList<>(1);

            clickHandler = (panel, user, clickType, slot) -> {
                SelectUserGUI.open(user,
                        this.getOnlineUserList(),
                        this.convertToUserList(this.likesObject.getDislikedBy()),
                        player -> {
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
            icon = new ItemStack(Material.LAVA_BUCKET);
            name = this.user.getTranslation(Constants.BUTTON + "remove-dislike-user");
            description = new ArrayList<>(1);

            clickHandler = (panel, user, clickType, slot) -> {
                SelectUserGUI.open(user,
                        this.convertToUserList(this.likesObject.getDislikedBy()),
                        player -> {
                            if (player != null)
                            {
                                this.likesObject.removeDislike(player.getUniqueId());
                                this.dislikedByUsers.remove(player.getName());
                            }

                            this.build();
                        });
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
                description(GuiUtils.stringSplit(description, 999)).
                glow(false).
                clickHandler(clickHandler).
                build();
    }


    /**
     * This method populates all likers into given panel builder.
     * @param panelBuilder PanelBuilder object.
     */
    private void populateLikers(PanelBuilder panelBuilder)
    {
        if (this.likeOffset > 0)
        {
            panelBuilder.item(18, this.createButton(Button.PREVIOUS_LIKE));
        }

        if ((this.likeOffset + 1) * 7 < this.likesObject.getLikes())
        {
            panelBuilder.item(26, this.createButton(Button.NEXT_LIKE));
        }

        final int startIndex = this.likeOffset * 7;

        for (int index = 0; index < 7 && startIndex + index < this.likesObject.getLikes(); index++)
        {
            String userName = this.likedByUsers.get(startIndex + index);

            panelBuilder.item(19 + index, new PanelItemBuilder().
                    icon(userName).
                    glow(false).
                    build());
        }
    }


    /**
     * This method populates all dislikers into given panel builder.
     * @param panelBuilder PanelBuilder object.
     */
    private void populateDislikers(PanelBuilder panelBuilder)
    {
        if (this.dislikeOffset > 0)
        {
            panelBuilder.item(36, this.createButton(Button.PREVIOUS_DISLIKE));
        }

        if ((this.dislikeOffset + 1) * 7 < this.likesObject.getDislikes())
        {
            panelBuilder.item(44, this.createButton(Button.NEXT_DISLIKE));
        }

        final int startIndex = this.dislikeOffset * 7;

        for (int index = 0; index < 7 && startIndex + index < this.likesObject.getDislikes(); index++)
        {
            String userName = this.dislikedByUsers.get(startIndex + index);

            panelBuilder.item(37 + index, new PanelItemBuilder().
                    icon(userName).
                    glow(false).
                    build());
        }
    }


    /**
     * This method returns list that contains all online users.
     * @return Online User List.
     */
    private List<User> getOnlineUserList()
    {
        return Bukkit.getOnlinePlayers().stream().map(User::getInstance).collect(Collectors.toList());
    }


    /**
     * This method converts and returns given set with UUIDs to User list.
     * @param userSet Set that must be converted.
     * @return Converted User List.
     */
    private List<User> convertToUserList(Set<UUID> userSet)
    {
        return userSet.stream().map(User::getInstance).collect(Collectors.toList());
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

        NEXT_LIKE,
        PREVIOUS_LIKE,

        NEXT_DISLIKE,
        PREVIOUS_DISLIKE,

        ADD_LIKE_USER,
        REMOVE_LIKE_USER,
        ADD_DISLIKE_USER,
        REMOVE_DISLIKE_USER
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
    private int likeOffset;

    /**
     * This variable stores index of current disliker.
     */
    private int dislikeOffset;

    /**
     * This list contains player names that liked current island.
     */
    private final List<String> likedByUsers;

    /**
     * This list contains player names that disliked current island.
     */
    private final List<String> dislikedByUsers;

    /**
     * This variable holds island rank by likes.
     */
    private final long likeRank;

    /**
     * This variable holds island rank by dislikes.
     */
    private final long dislikeRank;

    /**
     * This variable holds island rank by rank.
     */
    private final long overallRank;
}
