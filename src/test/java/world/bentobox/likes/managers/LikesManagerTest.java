package world.bentobox.likes.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableSet;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.logs.LogEntry;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.config.Settings.LikeMode;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.events.DislikeAddEvent;
import world.bentobox.likes.events.DislikeRemoveEvent;
import world.bentobox.likes.events.LikeAddEvent;
import world.bentobox.likes.events.LikeRemoveEvent;
import world.bentobox.likes.events.StarsAddEvent;
import world.bentobox.likes.events.StarsRemoveEvent;

/**
 * Tests for {@link LikesManager}.
 */
class LikesManagerTest extends CommonTestSetup
{
    private static final String GAME_MODE = "BSkyBlock";

    @Mock
    private LikesAddon addon;

    @Mock
    private GameModeAddon gameModeAddon;

    @Mock
    private PlayersManager playersManager;

    @Mock
    private Island secondIsland;

    private MockedStatic<DatabaseSetup> mockDb;

    private AbstractDatabaseHandler<Object> handler;

    private Settings settings;

    private LikesManager manager;

    private final List<Object> stored = new ArrayList<>();


    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        // Database
        this.handler = mock(AbstractDatabaseHandler.class);
        this.mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        this.mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(this.handler);
        when(this.handler.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));
        when(this.handler.loadObjects()).thenReturn(this.stored);

        // GameMode
        AddonDescription description = new AddonDescription.Builder("main", GAME_MODE, "1.0").build();
        when(this.gameModeAddon.getDescription()).thenReturn(description);
        when(this.iwm.getAddon(any())).thenReturn(Optional.of(this.gameModeAddon));

        // Addon
        this.settings = new Settings();
        when(this.addon.getSettings()).thenReturn(this.settings);
        when(this.addon.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(this.addon.getPlayers()).thenReturn(this.playersManager);
        when(this.playersManager.getName(any())).thenReturn("owner");

        // Two islands owned by the same player.
        when(this.island.getUniqueId()).thenReturn("island-1");
        when(this.island.getWorld()).thenReturn(this.world);
        when(this.secondIsland.getUniqueId()).thenReturn("island-2");
        when(this.secondIsland.getWorld()).thenReturn(this.world);
        when(this.secondIsland.getOwner()).thenReturn(this.uuid);
        when(this.secondIsland.getMemberSet()).thenReturn(ImmutableSet.of(this.uuid));
    }


    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        this.mockDb.closeOnDemand();
        super.tearDown();
    }


    private void createManager(LikeMode mode)
    {
        this.settings.setMode(mode);
        this.manager = new LikesManager(this.addon);
    }


    private static User mockUser()
    {
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(userId);
        when(user.getName()).thenReturn("user-" + userId);
        when(user.getTranslation(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(user.getTranslation(anyString(), any(String[].class))).
            thenAnswer(invocation -> invocation.getArgument(0));
        return user;
    }


    private static LikesObject storedObject(String id, String gameMode, int likes, int dislikes)
    {
        LikesObject object = new LikesObject();
        object.setUniqueId(id);
        object.setGameMode(gameMode);

        for (int i = 0; i < likes; i++)
        {
            object.addLike(UUID.randomUUID());
        }

        for (int i = 0; i < dislikes; i++)
        {
            object.addDislike(UUID.randomUUID());
        }

        object.setChanged(false);
        return object;
    }


    // ---------------------------------------------------------------------
    // Section: Loading
    // ---------------------------------------------------------------------


    @Test
    void testLoadFromDatabase()
    {
        this.stored.add(storedObject("a", GAME_MODE, 3, 0));
        this.stored.add(storedObject("b", GAME_MODE, 5, 1));
        this.stored.add(storedObject("c", "AcidIsland", 9, 0));

        this.createManager(LikeMode.LIKES_DISLIKES);

        assertNotNull(this.manager.getExistingIslandLikes("a"));
        assertNotNull(this.manager.getExistingIslandLikes("c"));
        assertNull(this.manager.getExistingIslandLikes("unknown"));

        // Game modes are ranked separately.
        assertEquals(2, this.manager.getSortedLikes(GAME_MODE).size());
        assertEquals(1, this.manager.getSortedLikes("AcidIsland").size());
        assertEquals("b", this.manager.getSortedLikes(GAME_MODE).exact(0).getUniqueId());
    }


    @Test
    void testReloadClearsCache()
    {
        this.stored.add(storedObject("a", GAME_MODE, 3, 0));
        this.createManager(LikeMode.LIKES);

        this.stored.clear();
        this.manager.load();

        assertNull(this.manager.getExistingIslandLikes("a"));
        assertTrue(this.manager.getSortedLikes(GAME_MODE).isEmpty());
    }


    @Test
    void testGetIslandLikesCreatesNewObject() throws Exception
    {
        this.createManager(LikeMode.LIKES);

        LikesObject object = this.manager.getIslandLikes("island-1", this.world);

        assertEquals("island-1", object.getUniqueId());
        assertEquals(GAME_MODE, object.getGameMode());
        assertSame(object, this.manager.getExistingIslandLikes("island-1"));
        assertSame(object, this.manager.getIslandLikes("island-1", this.gameModeAddon));
        verify(this.handler).saveObject(object);
    }


    @Test
    void testGetIslandLikesLoadsFromDatabase() throws Exception
    {
        this.createManager(LikeMode.LIKES);

        LikesObject inDatabase = storedObject("island-1", GAME_MODE, 2, 0);
        when(this.handler.objectExists("island-1")).thenReturn(true);
        when(this.handler.loadObject("island-1")).thenReturn(inDatabase);

        assertSame(inDatabase, this.manager.getIslandLikes("island-1", this.world));
        assertEquals(1, this.manager.getIslandRankByLikes(this.world, inDatabase));
    }


    @Test
    void testGetIslandLikesNullInDatabase() throws Exception
    {
        this.createManager(LikeMode.LIKES);

        when(this.handler.objectExists("island-1")).thenReturn(true);
        when(this.handler.loadObject("island-1")).thenReturn(null);

        LikesObject object = this.manager.getIslandLikes("island-1", this.world);

        assertNotNull(object);
        verify(this.addon).logError(anyString());
    }


    // ---------------------------------------------------------------------
    // Section: Likes
    // ---------------------------------------------------------------------


    @Test
    void testAddLike()
    {
        this.createManager(LikeMode.LIKES);
        User user = mockUser();

        this.manager.addLike(user, this.island, this.world);

        assertTrue(this.manager.hasLiked(user.getUniqueId(), "island-1", this.world));
        assertEquals(1, this.manager.getExistingIslandLikes("island-1").getLikes());
        verify(this.addon).callEvent(any(LikeAddEvent.class));
        verify(user).sendMessage(anyString());
        // Change is saved.
        assertFalse(this.manager.getExistingIslandLikes("island-1").isChanged());
    }


    @Test
    void testAddLikeTwiceCountsOnce()
    {
        this.createManager(LikeMode.LIKES);
        User user = mockUser();

        this.manager.addLike(user, this.island, this.world);
        this.manager.addLike(user, this.island, this.world);

        assertEquals(1, this.manager.getExistingIslandLikes("island-1").getLikes());
        verify(this.addon, times(1)).callEvent(any(LikeAddEvent.class));
    }


    @Test
    void testRemoveLike()
    {
        this.createManager(LikeMode.LIKES);
        User user = mockUser();

        this.manager.addLike(user, this.island, this.world);
        this.manager.removeLike(user, this.island, this.world);

        assertFalse(this.manager.hasLiked(user.getUniqueId(), "island-1", this.world));
        assertEquals(0, this.manager.getExistingIslandLikes("island-1").getLikes());
        verify(this.addon).callEvent(any(LikeRemoveEvent.class));
    }


    @Test
    void testRemoveLikeNotLiked()
    {
        this.createManager(LikeMode.LIKES);
        User user = mockUser();

        this.manager.removeLike(user, this.island, this.world);

        verify(this.addon, never()).callEvent(any());
        verify(user, never()).sendMessage(anyString());
    }


    @Test
    void testAddLikeLogsHistory()
    {
        this.settings.setLogHistory(true);
        this.createManager(LikeMode.LIKES);
        User user = mockUser();

        this.manager.addLike(user, this.island, this.world);
        this.manager.removeLike(user, this.island, this.world);

        List<LogEntry> history = this.manager.getExistingIslandLikes("island-1").getHistory();
        assertEquals(2, history.size());
        assertTrue(history.stream().allMatch(entry -> entry.getData().containsKey("user-id")));
    }


    @Test
    void testAddLikeNoHistoryByDefault()
    {
        this.createManager(LikeMode.LIKES);

        this.manager.addLike(mockUser(), this.island, this.world);

        assertTrue(this.manager.getExistingIslandLikes("island-1").getHistory().isEmpty());
    }


    @Test
    void testAddLikeSpawnIslandUsesSpawnName()
    {
        this.createManager(LikeMode.LIKES);
        when(this.island.isSpawn()).thenReturn(true);
        User user = mockUser();

        this.manager.addLike(user, this.island, this.world);

        verify(user).getTranslation("likes.gui.buttons.island.spawn");
    }


    @Test
    void testAddLikeNamedIslandDoesNotLookupOwner()
    {
        this.createManager(LikeMode.LIKES);
        when(this.island.getName()).thenReturn("My Island");

        this.manager.addLike(mockUser(), this.island, this.world);

        verify(this.playersManager, never()).getName(any());
    }


    @Test
    void testAddLikeInformsOnlyOnlineMembers()
    {
        this.settings.setInformPlayers(true);
        this.createManager(LikeMode.LIKES);
        // The only member is offline.
        when(this.mockPlayer.isOnline()).thenReturn(false);

        this.manager.addLike(mockUser(), this.island, this.world);

        verify(this.island, atLeastOnce()).getMemberSet();
        verify(this.mockPlayer, never()).sendMessage(anyString());
    }


    // ---------------------------------------------------------------------
    // Section: Dislikes
    // ---------------------------------------------------------------------


    @Test
    void testAddDislike()
    {
        this.createManager(LikeMode.LIKES_DISLIKES);
        User user = mockUser();

        this.manager.addDislike(user, this.island, this.world);
        this.manager.addDislike(user, this.island, this.world);

        assertTrue(this.manager.hasDisliked(user.getUniqueId(), "island-1", this.world));
        assertEquals(1, this.manager.getExistingIslandLikes("island-1").getDislikes());
        verify(this.addon, times(1)).callEvent(any(DislikeAddEvent.class));
    }


    @Test
    void testDislikeReplacesLike()
    {
        this.createManager(LikeMode.LIKES_DISLIKES);
        User user = mockUser();

        this.manager.addLike(user, this.island, this.world);
        this.manager.addDislike(user, this.island, this.world);

        LikesObject object = this.manager.getExistingIslandLikes("island-1");
        assertEquals(0, object.getLikes());
        assertEquals(1, object.getDislikes());
        assertEquals(-1, object.getRank());
    }


    @Test
    void testRemoveDislike()
    {
        this.createManager(LikeMode.LIKES_DISLIKES);
        User user = mockUser();

        this.manager.removeDislike(user, this.island, this.world);
        verify(this.addon, never()).callEvent(any());

        this.manager.addDislike(user, this.island, this.world);
        this.manager.removeDislike(user, this.island, this.world);

        assertFalse(this.manager.hasDisliked(user.getUniqueId(), "island-1", this.world));
        verify(this.addon).callEvent(any(DislikeRemoveEvent.class));
    }


    // ---------------------------------------------------------------------
    // Section: Stars
    // ---------------------------------------------------------------------


    @Test
    void testAddStars()
    {
        this.createManager(LikeMode.STARS);
        User user = mockUser();

        this.manager.addStars(user, 4, this.island, this.world);
        // Second vote is ignored until the first one is removed.
        this.manager.addStars(user, 1, this.island, this.world);

        assertTrue(this.manager.hasStarred(user.getUniqueId(), "island-1", this.world));
        assertEquals(4, this.manager.getStarred(user.getUniqueId(), "island-1", this.world));
        assertEquals(4.0, this.manager.getExistingIslandLikes("island-1").getStarsValue());
        verify(this.addon, times(1)).callEvent(any(StarsAddEvent.class));
    }


    @Test
    void testRemoveStars()
    {
        this.createManager(LikeMode.STARS);
        User user = mockUser();

        this.manager.removeStars(user, this.island, this.world);
        verify(this.addon, never()).callEvent(any());

        this.manager.addStars(user, 5, this.island, this.world);
        this.manager.removeStars(user, this.island, this.world);

        assertFalse(this.manager.hasStarred(user.getUniqueId(), "island-1", this.world));
        assertEquals(0, this.manager.getStarred(user.getUniqueId(), "island-1", this.world));
        verify(this.addon).callEvent(any(StarsRemoveEvent.class));
    }


    // ---------------------------------------------------------------------
    // Section: Multiple islands
    // ---------------------------------------------------------------------


    @Test
    void testIslandsOfSameOwnerAreTrackedSeparately()
    {
        this.createManager(LikeMode.LIKES);
        User user = mockUser();

        this.manager.addLike(user, this.secondIsland, this.world);

        assertNotSame(this.manager.getIslandLikes("island-1", this.world),
            this.manager.getIslandLikes("island-2", this.world));
        assertFalse(this.manager.hasLiked(user.getUniqueId(), "island-1", this.world));
        assertTrue(this.manager.hasLiked(user.getUniqueId(), "island-2", this.world));

        // Same user can like both islands of the same owner.
        this.manager.addLike(user, this.island, this.world);
        assertEquals(1, this.manager.getExistingIslandLikes("island-1").getLikes());
        assertEquals(1, this.manager.getExistingIslandLikes("island-2").getLikes());
    }


    @Test
    void testIslandsOfSameOwnerAreRankedSeparately()
    {
        this.createManager(LikeMode.LIKES);

        this.manager.addLike(mockUser(), this.island, this.world);
        this.manager.addLike(mockUser(), this.secondIsland, this.world);
        this.manager.addLike(mockUser(), this.secondIsland, this.world);

        List<LikesObject> top = this.manager.getTopByLikes(this.world);

        assertEquals(List.of("island-2", "island-1"), top.stream().map(LikesObject::getUniqueId).toList());
        assertEquals(1, this.manager.getIslandRankByLikes(this.world,
            this.manager.getExistingIslandLikes("island-2")));
        assertEquals(2, this.manager.getIslandRankByLikes(this.world,
            this.manager.getExistingIslandLikes("island-1")));
    }


    @Test
    void testRemovingOneIslandKeepsTheOther()
    {
        this.createManager(LikeMode.LIKES);
        this.manager.addLike(mockUser(), this.island, this.world);
        this.manager.addLike(mockUser(), this.secondIsland, this.world);

        this.manager.removeObject("island-2");

        assertNull(this.manager.getExistingIslandLikes("island-2"));
        assertNotNull(this.manager.getExistingIslandLikes("island-1"));
        // Removed island does not stay in the top list.
        assertEquals(List.of("island-1"),
            this.manager.getTopByLikes(this.world).stream().map(LikesObject::getUniqueId).toList());
    }


    // ---------------------------------------------------------------------
    // Section: Ranking
    // ---------------------------------------------------------------------


    @Test
    void testRankingFollowsChanges()
    {
        this.createManager(LikeMode.LIKES);
        User first = mockUser();

        this.manager.addLike(first, this.island, this.world);
        this.manager.addLike(mockUser(), this.island, this.world);
        this.manager.addLike(mockUser(), this.secondIsland, this.world);

        assertEquals("island-1", this.manager.getSortedLikes(this.world).exact(0).getUniqueId());

        // Island 1 drops to 1 like, island 2 raises to 3.
        this.manager.removeLike(first, this.island, this.world);
        this.manager.addLike(mockUser(), this.secondIsland, this.world);
        this.manager.addLike(mockUser(), this.secondIsland, this.world);

        assertEquals("island-2", this.manager.getSortedLikes(this.world).exact(0).getUniqueId());
        assertEquals(2, this.manager.getSortedLikes(this.world).size());
        assertEquals(2, this.manager.getIslandRankByLikes(this.world,
            this.manager.getExistingIslandLikes("island-1")));
    }


    @Test
    void testLikesDislikesRanking()
    {
        this.stored.add(storedObject("liked", GAME_MODE, 5, 1));
        this.stored.add(storedObject("disliked", GAME_MODE, 1, 6));
        this.stored.add(storedObject("neutral", GAME_MODE, 5, 3));
        this.createManager(LikeMode.LIKES_DISLIKES);

        assertEquals(List.of("liked", "neutral", "disliked"),
            this.manager.getTopByLikes(this.world).stream().map(LikesObject::getUniqueId).toList());
        assertEquals(List.of("disliked", "neutral", "liked"),
            this.manager.getTopByDislikes(this.world).stream().map(LikesObject::getUniqueId).toList());
        assertEquals(List.of("liked", "neutral", "disliked"),
            this.manager.getTopByRank(this.world).stream().map(LikesObject::getUniqueId).toList());

        LikesObject disliked = this.manager.getExistingIslandLikes("disliked");
        assertEquals(3, this.manager.getIslandRankByLikes(this.world, disliked));
        assertEquals(1, this.manager.getIslandRankByDislikes(this.world, disliked));
        assertEquals(3, this.manager.getIslandRankByRank(this.world, disliked));
        // Stars are not tracked in this mode.
        assertEquals(-1, this.manager.getIslandRankByStars(this.world, disliked));
        assertTrue(this.manager.getTopByStars(this.world).isEmpty());
    }


    @Test
    void testStarsRanking()
    {
        this.createManager(LikeMode.STARS);

        this.manager.addStars(mockUser(), 2, this.island, this.world);
        this.manager.addStars(mockUser(), 5, this.secondIsland, this.world);
        this.manager.addStars(mockUser(), 4, this.secondIsland, this.world);

        assertEquals(List.of("island-2", "island-1"),
            this.manager.getTopByStars(this.world).stream().map(LikesObject::getUniqueId).toList());
        assertEquals(2, this.manager.getIslandRankByStars(this.world,
            this.manager.getExistingIslandLikes("island-1")));
        // Likes are not tracked in this mode.
        assertTrue(this.manager.getTopByLikes(this.world).isEmpty());
        assertTrue(this.manager.getTopByDislikes(this.world).isEmpty());
        assertTrue(this.manager.getTopByRank(this.world).isEmpty());
    }


    @Test
    void testTopIsLimitedToTen()
    {
        for (int i = 0; i < 15; i++)
        {
            this.stored.add(storedObject("island" + i, GAME_MODE, i, 0));
        }

        this.createManager(LikeMode.LIKES);

        List<LikesObject> top = this.manager.getTopByLikes(this.world);
        assertEquals(10, top.size());
        assertEquals("island14", top.get(0).getUniqueId());
        assertEquals(15, this.manager.getSortedLikes(this.world).size());
    }


    @Test
    void testRankOfUnknownObject()
    {
        this.createManager(LikeMode.LIKES);

        assertEquals(-1, this.manager.getIslandRankByLikes(this.world, storedObject("x", GAME_MODE, 1, 0)));
    }


    // ---------------------------------------------------------------------
    // Section: Reset, wipe and save
    // ---------------------------------------------------------------------


    @Test
    void testResetLikes()
    {
        this.settings.setLogHistory(true);
        this.createManager(LikeMode.LIKES_DISLIKES);
        this.manager.addLike(mockUser(), this.island, this.world);
        this.manager.addDislike(mockUser(), this.island, this.world);
        this.manager.addLike(mockUser(), this.secondIsland, this.world);

        this.manager.resetLikes(this.uuid, "island-1", this.world);

        LikesObject object = this.manager.getExistingIslandLikes("island-1");
        assertEquals(0, object.getLikes());
        assertEquals(0, object.getDislikes());
        assertTrue(object.getLikedBy().isEmpty());
        assertTrue(object.getDislikedBy().isEmpty());
        assertEquals(this.uuid.toString(),
            object.getHistory().get(object.getHistory().size() - 1).getData().get("user-id"));
        // The other island of the same owner is not affected, and ranking is updated.
        assertEquals(1, this.manager.getExistingIslandLikes("island-2").getLikes());
        assertEquals("island-2", this.manager.getSortedLikes(this.world).exact(0).getUniqueId());
        assertEquals(2, this.manager.getSortedLikes(this.world).size());
    }


    @Test
    void testRemoveObject() throws Exception
    {
        this.createManager(LikeMode.LIKES);
        this.manager.addLike(mockUser(), this.island, this.world);
        LikesObject object = this.manager.getExistingIslandLikes("island-1");

        this.manager.removeObject("island-1");

        assertNull(this.manager.getExistingIslandLikes("island-1"));
        assertTrue(this.manager.getSortedLikes(this.world).isEmpty());
        verify(this.handler).deleteObject(object);
    }


    @Test
    void testRemoveUnknownObject() throws Exception
    {
        this.createManager(LikeMode.LIKES);

        this.manager.removeObject("unknown");

        verify(this.handler, never()).deleteObject(any());
    }


    @Test
    void testWipeData() throws Exception
    {
        LikesObject wiped = storedObject("a", GAME_MODE, 3, 0);
        LikesObject kept = storedObject("c", "AcidIsland", 9, 0);
        this.stored.add(wiped);
        this.stored.add(kept);
        this.createManager(LikeMode.LIKES_DISLIKES);

        this.manager.wipeData(this.world);

        assertNull(this.manager.getExistingIslandLikes("a"));
        assertNotNull(this.manager.getExistingIslandLikes("c"));
        assertTrue(this.manager.getSortedLikes(GAME_MODE).isEmpty());
        assertEquals(1, this.manager.getSortedLikes("AcidIsland").size());
        verify(this.handler).deleteObject(wiped);
        verify(this.handler, never()).deleteObject(kept);
    }


    @Test
    void testSaveOnlyChangedObjects() throws Exception
    {
        LikesObject changed = storedObject("a", GAME_MODE, 3, 0);
        LikesObject unchanged = storedObject("b", GAME_MODE, 1, 0);
        this.stored.add(changed);
        this.stored.add(unchanged);
        this.createManager(LikeMode.LIKES);
        changed.setChanged(true);

        this.manager.save();

        verify(this.handler).saveObject(changed);
        verify(this.handler, never()).saveObject(unchanged);
    }
}
