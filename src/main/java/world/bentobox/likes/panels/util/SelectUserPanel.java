///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.panels.util;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.panels.ConversationUtils;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.panels.admin.ListIslandsPanel;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This class contains all necessary things that allows to select single user from inputList by removing excludedUsers.
 */
public class SelectUserPanel
{
    private SelectUserPanel(User user, List<User> inputList, Set<User> excludedUsers, Consumer<User> consumer)
    {
        this.consumer = consumer;
        this.user = user;

        this.elements = inputList.stream().
            filter(player -> excludedUsers.isEmpty() || !excludedUsers.contains(player)).
            collect(Collectors.toList());

        // Shows only online players
        this.activeFilter = Filter.ONLINE_PLAYERS;
        this.searchString = "";
        this.pageIndex = 0;

        this.updateFilters();
    }


    /**
     * This method builds all necessary elements in GUI panel.
     */
    private void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
            name(this.user.getTranslation(Constants.TITLES + "select-player"));

        GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        final int MAX_ELEMENTS = 21;

        if (this.pageIndex < 0)
        {
            this.pageIndex = this.filterElements.size() / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (this.filterElements.size() / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        int userIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (userIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            userIndex < this.filterElements.size() &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index, this.createUserButton(this.filterElements.get(userIndex++)));
            }

            index++;
        }

        if (this.filterElements.size() > MAX_ELEMENTS)
        {
            // Navigation buttons if necessary
            panelBuilder.item(18, this.createButton(Action.PREVIOUS));
            panelBuilder.item(26, this.createButton(Action.NEXT));
        }

        // Add search block icon.
        panelBuilder.item(40, this.createButton(Action.SEARCH));
        // Add return button
        panelBuilder.item(44, this.createButton(Action.RETURN));

        // Add 2 filter buttons
        panelBuilder.item(3, this.createButton(Filter.ONLINE_PLAYERS));
        panelBuilder.item(5, this.createButton(Filter.ALL_PLAYERS));

        panelBuilder.build();
    }


    /**
     * This method creates PanelItem that represents given user.
     *
     * @param user Material which icon must be created.
     * @return PanelItem that represents given user.
     */
    private PanelItem createUserButton(User user)
    {
        List<String> description = new ArrayList<>(2);
        description.add("");
        description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-select"));

        return new PanelItemBuilder().
            icon(user.getName()).
            description(description).
            name(this.user.getTranslation(Constants.BUTTONS + "user.name", Constants.PARAMETER_NAME, user.getName())).
            clickHandler((panel, user1, clickType, slot) ->
            {
                this.consumer.accept(user);
                return true;
            }).
            build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Action button)
    {
        final String reference = Constants.BUTTONS + button.name().toLowerCase();

        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();

        PanelItem.ClickHandler clickHandler;
        Material icon;
        int count = 1;

        switch (button)
        {
            case RETURN:
            {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

                clickHandler = (panel, user, clickType, i) -> {
                    // Return NULL.
                    this.consumer.accept(null);
                    return true;
                };

                icon = Material.OAK_DOOR;

                break;
            }
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

                clickHandler = (panel, user, clickType, slot) -> {
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

                icon = Material.PAPER;

                break;
            }
            default:
                return PanelItem.empty();
        }

        return new PanelItemBuilder().
            name(name).
            amount(count).
            description(description).
            icon(icon).
            clickHandler(clickHandler).
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
            case ONLINE_PLAYERS:
                material = Material.FILLED_MAP;
                break;
            case ALL_PLAYERS:
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
     * This method filters out users that do not contains search field.
     *
     * @param userCollection Collection of the materials from which it should search.
     * @return List of Users that contains searched field.
     */
    private List<User> searchElements(Collection<User> userCollection)
    {
        return userCollection.stream().
            filter(user ->
            {
                // If user name is set and name contains search field, then do not filter out.
                return user.getName().toLowerCase().contains(this.searchString.toLowerCase());
            }).
            distinct().
            collect(Collectors.toList());
    }


    /**
     * This method updates element list.
     */
    private void updateFilters()
    {
        switch (this.activeFilter)
        {
            case ONLINE_PLAYERS:
                this.filterElements = this.elements.stream().
                    filter(User::isOnline).
                    collect(Collectors.toList());
                break;
            case ALL_PLAYERS:
                this.filterElements = this.elements;
                break;
            default:
                this.filterElements = Collections.emptyList();
        }

        if (this.searchString != null && !this.searchString.isEmpty())
        {
            // Search through filtered elements.
            this.filterElements = this.searchElements(this.filterElements);
        }

        // Update max page index.
        this.maxPageIndex = (int) Math.ceil(1.0 * this.elements.size() / 21) - 1;

        // Sort by name.
        this.filterElements.sort(Comparator.comparing(User::getName));
    }


// ---------------------------------------------------------------------
// Section: Static Methods
// ---------------------------------------------------------------------


    public static void open(User user, List<User> userList, List<User> excludedList, Consumer<User> consumer)
    {
        new SelectUserPanel(user, userList, new HashSet<>(excludedList), consumer).build();
    }


    public static void open(User user, List<User> userList, Consumer<User> consumer)
    {
        new SelectUserPanel(user, userList, Collections.emptySet(), consumer).build();
    }


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


    /**
     * Stores all available actions for Panel.
     */
    private enum Action
    {
        PREVIOUS,
        NEXT,
        RETURN,
        SEARCH
    }


    /**
     * This enum holds all possible filters in current GUI.
     */
    private enum Filter
    {
        /**
         * Shows online users.
         */
        ONLINE_PLAYERS,
        /**
         * Shows all users.
         */
        ALL_PLAYERS
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * List with elements that will be displayed in current GUI.
     */
    private final List<User> elements;

    /**
     * This variable stores consumer.
     */
    private final Consumer<User> consumer;

    /**
     * User who runs GUI.
     */
    private final User user;

    /**
     * String that allows to search for a material.
     */
    private String searchString;

    /**
     * List with elements that will be displayed in current GUI.
     */
    private List<User> filterElements;

    /**
     * Page index.
     */
    private int pageIndex;

    /**
     * This variable stores maximal page index for elements.
     */
    private int maxPageIndex;

    /**
     * Variable stores active filter.
     */
    private Filter activeFilter;
}
