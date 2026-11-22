package world.bentobox.likes.requests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.config.Settings.LikeMode;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.managers.LikesManager;

/**
 * Tests for {@link LikesRequestHandler} and {@link TopTenRequestHandler}.
 */
class RequestHandlersTest extends CommonTestSetup
{
    @Mock
    private LikesAddon addon;

    @Mock
    private LikesManager manager;

    private Settings settings;

    private LikesObject first;

    private LikesObject second;


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        this.settings = new Settings();
        when(this.addon.getSettings()).thenReturn(this.settings);
        when(this.addon.getAddonManager()).thenReturn(this.manager);

        this.mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(this.world);
        this.mockedBukkit.when(() -> Bukkit.getWorld("missing")).thenReturn(null);

        this.first = new LikesObject();
        this.first.setUniqueId("island-1");
        this.first.addLike(UUID.randomUUID());
        this.first.addLike(UUID.randomUUID());
        this.first.addDislike(UUID.randomUUID());
        this.first.addStars(UUID.randomUUID(), 4);

        this.second = new LikesObject();
        this.second.setUniqueId("island-2");
        this.second.addLike(UUID.randomUUID());

        when(this.manager.getIslandLikes("island-1", this.world)).thenReturn(this.first);
        when(this.manager.getIslandRankByLikes(this.world, this.first)).thenReturn(1);
        when(this.manager.getIslandRankByDislikes(this.world, this.first)).thenReturn(2);
        when(this.manager.getIslandRankByRank(this.world, this.first)).thenReturn(3);
        when(this.manager.getIslandRankByStars(this.world, this.first)).thenReturn(-1);

        List<LikesObject> top = new ArrayList<>(List.of(this.first, this.second));
        when(this.manager.getTopByLikes(this.world)).thenReturn(top);
        when(this.manager.getTopByDislikes(this.world)).thenReturn(top);
        when(this.manager.getTopByRank(this.world)).thenReturn(top);
        when(this.manager.getTopByStars(this.world)).thenReturn(top);
    }


    private static Map<String, Object> request(Object... values)
    {
        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < values.length; i += 2)
        {
            map.put((String) values[i], values[i + 1]);
        }

        return map;
    }


    // ---------------------------------------------------------------------
    // Section: Island likes
    // ---------------------------------------------------------------------


    @Test
    void testLikesHandlerLabel()
    {
        assertEquals("island-likes", new LikesRequestHandler(this.addon).getLabel());
    }


    @Test
    void testLikesHandlerInvalidInput()
    {
        LikesRequestHandler handler = new LikesRequestHandler(this.addon);

        assertEquals(Map.of(), handler.handle(null));
        assertEquals(Map.of(), handler.handle(new HashMap<>()));
        assertEquals(Map.of(), handler.handle(request("island", "island-1")));
        assertEquals(Map.of(), handler.handle(request("world-name", "world")));
        assertEquals(Map.of(), handler.handle(request("world-name", 1, "island", "island-1")));
        assertEquals(Map.of(), handler.handle(request("world-name", "world", "island", 1)));
        assertEquals(Map.of(), handler.handle(request("world-name", "missing", "island", "island-1")));
    }


    @SuppressWarnings("unchecked")
    @Test
    void testLikesHandler()
    {
        Map<String, Object> result = (Map<String, Object>) new LikesRequestHandler(this.addon).
            handle(request("world-name", "world", "island", "island-1"));

        assertEquals(2L, result.get("likes"));
        assertEquals(1L, result.get("dislikes"));
        assertEquals(1L, result.get("rank"));
        assertEquals(4.0, result.get("stars"));
        assertEquals(1, result.get("placeByLikes"));
        assertEquals(2, result.get("placeByDislikes"));
        assertEquals(3, result.get("placeByRank"));
        assertEquals(-1, result.get("placeByStars"));
        assertEquals(this.first.getLikedBy(), result.get("likedBy"));
        assertEquals(this.first.getDislikedBy(), result.get("dislikedBy"));
        assertEquals(this.first.getStarredBy(), result.get("staredBy"));
    }


    // ---------------------------------------------------------------------
    // Section: Top ten
    // ---------------------------------------------------------------------


    @Test
    void testTopTenHandlerLabel()
    {
        assertEquals("top-ten-likes", new TopTenRequestHandler(this.addon).getLabel());
    }


    @Test
    void testTopTenHandlerInvalidInput()
    {
        TopTenRequestHandler handler = new TopTenRequestHandler(this.addon);

        assertEquals(Map.of(), handler.handle(null));
        assertEquals(Map.of(), handler.handle(new HashMap<>()));
        assertEquals(Map.of(), handler.handle(request("type", "likes")));
        assertEquals(Map.of(), handler.handle(request("world-name", 1)));
        assertEquals(Map.of(), handler.handle(request("world-name", "missing")));
    }


    @SuppressWarnings("unchecked")
    private Map<String, Number> top(Object... values)
    {
        return (Map<String, Number>) new TopTenRequestHandler(this.addon).handle(request(values));
    }


    @Test
    void testTopTenDefaultsToLikes()
    {
        Map<String, Number> result = this.top("world-name", "world");

        assertEquals(List.of("island-1", "island-2"), new ArrayList<>(result.keySet()));
        assertEquals(2L, result.get("island-1"));
        assertEquals(1L, result.get("island-2"));
    }


    @Test
    void testTopTenDefaultsToStarsInStarsMode()
    {
        this.settings.setMode(LikeMode.STARS);

        Map<String, Number> result = this.top("world-name", "world");

        assertEquals(4.0, result.get("island-1"));
        assertEquals(0.0, result.get("island-2"));
    }


    @Test
    void testTopTenByType()
    {
        assertEquals(2L, this.top("world-name", "world", "type", "likes").get("island-1"));
        assertEquals(1L, this.top("world-name", "world", "type", "dislikes").get("island-1"));
        assertEquals(1L, this.top("world-name", "world", "type", "rank").get("island-1"));
        assertEquals(4.0, this.top("world-name", "world", "type", "STARS").get("island-1"));
    }


    @Test
    void testTopTenEmpty()
    {
        when(this.manager.getTopByLikes(this.world)).thenReturn(List.of());

        assertTrue(this.top("world-name", "world").isEmpty());
    }
}
