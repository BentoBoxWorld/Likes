///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.panels.admin;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.metadata.MetaDataValue;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.panels.ConversationUtils;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.panels.util.SelectBlocksPanel;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This class contains all necessary things that allows to select single island from all existing in given world.
 */
public class ListIslandsPanel extends CommonPanel
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
        this.searchString = "";
        this.pageIndex = 0;

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

        GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        panelBuilder.item(3, this.createButton(Filter.IS_ONLINE));
        panelBuilder.item(4, this.createButton(Filter.HAS_DATA));
        panelBuilder.item(5, this.createButton(Filter.ALL_ISLANDS));

        // Add search field.
        panelBuilder.item(40, this.createButton(Action.SEARCH));

        // Populate islands
        this.populateIslandList(panelBuilder);

        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method populates panel with all
     *
     * @param panelBuilder panelBuilder that must be populated.
     */
    private void populateIslandList(PanelBuilder panelBuilder)
    {
        if (this.pageIndex < 0)
        {
            this.pageIndex = this.maxPageIndex;
        }
        else if (this.pageIndex > this.maxPageIndex)
        {
            this.pageIndex = 0;
        }

        if (this.elements.size() > MAX_ELEMENTS)
        {
            // Navigation buttons if necessary

            panelBuilder.item(18, this.createButton(Action.PREVIOUS));
            panelBuilder.item(26, this.createButton(Action.NEXT));
        }

        int elementIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (elementIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            elementIndex < this.elements.size() &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index,
                    this.createIslandButton(this.elements.get(elementIndex++)));
            }

            index++;
        }
    }


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

                    if (member != null &&
                        member.isPlayer() &&
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
    private PanelItem createIslandButton(Island island)
    {
        if (island == null)
        {
            // Player do not have an island.
            return PanelItem.empty();
        }

        LikesObject likesObject =
            this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId());

        User owner = User.getInstance(island.getOwner());

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
                User user = User.getInstance(uuid);

                if (memberBuilder.length() > 0)
                {
                    memberBuilder.append("\n");
                }

                if (user != null)
                {
                    memberBuilder.append(
                        this.user.getTranslationOrNothing(Constants.BUTTONS + "island.member",
                            Constants.PARAMETER_PLAYER, user.getName()));
                }
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
                case LIKES:
                {
                    int rank = this.addon.getAddonManager().getIslandRankByLikes(this.world, likesObject);

                    if (rank != -1)
                    {
                        placeText = this.user.getTranslation(reference + "place",
                            Constants.PARAMETER_NUMBER, String.valueOf(rank),
                            Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "likes"));
                    }

                    break;
                }
                case LIKES_DISLIKES:
                {
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
                    break;
                }
                case STARS:
                {
                    int byStars = this.addon.getAddonManager().getIslandRankByStars(this.world, likesObject);

                    if (byStars != -1)
                    {
                        placeText = this.user.getTranslation(reference + "place",
                            Constants.PARAMETER_NUMBER, String.valueOf(byStars),
                            Constants.PARAMETER_TYPE, this.user.getTranslation(Constants.TYPES + "stars"));
                    }
                    break;
                }
            }
        }

        // Get Numbers Text
        String numbersText = "";

        switch (this.addon.getSettings().getMode())
        {
            case LIKES:
                numbersText = this.user.getTranslation(reference + "numbers_likes",
                    Constants.PARAMETER_LIKES, String.valueOf(likesObject == null ? 0 : likesObject.getLikes()));
                break;
            case LIKES_DISLIKES:
                numbersText = this.user.getTranslation(reference + "numbers_likes_dislikes",
                    Constants.PARAMETER_LIKES, String.valueOf(likesObject == null ? 0 : likesObject.getLikes()),
                    Constants.PARAMETER_DISLIKES, String.valueOf(likesObject == null ? 0 : likesObject.getDislikes()),
                    Constants.PARAMETER_RANK, String.valueOf(likesObject == null ? 0 : likesObject.getRank()));
                break;
            case STARS:
                numbersText = this.user.getTranslation(reference + "numbers_stars",
                    Constants.PARAMETER_STARS, this.hundredsFormat.format(likesObject == null ? 0 : likesObject.getStarsValue()),
                    Constants.PARAMETER_NUMBER, String.valueOf(likesObject == null ? 0 : likesObject.numberOfStars()));
                break;
        }

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
            case MANAGE:
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                if(this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId()) != null)
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-remove"));
                }

                break;
            case ICON:
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
                break;
        }

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, slot) ->
        {
            switch (this.type)
            {
                case MANAGE:
                    if (clickType.isShiftClick())
                    {
                        this.addon.getAddonManager().removeObject(island.getUniqueId());
                        this.build();
                    }
                    else
                    {
                        AdminViewPanel.openPanel(this, island);
                    }
                    break;
                case ICON:
                    SelectBlocksPanel.open(user, (hasSelected, materials) ->
                    {
                        if (hasSelected && materials.size() == 1)
                        {
                            if (island.getMetaData() == null)
                            {
                                island.setMetaData(new HashMap<>(4));
                            }

                            // Put icon in metadata.
                            island.putMetaData(Constants.METADATA_ICON,
                                new MetaDataValue(materials.iterator().next().name()));
                        }

                        this.build();
                    });
                    break;
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
            material = null;
        }

        if (material == null &&
            island.getMetaData() != null &&
            island.getMetaData().containsKey(Constants.METADATA_ICON))
        {
            material = Material.matchMaterial(island.getMetaData(Constants.METADATA_ICON).asString());
        }

        if (material == null)
        {
            material = likesObject == null ? Material.PAPER : Material.WRITTEN_BOOK;
        }

        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(nameText).
            description(description).
            clickHandler(clickHandler);

        if (material.equals(Material.PLAYER_HEAD))
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
     * Create button panel item with a given button type.
     *
     * @param button the button
     * @return the panel item
     */
    private PanelItem createButton(Action button)
    {
        final String reference = Constants.BUTTONS + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();

        PanelItem.ClickHandler clickHandler;

        Material icon = Material.PAPER;
        boolean glow = false;
        int count = 1;

        switch (button)
        {
            case PREVIOUS:
            {
                count = GuiUtils.getPreviousPage(this.pageIndex, this.maxPageIndex);
                description.add(this.user.getTranslationOrNothing(reference + ".description",
                    Constants.PARAMETER_NUMBER, String.valueOf(count)));

                // add empty line
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-previous"));

                clickHandler = (panel, user, clickType, i) -> {
                    this.pageIndex--;
                    this.build();
                    return true;
                };

                icon = Material.TIPPED_ARROW;
                break;
            }
            case NEXT:
            {
                count = GuiUtils.getNextPage(this.pageIndex, this.maxPageIndex);
                description.add(this.user.getTranslationOrNothing(reference + ".description",
                    Constants.PARAMETER_NUMBER, String.valueOf(count)));

                // add empty line
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-next"));

                clickHandler = (panel, user, clickType, i) -> {
                    this.pageIndex++;
                    this.build();
                    return true;
                };

                icon = Material.TIPPED_ARROW;
                break;
            }
            case SEARCH:
            {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (this.searchString != null && !this.searchString.isEmpty())
                {
                    description.add(this.user.getTranslation(reference + ".search",
                        Constants.PARAMETER_VALUE, this.searchString));
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

                if (this.searchString != null && !this.searchString.isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-clear"));
                }

                clickHandler = (panel, user, clickType, slot) ->
                {
                    if (clickType.isRightClick())
                    {
                        // Clear string.
                        this.searchString = "";
                        this.updateFilters();
                        // Rebuild gui.
                        this.build();
                    }
                    else
                    {
                        // Create consumer that process description change
                        Consumer<String> consumer = value ->
                        {
                            if (value != null)
                            {
                                this.searchString = value;
                                this.updateFilters();
                            }

                            this.build();
                        };

                        // start conversation
                        ConversationUtils.createStringInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-search"),
                            user.getTranslation(Constants.CONVERSATIONS + "search-updated"));
                    }

                    return true;
                };

                break;
            }
            default:
                return PanelItem.empty();
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(icon).
            amount(Math.max(count, 1)).
            clickHandler(clickHandler).
            glow(glow).
            build();
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
            this.pageIndex = 0;

            this.build();
            return true;
        };

        Material material;

        switch (button)
        {
            case IS_ONLINE:
                material = Material.FILLED_MAP;
                break;
            case HAS_DATA:
                material = Material.WRITTEN_BOOK;
                break;
            case ALL_ISLANDS:
                material = Material.CHEST;
                break;
            default:
                material = Material.PAPER;
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(this.activeFilter == button).
            build();
    }


    /**
     * This method updates element list.
     */
    private void updateFilters()
    {
        switch (this.activeFilter)
        {
            case IS_ONLINE:
                this.elements = Bukkit.getOnlinePlayers().stream().
                    map(player -> this.addon.getIslands().getIsland(this.world, player.getUniqueId())).
                    filter(Objects::nonNull).
                    distinct().
                    collect(Collectors.toList());
                break;
            case HAS_DATA:
                this.elements = this.addon.getIslands().getIslands(this.world).stream().
                    filter(island -> this.addon.getAddonManager().getExistingIslandLikes(island.getUniqueId()) != null).
                    distinct().
                    collect(Collectors.toList());
                break;
            case ALL_ISLANDS:
                this.elements = this.addon.getIslands().getIslands(this.world).stream().
                    filter(Island::isOwned).
                    distinct().
                    collect(Collectors.toList());
                break;
            default:
                this.elements = Collections.emptyList();
        }

        if (this.searchString != null && !this.searchString.isEmpty())
        {
            // Search through filtered elements.
            this.elements = this.searchElements(this.elements);
        }

        // Update max page index.
        this.maxPageIndex = (int) Math.ceil(1.0 * this.elements.size() / MAX_ELEMENTS) - 1;

        // Sort by name.
        this.elements.sort((o1, o2) ->
        {
            User u1 = User.getInstance(o1.getOwner());
            User u2 = User.getInstance(o2.getOwner());

            if (u1 == null || !u1.isPlayer())
            {
                return -1;
            }
            else if (u2 == null || !u2.isPlayer())
            {
                return 1;
            }
            else
            {
                return u1.getName().compareTo(u2.getName());
            }
        });
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


    /**
     * This enum holds all possible actions in current GUI.
     */
    private enum Action
    {
        /**
         * Process search function.
         */
        SEARCH,
        /**
         * Allows to select previous bundles in multi-page situation.
         */
        PREVIOUS,
        /**
         * Allows to select next bundles in multi-page situation.
         */
        NEXT
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
    private List<Island> elements;

    /**
     * Stores current string for searching.
     */
    private String searchString;

    /**
     * This variable holds current pageIndex for multi-page generator choosing.
     */
    private int pageIndex;

    /**
     * This variable stores maximal page index for previous/next page.
     */
    private int maxPageIndex;

    /**
     * Allows to switch between active tabs.
     */
    private Filter activeFilter;

    /**
     * Stores maximal elements per page.
     */
    public static final int MAX_ELEMENTS = 21;
}
