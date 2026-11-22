package world.bentobox.likes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.config.Settings.LikeMode;
import world.bentobox.likes.config.Settings.VIEW_MODE;

/**
 * Tests for {@link Settings}.
 */
class SettingsTest extends CommonTestSetup
{
    private Settings settings;


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        this.settings = new Settings();
    }


    @Test
    void testDefaults()
    {
        assertEquals(Material.PLAYER_HEAD, this.settings.getDefaultIcon());
        assertEquals(LikeMode.LIKES_DISLIKES, this.settings.getMode());
        assertEquals(0, this.settings.getLikeAddCost());
        assertEquals(0, this.settings.getLikeRemoveCost());
        assertEquals(0, this.settings.getDislikeAddCost());
        assertEquals(0, this.settings.getDislikeRemoveCost());
        assertFalse(this.settings.isLogHistory());
        assertTrue(this.settings.getDisabledGameModes().isEmpty());
    }


    @Test
    void testSetters()
    {
        this.settings.setDefaultIcon(Material.STONE);
        this.settings.setMode(LikeMode.STARS);
        this.settings.setLikeAddCost(1.5);
        this.settings.setLikeRemoveCost(2.5);
        this.settings.setDislikeAddCost(3.5);
        this.settings.setDislikeRemoveCost(4.5);
        this.settings.setLogHistory(true);
        this.settings.setResetLikes(true);
        this.settings.setInformPlayers(true);
        this.settings.setAllowUnowned(true);
        this.settings.setDisabledGameModes(Set.of("AcidIsland"));

        assertEquals(Material.STONE, this.settings.getDefaultIcon());
        assertEquals(LikeMode.STARS, this.settings.getMode());
        assertEquals(1.5, this.settings.getLikeAddCost());
        assertEquals(2.5, this.settings.getLikeRemoveCost());
        assertEquals(3.5, this.settings.getDislikeAddCost());
        assertEquals(4.5, this.settings.getDislikeRemoveCost());
        assertTrue(this.settings.isLogHistory());
        assertTrue(this.settings.isResetLikes());
        assertTrue(this.settings.isInformPlayers());
        assertTrue(this.settings.isAllowUnowned());
        assertEquals(Set.of("AcidIsland"), this.settings.getDisabledGameModes());

        this.settings.setResetLikes(false);
        this.settings.setInformPlayers(false);
        this.settings.setAllowUnowned(false);

        assertFalse(this.settings.isResetLikes());
        assertFalse(this.settings.isInformPlayers());
        assertFalse(this.settings.isAllowUnowned());
    }


    @Test
    void testViewModeByName()
    {
        assertEquals(VIEW_MODE.LIKES, VIEW_MODE.getMode("likes"));
        assertEquals(VIEW_MODE.DISLIKES, VIEW_MODE.getMode("Dislikes"));
        assertEquals(VIEW_MODE.RANK, VIEW_MODE.getMode("RANK"));
        assertEquals(VIEW_MODE.STARS, VIEW_MODE.getMode("stars"));
        assertNull(VIEW_MODE.getMode("unknown"));
    }
}
