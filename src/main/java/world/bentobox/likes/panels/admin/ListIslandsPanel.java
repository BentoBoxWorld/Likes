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

        this.filterElements = Objects.requireNonNull(this.addon, "addon").getIslands().getIslands(this.world).stream().
            filter(island -> island.isOwned() || island.isSpawn()).
            sorted(ISLAND_COMPARATOR).
            distinct().
            collect(Collectors.toCollection(ArrayList::new));

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
                // Players can have multiple islands in the same world.
                flatMap(player -> this.addon.getIslands().getIslands(this.world, player.getUniqueId()).stream()).
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
            collect(Collectors.toCollection(ArrayList::new));
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

        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(this.getNameText(island, owner)).
            description(this.getDescription(island, owner, likesObject)).
            clickHandler(this.getClickHandler(island));

        Material material = this.getIconMaterial(island, owner, likesObject);

        if (!Material.PLAYER_HEAD.equals(material))
        {
            itemBuilder.icon(material);
        }
        else if (owner == null)
        {
            itemBuilder.icon(Material.PAPER);
        }
        else
        {
            itemBuilder.icon(owner.getName());
        }

        // At last add item to the panel builder
        return itemBuilder.build();
    }


    /**
     * Returns the name of the owner, or "unknown" text if island does not have an owner.
     */
    private String getOwnerName(User owner)
    {
        return owner == null ? this.user.getTranslation(ISLAND_REFERENCE + "unknown") : owner.getName();
    }


    /**
     * Returns the text for the island button name.
     */
    private String getNameText(Island island, User owner)
    {
        String nameText;

        if (island.getName() == null || island.getName().isEmpty())
        {
            nameText = this.user.getTranslation(ISLAND_REFERENCE + "owners-island",
                Constants.PARAMETER_PLAYER,
                this.getOwnerName(owner));
        }
        else
        {
            nameText = island.getName();
        }

        return this.user.getTranslation(ISLAND_REFERENCE + "name",
            Constants.PARAMETER_NAME, nameText);
    }


    /**
     * Returns the text with the list of island members.
     */
    private String getMemberText(Island island)
    {
        if (island.getMemberSet().size() <= 1)
        {
            return "";
        }

        StringBuilder memberBuilder = new StringBuilder(
            this.user.getTranslationOrNothing(Constants.BUTTONS + "island.members-title"));

        for (UUID uuid : island.getMemberSet())
        {
            String name = this.addon.getPlayers().getName(uuid);

            if (!memberBuilder.isEmpty())
            {
                memberBuilder.append("\n");
            }

            memberBuilder.append(
                this.user.getTranslationOrNothing(Constants.BUTTONS + "island.member",
                    Constants.PARAMETER_PLAYER, name));
        }

        return memberBuilder.toString();
    }


    /**
     * Returns the text for a single place in the top, or empty text if island is not ranked.
     */
    private String getPlaceLine(int rank, String type)
    {
        if (rank == -1)
        {
            return "";
        }

        return this.user.getTranslation(ISLAND_REFERENCE + PLACE_BUTTON_KEY,
            Constants.PARAMETER_NUMBER, String.valueOf(rank),
            Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + type));
    }


    /**
     * Returns the text with island places in the tops.
     */
    private String getPlaceText(LikesObject likesObject)
    {
        if (likesObject == null)
        {
            return "";
        }

        return switch (this.addon.getSettings().getMode()) {
            case LIKES -> this.getPlaceLine(
                this.addon.getAddonManager().getIslandRankByLikes(this.world, likesObject), "likes");
            case LIKES_DISLIKES -> {
                int byLikes = this.addon.getAddonManager().getIslandRankByLikes(this.world, likesObject);
                int byDislikes = this.addon.getAddonManager().getIslandRankByDislikes(this.world, likesObject);
                int byRank = this.addon.getAddonManager().getIslandRankByRank(this.world, likesObject);

                String placeText = this.getPlaceLine(byLikes, "likes");

                if (byDislikes != -1)
                {
                    placeText += "\n" + this.getPlaceLine(byDislikes, "dislikes");
                }

                if (byRank != -1)
                {
                    placeText += "\n" + this.getPlaceLine(byRank, "rank");
                }

                yield placeText;
            }
            case STARS -> this.getPlaceLine(
                this.addon.getAddonManager().getIslandRankByStars(this.world, likesObject), "stars");
        };
    }


    /**
     * Returns the text with island likes, dislikes and stars numbers.
     */
    private String getNumbersText(LikesObject likesObject)
    {
        // Island without data has zero in all numbers.
        LikesObject numbers = likesObject == null ? new LikesObject() : likesObject;

        return switch (this.addon.getSettings().getMode()) {
            case LIKES -> this.user.getTranslation(ISLAND_REFERENCE + "numbers_likes",
                Constants.PARAMETER_LIKES, String.valueOf(numbers.getLikes()));
            case LIKES_DISLIKES -> this.user.getTranslation(ISLAND_REFERENCE + "numbers_likes_dislikes",
                Constants.PARAMETER_LIKES, String.valueOf(numbers.getLikes()),
                Constants.PARAMETER_DISLIKES, String.valueOf(numbers.getDislikes()),
                Constants.PARAMETER_RANK, String.valueOf(numbers.getRank()));
            case STARS -> this.user.getTranslation(ISLAND_REFERENCE + "numbers_stars",
                Constants.PARAMETER_STARS,
                this.hundredsFormat.format(numbers.getStarsValue()),
                Constants.PARAMETER_NUMBER,
                String.valueOf(numbers.numberOfStars()));
        };
    }


    /**
     * Returns the description lines for the island button.
     */
    private List<String> getDescription(Island island, User owner, LikesObject likesObject)
    {
        String ownerText = this.user.getTranslation(ISLAND_REFERENCE + "owner",
            Constants.PARAMETER_PLAYER,
            this.getOwnerName(owner));

        // Now combine everything.
        String descriptionText = this.user.getTranslation(ISLAND_REFERENCE + "description",
            Constants.PARAMETER_OWNER, ownerText,
            Constants.PARAMETER_MEMBERS, this.getMemberText(island),
            Constants.PARAMETER_PLACE, this.getPlaceText(likesObject),
            Constants.PARAMETER_NUMBERS, this.getNumbersText(likesObject));
        List<String> description = Arrays.stream(descriptionText.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
            split("\n")).
            collect(Collectors.toCollection(ArrayList::new));

        description.add("");

        if (this.type == Type.MANAGE)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

            if (this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId()) != null)
            {
                description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-remove"));
            }
        }
        else if (this.type == Type.ICON)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }

        return description;
    }


    /**
     * Returns the click handler for the island button.
     */
    private PanelItem.ClickHandler getClickHandler(Island island)
    {
        return (panel, clicker, clickType, slot) ->
        {
            if (this.type == Type.MANAGE)
            {
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
            else if (this.type == Type.ICON)
            {
                SingleBlockSelector.open(this.user,
                    SingleBlockSelector.Mode.ANY,
                    (status, block) ->
                    {
                        if (Boolean.TRUE.equals(status))
                        {
                            island.putMetaData(Constants.METADATA_ICON,
                                new MetaDataValue(block.name()));
                        }

                        this.build();
                    });
            }

            return true;
        };
    }


    /**
     * Returns the material that is used as the island icon.
     */
    private Material getIconMaterial(Island island, User owner, LikesObject likesObject)
    {
        String permission = Utils.getPermissionValue(owner, this.iconPermission, null);

        if (permission != null && !permission.equals("*"))
        {
            return Material.matchMaterial(permission);
        }

        return island.getMetaData().
            map(map -> map.get(Constants.METADATA_ICON)).
            map(metaDataValue -> Material.matchMaterial(metaDataValue.asString())).
            orElseGet(() -> likesObject == null ? Material.PAPER : Material.WRITTEN_BOOK);
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
     * String constant for "place" button key.
     */
    private static final String PLACE_BUTTON_KEY = "place";

    private static final String ISLAND_REFERENCE = Constants.BUTTONS + "island.";

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
    private static final Comparator<Island> ISLAND_COMPARATOR = (o1, o2) -> {
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
