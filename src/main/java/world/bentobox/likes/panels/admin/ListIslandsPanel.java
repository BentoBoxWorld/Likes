///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.panels.admin;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.*;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.metadata.MetaDataValue;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.CommonPagedPanel;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.panels.util.SingleBlockSelector;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This class contains all necessary things that allows to select single island from all existing in given world.
 */
public class ListIslandsPanel extends CommonPagedPanel<Island>
{
    /**
     * Default constructor.
     *
     * @param parent Parent Panel
     * @param type Action type
     */
    private ListIslandsPanel(CommonPanel parent, Type type)
    {
        super(parent);

        this.iconPermission = this.permissionPrefix + "likes.icon";

        this.activeFilter = Filter.IS_ONLINE;

        this.filterElements = this.addon.getIslands().getIslands(this.world).stream().
            filter(island -> island.isOwned() || island.isSpawn()).
            sorted(ISLAND_COMPARATOR).
            distinct().
            collect(Collectors.toList());

        this.updateFilters();

        this.type = type;
    }


    /**
     * This method builds all necessary elements in GUI panel.
     */
    @Override
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLES + "select-island"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        panelBuilder.item(3, this.createButton(Filter.IS_ONLINE));
        panelBuilder.item(4, this.createButton(Filter.HAS_DATA));
        panelBuilder.item(5, this.createButton(Filter.ALL_ISLANDS));

        // Populate islands
        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    /**
     * This method updates element list.
     */
    protected void updateFilters()
    {
        switch (this.activeFilter)
        {
            case IS_ONLINE -> this.filterElements = Bukkit.getOnlinePlayers().stream().
                map(player -> this.addon.getIslands().getIsland(this.world, player.getUniqueId())).
                filter(Objects::nonNull).
                distinct().
                collect(Collectors.toList());
            case HAS_DATA -> this.filterElements = this.addon.getIslands().getIslands(this.world).stream().
                filter(island -> this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId()) != null).
                distinct().
                collect(Collectors.toList());
            case ALL_ISLANDS -> this.filterElements = this.addon.getIslands().getIslands(this.world).stream().
                filter(Island::isOwned).
                distinct().
                collect(Collectors.toList());
            default -> this.filterElements = Collections.emptyList();
        }

        if (this.searchString != null && !this.searchString.isEmpty())
        {
            // Search through filtered elements.
            this.filterElements = this.searchElements(this.filterElements);
        }

        // Sort by name.
        this.filterElements.sort(ISLAND_COMPARATOR);
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method filters out islands that do not contains search field.
     *
     * @param islandCollection Collection of the islands from which it should search.
     * @return List of Islands that contains searched field.
     */
    private List<Island> searchElements(Collection<Island> islandCollection)
    {
        return islandCollection.stream().
            filter(island ->
            {
                // If island name is set and name contains search field, then do not filter out.
                if (island.getName() != null &&
                    !island.getName().isEmpty() &&
                    island.getName().toLowerCase().contains(this.searchString.toLowerCase()))
                {
                    return true;
                }

                // If island member names do not contains search field, then filter it out.
                for (UUID uuid : island.getMemberSet())
                {
                    User member = User.getInstance(uuid);

                    if (member.isPlayer() &&
                        member.getName().toLowerCase().contains(this.searchString.toLowerCase()))
                    {
                        return true;
                    }
                }

                // Island do not contains filter field.
                return false;
            }).
            distinct().
            collect(Collectors.toList());
    }


    /**
     * This method creates PanelItem that represents given island.
     *
     * @param island island which icon must be created.
     * @return PanelItem that represents given island.
     */
    @Override
    protected PanelItem createElementButton(Island island)
    {
        if (island == null)
        {
            // Player do not have an island.
            return PanelItem.empty();
        }

        LikesObject likesObject =
            this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId());

        User owner = island.getOwner() == null ? null : User.getInstance(island.getOwner());

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
                String name = this.addon.getPlayers().getName(uuid);

                if (memberBuilder.length() > 0)
                {
                    memberBuilder.append("\n");
                }

                memberBuilder.append(
                    this.user.getTranslationOrNothing(Constants.BUTTONS + "island.member",
                        Constants.PARAMETER_PLAYER, name));
            }

            memberText = memberBuilder.toString();
        }
        else
        {
            memberText = "";
        }

        // Get Place Text
        String placeText = "";

        if (likesObject != null)
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
                    int byStars = this.addon.getAddonManager().getIslandRankByStars(this.world, likesObject);

                    if (byStars != -1)
                    {
                        placeText = this.user.getTranslation(reference + "place",
                            Constants.PARAMETER_NUMBER, String.valueOf(byStars),
                            Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "stars"));
                    }
                }
            }
        }

        // Get Numbers Text
        String numbersText = switch (this.addon.getSettings().getMode()) {
            case LIKES -> this.user.getTranslation(reference + "numbers_likes",
                Constants.PARAMETER_LIKES, String.valueOf(likesObject == null ? 0 : likesObject.getLikes()));
            case LIKES_DISLIKES -> this.user.getTranslation(reference + "numbers_likes_dislikes",
                Constants.PARAMETER_LIKES, String.valueOf(likesObject == null ? 0 : likesObject.getLikes()),
                Constants.PARAMETER_DISLIKES, String.valueOf(likesObject == null ? 0 : likesObject.getDislikes()),
                Constants.PARAMETER_RANK, String.valueOf(likesObject == null ? 0 : likesObject.getRank()));
            case STARS -> this.user.getTranslation(reference + "numbers_stars",
                Constants.PARAMETER_STARS,
                this.hundredsFormat.format(likesObject == null ? 0 : likesObject.getStarsValue()),
                Constants.PARAMETER_NUMBER,
                String.valueOf(likesObject == null ? 0 : likesObject.numberOfStars()));
        };

        // Now combine everything.
        String descriptionText = this.user.getTranslation(reference + "description",
            Constants.PARAMETER_OWNER, ownerText,
            Constants.PARAMETER_MEMBERS, memberText,
            Constants.PARAMETER_PLACE, placeText,
            Constants.PARAMETER_NUMBERS, numbersText);
        List<String> description = Arrays.stream(descriptionText.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
            split("\n")).
            collect(Collectors.toList());

        description.add("");

        switch (this.type)
        {
            case MANAGE -> {
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));
                if (this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId()) != null)
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-remove"));
                }
            }
            case ICON -> description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, slot) ->
        {
            switch (this.type)
            {
                case MANAGE -> {
                    if (clickType.isShiftClick())
                    {
                        this.addon.getAddonManager().removeObject(island.getUniqueId());
                        this.build();
                    }
                    else
                    {
                        AdminViewPanel.openPanel(this, island);
                    }
                }
                case ICON -> {
                    SingleBlockSelector.open(this.user,
                        SingleBlockSelector.Mode.ANY,
                        (status, block) ->
                        {
                            if (status)
                            {
                                island.putMetaData(Constants.METADATA_ICON,
                                    new MetaDataValue(block.name()));
                            }

                            this.build();
                        });
                }
            }

            return true;
        };

        Material material;
        String permission = Utils.getPermissionValue(owner, this.iconPermission, null);

        if (permission != null && !permission.equals("*"))
        {
            material = Material.matchMaterial(permission);
        }
        else
        {
            material = island.getMetaData().
                map(map -> map.get(Constants.METADATA_ICON)).
                map(metaDataValue -> Material.matchMaterial(metaDataValue.asString())).
                orElseGet(() -> likesObject == null ? Material.PAPER : Material.WRITTEN_BOOK);
        }

        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(nameText).
            description(description).
            clickHandler(clickHandler);

        if (Material.PLAYER_HEAD.equals(material))
        {
            if (owner == null)
            {
                itemBuilder.icon(Material.PAPER);
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
        return itemBuilder.build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Filter button)
    {
        String name = this.user.getTranslation(Constants.BUTTONS + button.name().toLowerCase() + ".name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(
            Constants.BUTTONS + button.name().toLowerCase() + ".description"));

        if (this.activeFilter != button)
        {
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));
        }

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) ->
        {
            this.activeFilter = button;
            this.updateFilters();
            this.build();
            return true;
        };

        Material material = switch (button) {
            case IS_ONLINE -> Material.FILLED_MAP;
            case HAS_DATA -> Material.WRITTEN_BOOK;
            case ALL_ISLANDS -> Material.CHEST;
        };

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(this.activeFilter == button).
            build();
    }


    /**
     * Opens gui with simple method call.
     *
     * @param parent Parent Panel
     * @param type Action type
     */
    public static void open(CommonPanel parent, Type type)
    {
        new ListIslandsPanel(parent, type).build();
    }


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


    /**
     * Action type that should happen when PanelItem is clicked.
     */
    enum Type
    {
        MANAGE,
        ICON
    }


    /**
     * This enum holds all possible filters in current GUI.
     */
    private enum Filter
    {
        /**
         * Shows islands with online users.
         */
        IS_ONLINE,
        /**
         * Shows islands with some valid data.
         */
        HAS_DATA,
        /**
         * Shows islands with data.
         */
        ALL_ISLANDS
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This variable stores gui type.
     */
    private final Type type;

    /**
     * Prefix for custom icon.
     */
    private final String iconPermission;

    /**
     * List with elements that will be displayed in current GUI.
     */
    private List<Island> filterElements;

    /**
     * Allows to switch between active tabs.
     */
    private Filter activeFilter;

    /**
     * This comparator orders island by their owner names.
     */
    private final static Comparator<Island> ISLAND_COMPARATOR = (o1, o2) -> {
        if (o1.getOwner() == null && o2.getOwner() == null)
        {
            return 0;
        }
        else if (o1.getOwner() == null)
        {
            return 1;
        }
        else if (o2.getOwner() == null)
        {
            return -1;
        }

        User u1 = User.getInstance(o1.getOwner());
        User u2 = User.getInstance(o2.getOwner());

        if (!u1.isPlayer())
        {
            return 1;
        }
        else if (!u2.isPlayer())
        {
            return -1;
        }
        else
        {
            return u1.getName().compareTo(u2.getName());
        }
    };
}
