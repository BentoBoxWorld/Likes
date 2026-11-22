package world.bentobox.likes.placeholders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.likes.CommonTestSetup;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.managers.LikesManager;
import world.bentobox.likes.utils.collections.IndexedTreeSet;

/**
 * Tests for {@link LikesAddonPlaceholder} and {@link LikesAddonPlaceholderType}.
 */
class LikesAddonPlaceholderTest extends CommonTestSetup
{
    private static final String GAME_MODE = "BSkyBlock";

    @Mock
    private LikesAddon addon;

    @Mock
    private LikesManager manager;

    @Mock
    private GameModeAddon gameModeAddon;

    @Mock
    private PlayersManager playersManager;

    @Mock
    private User user;

    @Mock
    private Island secondIsland;

    private LikesObject first;

    private LikesObject second;

    private IndexedTreeSet<LikesObject> sorted;


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        AddonDescription description = new AddonDescription.Builder("main", GAME_MODE, "1.0").build();
        when(this.gameModeAddon.getDescription()).thenReturn(description);
        when(this.gameModeAddon.getOverWorld()).thenReturn(this.world);
        when(this.gameModeAddon.getIslands()).thenReturn(this.im);

        when(this.addon.getAddonManager()).thenReturn(this.manager);
        when(this.addon.getIslands()).thenReturn(this.im);
        when(this.addon.getPlayers()).thenReturn(this.playersManager);
        when(this.playersManager.getName(this.uuid)).thenReturn("tastybento");

        when(this.user.getUniqueId()).thenReturn(this.uuid);
        when(this.user.isPlayer()).thenReturn(true);
        when(this.user.getLocation()).thenReturn(this.location);

        // Player has two islands. First is primary one.
        when(this.island.getUniqueId()).thenReturn("island-1");
        when(this.island.getWorld()).thenReturn(this.world);
        when(this.island.getName()).thenReturn("First");
        when(this.secondIsland.getUniqueId()).thenReturn("island-2");
        when(this.secondIsland.getWorld()).thenReturn(this.world);
        when(this.secondIsland.getOwner()).thenReturn(this.uuid);
        when(this.secondIsland.getMemberSet()).thenReturn(ImmutableSet.of(this.uuid));
        when(this.im.getIsland(this.world, this.uuid)).thenReturn(this.island);
        when(this.im.getIslandAt(any())).thenReturn(Optional.empty());
        when(this.im.getIslandById("island-1")).thenReturn(Optional.of(this.island));
        when(this.im.getIslandById("island-2")).thenReturn(Optional.of(this.secondIsland));

        this.first = new LikesObject();
        this.first.setUniqueId("island-1");
        this.first.setGameMode(GAME_MODE);
        this.first.addLike(UUID.randomUUID());
        this.first.addDislike(UUID.randomUUID());
        this.first.addDislike(UUID.randomUUID());
        this.first.addStars(UUID.randomUUID(), 3);

        this.second = new LikesObject();
        this.second.setUniqueId("island-2");
        this.second.setGameMode(GAME_MODE);
        this.second.addLike(UUID.randomUUID());
        this.second.addLike(UUID.randomUUID());
        this.second.addStars(UUID.randomUUID(), 5);

        when(this.manager.getIslandLikes("island-1", this.gameModeAddon)).thenReturn(this.first);
        when(this.manager.getIslandLikes("island-2", this.gameModeAddon)).thenReturn(this.second);
        when(this.manager.getIslandRankByLikes(this.world, this.first)).thenReturn(2);
        when(this.manager.getIslandRankByLikes(this.world, this.second)).thenReturn(1);
        when(this.manager.getIslandRankByDislikes(this.world, this.first)).thenReturn(1);
        when(this.manager.getIslandRankByRank(this.world, this.first)).thenReturn(2);
        when(this.manager.getIslandRankByStars(this.world, this.first)).thenReturn(2);

        this.sorted = new IndexedTreeSet<>(LikesManager.LIKES_COMPARATOR);
        this.sorted.add(this.first);
        this.sorted.add(this.second);
        when(this.manager.getSortedLikes(GAME_MODE)).thenReturn(this.sorted);
        when(this.manager.getSortedDislikes(GAME_MODE)).thenReturn(this.sorted);
        when(this.manager.getSortedRank(GAME_MODE)).thenReturn(this.sorted);
        when(this.manager.getSortedStars(GAME_MODE)).thenReturn(this.sorted);
    }


    private String replace(LikesAddonPlaceholderType type)
    {
        return new LikesAddonPlaceholder(this.addon, this.gameModeAddon, type).onReplace(this.user);
    }


    @Test
    void testNullUser()
    {
        assertEquals("", new LikesAddonPlaceholder(this.addon,
            this.gameModeAddon,
            LikesAddonPlaceholderType.ISLAND_LIKES).onReplace(null));
    }


    @Test
    void testPlaceholderNamesAreUnique()
    {
        Set<String> names = new HashSet<>();

        Arrays.stream(LikesAddonPlaceholderType.values()).forEach(type -> {
            assertNotNull(type.getReplacer());
            assertFalse(type.getPlaceholder().isBlank());
            assertTrue(names.add(type.getPlaceholder()), "Duplicate placeholder " + type.getPlaceholder());
        });
    }


    @Test
    void testIslandPlaceholdersUsePrimaryIsland()
    {
        assertEquals("1", this.replace(LikesAddonPlaceholderType.ISLAND_LIKES));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.ISLAND_LIKES_PLACE));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.ISLAND_DISLIKES));
        assertEquals("1", this.replace(LikesAddonPlaceholderType.ISLAND_DISLIKES_PLACE));
        assertEquals("-1", this.replace(LikesAddonPlaceholderType.ISLAND_RANK));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.ISLAND_RANK_PLACE));
        assertEquals("3.0", this.replace(LikesAddonPlaceholderType.ISLAND_STARS));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.ISLAND_STARS_PLACE));
    }


    @Test
    void testIslandPlaceholdersUseOwnIslandWherePlayerStands()
    {
        // Player walks to their second island.
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.secondIsland));

        assertEquals("2", this.replace(LikesAddonPlaceholderType.ISLAND_LIKES));
        assertEquals("1", this.replace(LikesAddonPlaceholderType.ISLAND_LIKES_PLACE));
        assertEquals("0", this.replace(LikesAddonPlaceholderType.ISLAND_DISLIKES));
        assertEquals("5.0", this.replace(LikesAddonPlaceholderType.ISLAND_STARS));
    }


    @Test
    void testIslandPlaceholdersOnForeignIslandUsePrimaryIsland()
    {
        when(this.secondIsland.getMemberSet()).thenReturn(ImmutableSet.of(UUID.randomUUID()));
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.secondIsland));

        assertEquals("1", this.replace(LikesAddonPlaceholderType.ISLAND_LIKES));
        // Visited placeholders show the island where player stands.
        assertEquals("2", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_LIKES));
    }


    @Test
    void testIslandPlaceholdersNoIsland()
    {
        when(this.im.getIsland(this.world, this.uuid)).thenReturn(null);

        assertEquals("", this.replace(LikesAddonPlaceholderType.ISLAND_LIKES));
        assertEquals("", this.replace(LikesAddonPlaceholderType.ISLAND_LIKES_PLACE));
        assertEquals("", this.replace(LikesAddonPlaceholderType.ISLAND_DISLIKES));
        assertEquals("", this.replace(LikesAddonPlaceholderType.ISLAND_DISLIKES_PLACE));
        assertEquals("", this.replace(LikesAddonPlaceholderType.ISLAND_RANK));
        assertEquals("", this.replace(LikesAddonPlaceholderType.ISLAND_RANK_PLACE));
        assertEquals("", this.replace(LikesAddonPlaceholderType.ISLAND_STARS));
        assertEquals("", this.replace(LikesAddonPlaceholderType.ISLAND_STARS_PLACE));
    }


    @Test
    void testVisitedPlaceholders()
    {
        when(this.im.getIslandAt(this.location)).thenReturn(Optional.of(this.island));

        assertEquals("1", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_LIKES));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_LIKES_PLACE));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_DISLIKES));
        assertEquals("1", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_DISLIKES_PLACE));
        assertEquals("-1", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_RANK));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_RANK_PLACE));
        assertEquals("3.0", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_STARS));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_STARS_PLACE));
    }


    @Test
    void testVisitedPlaceholdersNotOnIsland()
    {
        assertEquals("", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_LIKES));
        assertEquals("", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_LIKES_PLACE));
        assertEquals("", this.replace(LikesAddonPlaceholderType.VISITED_ISLAND_STARS));
    }


    @Test
    void testTopPlaceholders()
    {
        // Second island has more likes, so it is the first one.
        assertEquals("tastybento", this.replace(LikesAddonPlaceholderType.BY_LIKES_OWNER_NAME_1));
        assertEquals("tastybento", this.replace(LikesAddonPlaceholderType.BY_LIKES_ISLAND_NAME_1));
        assertEquals("2", this.replace(LikesAddonPlaceholderType.BY_LIKES_COUNT_1));
        assertEquals("tastybento", this.replace(LikesAddonPlaceholderType.BY_LIKES_OWNER_NAME_2));
        assertEquals("First", this.replace(LikesAddonPlaceholderType.BY_LIKES_ISLAND_NAME_2));
        assertEquals("1", this.replace(LikesAddonPlaceholderType.BY_LIKES_COUNT_2));
    }


    @Test
    void testTopPlaceholdersBeyondListAreEmpty()
    {
        assertEquals("", this.replace(LikesAddonPlaceholderType.BY_LIKES_OWNER_NAME_5));
        assertEquals("", this.replace(LikesAddonPlaceholderType.BY_LIKES_ISLAND_NAME_5));
        assertEquals("", this.replace(LikesAddonPlaceholderType.BY_LIKES_COUNT_8));
    }


    @Test
    void testTopPlaceholdersEmptyList()
    {
        this.sorted.clear();

        Arrays.stream(LikesAddonPlaceholderType.values()).
            filter(type -> type.name().startsWith("BY_")).
            forEach(type -> assertEquals("", this.replace(type), type.name()));
    }


    @Test
    void testTopPlaceholdersDeletedIsland()
    {
        when(this.im.getIslandById("island-2")).thenReturn(Optional.empty());

        assertEquals("", this.replace(LikesAddonPlaceholderType.BY_LIKES_OWNER_NAME_1));
        assertEquals("", this.replace(LikesAddonPlaceholderType.BY_LIKES_ISLAND_NAME_1));
    }


    @Test
    void testAllTopPlaceholdersResolve()
    {
        Arrays.stream(LikesAddonPlaceholderType.values()).
            filter(type -> type.name().startsWith("BY_")).
            forEach(type -> assertNotNull(this.replace(type), type.name()));
    }
}
