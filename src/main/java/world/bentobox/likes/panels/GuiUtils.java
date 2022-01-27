///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.panels;


/**
 * This class contains static methods that is used through multiple GUIs.
 */
public class GuiUtils
{
// ---------------------------------------------------------------------
// Section: Paging
// ---------------------------------------------------------------------


    /**
     * This method returns index of previous page based on current page index and max page index.
     *
     * @param pageIndex Current page index.
     * @param maxPageIndex Maximal page count.
     * @return Integer of previous page index.
     */
    public static int getPreviousPage(int pageIndex, int maxPageIndex)
    {
        // Page 0 is viewed... back arrow = last page ... next arrow = 2
        // Page 1 is viewed... back arrow = 1 ... next arrow = 3
        // Page 2 is viewed... back arrow = 2 ... next arrow = 4
        // Page n is viewed... back arrow = n ... next arrow = n+2
        // Last page is viewed .. back arrow = last... next arrow = 1

        return pageIndex == 0 ? maxPageIndex + 1 : pageIndex;
    }


    /**
     * This method returns index of next page based on current page index and max page index.
     *
     * @param pageIndex Current page index.
     * @param maxPageIndex Maximal page count.
     * @return Integer of next page index.
     */
    public static int getNextPage(int pageIndex, int maxPageIndex)
    {
        // Page 0 is viewed... back arrow = last page ... next arrow = 2
        // Page 1 is viewed... back arrow = 1 ... next arrow = 3
        // Page 2 is viewed... back arrow = 2 ... next arrow = 4
        // Page n is viewed... back arrow = n ... next arrow = n+2
        // Last page is viewed .. back arrow = last... next arrow = 1

        return pageIndex == maxPageIndex ? 1 : pageIndex + 2;
    }
}