///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.likes.panels;


import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.likes.panels.util.PagedButtons;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;


/**
 * This panel implements common things for Paged pages.
 *
 * @param <T> Type of elements that are paged.
 */
public abstract class CommonPagedPanel<T> extends CommonPanel
{
    /**
     * Instantiates a new Common paged panel.
     *
     * @param addon the addon
     * @param user the user
     * @param world the world
     * @param permissionPrefix the permission prefix
     */
    protected CommonPagedPanel(LikesAddon addon,
        User user,
        World world,
        String permissionPrefix)
    {
        super(addon, user, world, permissionPrefix);
    }


    /**
     * Instantiates a new Common paged panel.
     *
     * @param parentPanel the parent panel
     */
    protected CommonPagedPanel(@NotNull CommonPanel parentPanel)
    {
        super(parentPanel);
    }


    /**
     * This method is called when filter value is updated.
     */
    protected abstract void updateFilters();


    /**
     * Create element button panel item.
     *
     * @param object the object
     * @return the panel item
     */
    protected abstract PanelItem createElementButton(T object);


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

        if (size > MAX_ELEMENTS && 1.0 * size / MAX_ELEMENTS > this.pageIndex + 1)
        {
            panelBuilder.item(26, this.getButton(PagedButtons.Type.NEXT));
        }

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
     * Text that contains filter string.
     */
    protected String searchString = "";

    /**
     * Current page index.
     */
    private int pageIndex;
}
