//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.likes.panels.util;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.panels.ConversationUtils;
import world.bentobox.likes.utils.Constants;


/**
 * This single abstract class will manage paged selectors similar to CommonPagedPanel.
 */
public abstract class PagedSelector<T>
{
    private static final String DESCRIPTION = "description";
    /**
     * Instantiates a new Paged selector.
     *
     * @param user the user
     */
    protected PagedSelector(User user)
    {
        this.user = user;
        this.searchString = "";
    }


    /**
     * Build.
     */
    protected abstract void build();


    /**
     * Create element button panel item.
     *
     * @param object the object
     * @return the panel item
     */
    protected abstract PanelItem createElementButton(T object);


    /**
     * This method is called when filter value is updated.
     */
    protected abstract void updateFilters();


    /**
     * Populate elements.
     *
     * @param panelBuilder the panel builder
     * @param objectList the object list
     */
    protected void populateElements(PanelBuilder panelBuilder, List<T> objectList)
    {
        final int MAX_ELEMENTS = 21;
        final int size = objectList.size();

        if (this.pageIndex < 0)
        {
            this.pageIndex = size / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (size / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        int objectIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (objectIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            objectIndex < size &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index, this.createElementButton(objectList.get(objectIndex++)));
            }

            index++;
        }

        // Add next page button if there are more than MAX_ELEMENTS objects and pageIndex + 1 is
        // larger or equal to the max page count.
        if (size > MAX_ELEMENTS && 1.0 * size / MAX_ELEMENTS > this.pageIndex + 1)
        {
            panelBuilder.item(26, this.getButton(CommonButtons.NEXT));

        }

        // Add previous page button if pageIndex is not 0.
        if (this.pageIndex > 0)
        {
            panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
        }

        // Add search button only if there is more than MAX_ELEMENTS objects or searchString
        // is not blank.
        if (!this.searchString.isBlank() || objectList.size() > MAX_ELEMENTS)
        {
            panelBuilder.item(40, this.getButton(CommonButtons.SEARCH));
        }
    }


    /**
     * This method returns PanelItem that represents given Button.
     * @param button Button that must be returned.
     * @return PanelItem with requested functionality.
     */
    protected PanelItem getButton(CommonButtons button)
    {
        final String reference = Constants.BUTTONS + button.name().toLowerCase() + ".";
        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);

        if (button == CommonButtons.NEXT)
        {
            this.buildNextButtonDescription(description, reference);
            ItemStack icon = new ItemStack(Material.OAK_SIGN, this.pageIndex + 2);
            PanelItem.ClickHandler clickHandler = (panel, clicker, clickType, slot) ->
            {
                this.pageIndex++;
                this.build();
                return true;
            };
            return new PanelItemBuilder().icon(icon).name(name).description(description).clickHandler(clickHandler).build();
        }
        else if (button == CommonButtons.PREVIOUS)
        {
            this.buildPreviousButtonDescription(description, reference);
            ItemStack icon = new ItemStack(Material.OAK_SIGN, Math.max(1, this.pageIndex));
            PanelItem.ClickHandler clickHandler = (panel, clicker, clickType, slot) ->
            {
                this.pageIndex--;
                this.build();
                return true;
            };
            return new PanelItemBuilder().icon(icon).name(name).description(description).clickHandler(clickHandler).build();
        }
        else if (button == CommonButtons.SEARCH)
        {
            this.buildSearchButtonDescription(description, reference);
            ItemStack icon = new ItemStack(Material.ANVIL);
            PanelItem.ClickHandler clickHandler = this.createSearchClickHandler();
            return new PanelItemBuilder().icon(icon).name(name).description(description).clickHandler(clickHandler).build();
        }

        return new PanelItemBuilder().icon(new ItemStack(Material.PAPER)).name(name).description(description).build();
    }

    /** Build next button description. */
    private void buildNextButtonDescription(List<String> description, String reference)
    {
        description.add(this.user.getTranslation(reference + DESCRIPTION,
            Constants.PARAMETER_NUMBER, String.valueOf(this.pageIndex + 2)));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-next"));
    }

    /** Build previous button description. */
    private void buildPreviousButtonDescription(List<String> description, String reference)
    {
        description.add(this.user.getTranslation(reference + DESCRIPTION,
            Constants.PARAMETER_NUMBER, String.valueOf(this.pageIndex)));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-previous"));
    }

    /** Build search button description. */
    private void buildSearchButtonDescription(List<String> description, String reference)
    {
        description.add(this.user.getTranslation(reference + DESCRIPTION));

        if (this.searchString != null && !this.searchString.isEmpty())
        {
            description.add(this.user.getTranslation(reference + "search",
                Constants.PARAMETER_VALUE, this.searchString));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (this.searchString != null && !this.searchString.isEmpty())
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-clear"));
        }
    }

    /** Create search click handler. */
    private PanelItem.ClickHandler createSearchClickHandler()
    {
        return (panel, clicker, clickType, slot) -> {
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
                    clicker,
                    clicker.getTranslation(Constants.CONVERSATIONS + "write-search"),
                    clicker.getTranslation(Constants.CONVERSATIONS + "search-updated"));
            }

            return true;
        };
    }


    /**
     * Next and Previous Buttons.
     */
    private enum CommonButtons
    {
        NEXT,
        PREVIOUS,
        SEARCH
    }


    /**
     * Current page index.
     */
    private int pageIndex;

    /**
     * User who opens gui.
     */
    protected final User user;

    /**
     * Text that contains filter string.
     */
    protected String searchString;
}
