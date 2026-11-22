package world.bentobox.likes.panels;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.managers.LikesManager;
import world.bentobox.likes.panels.user.LikesViewPanel;
import world.bentobox.likes.utils.Constants;

/**
 * Tests for {@link LikesViewPanel} when there is nothing to show.
 */
class LikesViewPanelTest extends CommonTestSetup
{
    @Mock
    private LikesAddon addon;

    @Mock
    private LikesManager manager;

    @Mock
    private User user;


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        when(this.addon.getAddonManager()).thenReturn(this.manager);
        when(this.island.getUniqueId()).thenReturn("island-1");
        when(this.user.getUniqueId()).thenReturn(this.uuid);
        when(this.user.getTranslation(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void testNoDataAboutOwnIsland()
    {
        LikesViewPanel.openPanel(this.addon, this.user, this.world, "bskyblock.", this.island);

        verify(this.manager).getExistingIslandLikes("island-1");
        verify(this.user).sendMessage(contains(Constants.ERRORS + "no-data-about-your-island"));
    }


    @Test
    void testNoDataAboutOtherIsland()
    {
        when(this.island.getMemberSet()).thenReturn(ImmutableSet.of(UUID.randomUUID()));

        LikesViewPanel.openPanel(this.addon, this.user, this.world, "bskyblock.", this.island);

        verify(this.user).sendMessage(contains(Constants.ERRORS + "no-data-about-island"));
    }
}
