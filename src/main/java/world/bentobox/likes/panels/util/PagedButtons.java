package world.bentobox.likes.panels.util;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.panels.ConversationUtils;
import world.bentobox.likes.utils.Constants;


/**
 * This class creates next, previous and search buttons that are the same in all paged panels.
 */
public final class PagedButtons
{
    /**
     * Utility class. Not for instantiation.
     */
    private PagedButtons()
    {
    }


    /**
     * This method creates the requested paging button.
     *
     * @param type Type of the button.
     * @param user User who views the panel.
     * @param pageIndex Index of the current page.
     * @param searchString Current search text.
     * @param changePage Consumer that changes current page by the given amount.
     * @param setSearch Consumer that sets the new search text and updates filters.
     * @param build Runnable that rebuilds the panel.
     * @return PanelItem with requested functionality.
     */
    public static PanelItem create(Type type,
        User user,
        int pageIndex,
        String searchString,
        IntConsumer changePage,
        Consumer<String> setSearch,
        Runnable build)
    {
        final String reference = Constants.BUTTONS + type.name().toLowerCase() + ".";
        final List<String> description = new ArrayList<>(3);

        PanelItemBuilder builder = new PanelItemBuilder().
            name(user.getTranslation(reference + "name"));

        switch (type)
        {
            case NEXT -> {
                description.add(user.getTranslation(reference + DESCRIPTION,
                    Constants.PARAMETER_NUMBER, String.valueOf(pageIndex + 2)));
                description.add("");
                description.add(user.getTranslation(Constants.TIPS + "click-to-next"));

                builder.icon(new ItemStack(Material.OAK_SIGN, pageIndex + 2));
                builder.clickHandler((panel, clicker, clickType, slot) ->
                {
                    changePage.accept(1);
                    build.run();
                    return true;
                });
            }
            case PREVIOUS -> {
                description.add(user.getTranslation(reference + DESCRIPTION,
                    Constants.PARAMETER_NUMBER, String.valueOf(pageIndex)));
                description.add("");
                description.add(user.getTranslation(Constants.TIPS + "click-to-previous"));

                builder.icon(new ItemStack(Material.OAK_SIGN, Math.max(1, pageIndex)));
                builder.clickHandler((panel, clicker, clickType, slot) ->
                {
                    changePage.accept(-1);
                    build.run();
                    return true;
                });
            }
            case SEARCH -> {
                PagedButtons.addSearchDescription(description, user, reference, searchString);

                builder.icon(new ItemStack(Material.ANVIL));
                builder.clickHandler(PagedButtons.createSearchClickHandler(setSearch, build));
            }
        }

        return builder.description(description).build();
    }


    /**
     * This method adds search button description lines.
     */
    private static void addSearchDescription(List<String> description,
        User user,
        String reference,
        String searchString)
    {
        final boolean hasSearch = searchString != null && !searchString.isEmpty();

        description.add(user.getTranslation(reference + DESCRIPTION));

        if (hasSearch)
        {
            description.add(user.getTranslation(reference + "search",
                Constants.PARAMETER_VALUE, searchString));
        }

        description.add("");
        description.add(user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (hasSearch)
        {
            description.add(user.getTranslation(Constants.TIPS + "right-click-to-clear"));
        }
    }


    /**
     * This method creates click handler that clears search text on right click and asks for a new one otherwise.
     */
    private static PanelItem.ClickHandler createSearchClickHandler(Consumer<String> setSearch, Runnable build)
    {
        return (panel, clicker, clickType, slot) ->
        {
            if (clickType.isRightClick())
            {
                // Clear string.
                setSearch.accept("");
                // Rebuild gui.
                build.run();
            }
            else
            {
                // Create consumer that process description change
                Consumer<String> consumer = value ->
                {
                    if (value != null)
                    {
                        setSearch.accept(value);
                    }

                    build.run();
                };

                // start conversation
                ConversationUtils.createStringInput(consumer,
                    clicker,
                    clicker.getTranslation(Constants.CONVERSATIONS + "write-search"),
                    clicker.getTranslation(Constants.CONVERSATIONS + "search-updated"));
            }

            return true;
        };
    }


    /**
     * Types of paging buttons.
     */
    public enum Type
    {
        /**
         * Next page button.
         */
        NEXT,
        /**
         * Previous page button.
         */
        PREVIOUS,
        /**
         * Search button.
         */
        SEARCH
    }


    /**
     * Translation reference suffix of the button description.
     */
    private static final String DESCRIPTION = "description";
}
