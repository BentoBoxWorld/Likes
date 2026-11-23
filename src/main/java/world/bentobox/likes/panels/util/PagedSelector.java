//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.likes.panels.util;


import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This single abstract class will manage paged selectors similar to CommonPagedPanel.
 */
public abstract class PagedSelector<T>
{
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
            panelBuilder.item(26, this.getButton(PagedButtons.Type.NEXT));

        }

        // Add previous page button if pageIndex is not 0.
        if (this.pageIndex > 0)
        {
            panelBuilder.item(18, this.getButton(PagedButtons.Type.PREVIOUS));
        }

        // Add search button only if there is more than MAX_ELEMENTS objects or searchString
        // is not blank.
        if (!this.searchString.isBlank() || objectList.size() > MAX_ELEMENTS)
        {
            panelBuilder.item(40, this.getButton(PagedButtons.Type.SEARCH));
        }
    }


    /**
     * This method returns PanelItem that represents given Button.
     * @param button Button that must be returned.
     * @return PanelItem with requested functionality.
     */
    private PanelItem getButton(PagedButtons.Type button)
    {
        return PagedButtons.create(button,
            this.user,
            this.pageIndex,
            this.searchString,
            change -> this.pageIndex += change,
            value ->
            {
                this.searchString = value;
                this.updateFilters();
            },
            this::build);
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
