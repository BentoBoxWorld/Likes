package world.bentobox.likes.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.panels.util.PagedButtons;
import world.bentobox.likes.utils.Constants;

/**
 * Tests for {@link PagedButtons}.
 */
class PagedButtonsTest extends CommonTestSetup
{
    @Mock
    private User user;

    private int pageIndex;

    private final List<String> searches = new ArrayList<>();

    private int builds;


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        this.pageIndex = 3;
        // Translations return their reference, followed by the first parameter value if there is one.
        when(this.user.getTranslation(anyString(), any(String[].class))).thenAnswer(invocation ->
        {
            Object[] arguments = invocation.getArguments();
            return arguments.length > 2 ? arguments[0] + ":" + arguments[2] : arguments[0];
        });
    }


    private PanelItem create(PagedButtons.Type type, String searchString)
    {
        return PagedButtons.create(type,
            this.user,
            this.pageIndex,
            searchString,
            change -> this.pageIndex += change,
            this.searches::add,
            () -> this.builds++);
    }


    private void click(PanelItem item, ClickType clickType)
    {
        assertTrue(item.getClickHandler().orElseThrow().onClick(mock(Panel.class), this.user, clickType, 0));
    }


    @Test
    void testNextButton()
    {
        PanelItem item = this.create(PagedButtons.Type.NEXT, "");

        assertEquals(Constants.BUTTONS + "next.name", item.getName());
        // Next page number is shown.
        assertEquals(Constants.BUTTONS + "next.description:5", item.getDescription().get(0));
        assertEquals(Constants.TIPS + "click-to-next", item.getDescription().get(2));

        this.click(item, ClickType.LEFT);

        assertEquals(4, this.pageIndex);
        assertEquals(1, this.builds);
    }


    @Test
    void testPreviousButton()
    {
        PanelItem item = this.create(PagedButtons.Type.PREVIOUS, "");

        assertEquals(Constants.BUTTONS + "previous.description:3", item.getDescription().get(0));
        assertEquals(Constants.TIPS + "click-to-previous", item.getDescription().get(2));

        this.click(item, ClickType.LEFT);

        assertEquals(2, this.pageIndex);
        assertEquals(1, this.builds);
    }


    @Test
    void testSearchButtonWithoutSearch()
    {
        PanelItem item = this.create(PagedButtons.Type.SEARCH, "");

        assertEquals(List.of(Constants.BUTTONS + "search.description",
            "",
            Constants.TIPS + "left-click-to-edit"), item.getDescription());
    }


    @Test
    void testSearchButtonNullSearch()
    {
        PanelItem item = this.create(PagedButtons.Type.SEARCH, null);

        assertEquals(3, item.getDescription().size());
        assertFalse(item.getDescription().contains(Constants.TIPS + "right-click-to-clear"));
    }


    @Test
    void testSearchButtonWithSearch()
    {
        PanelItem item = this.create(PagedButtons.Type.SEARCH, "tasty");

        assertEquals(List.of(Constants.BUTTONS + "search.description",
            Constants.BUTTONS + "search.search:tasty",
            "",
            Constants.TIPS + "left-click-to-edit",
            Constants.TIPS + "right-click-to-clear"), item.getDescription());
    }


    @Test
    void testSearchButtonRightClickClearsSearch()
    {
        PanelItem item = this.create(PagedButtons.Type.SEARCH, "tasty");

        this.click(item, ClickType.RIGHT);

        assertEquals(List.of(""), this.searches);
        assertEquals(1, this.builds);
        assertEquals(3, this.pageIndex);
    }
}
