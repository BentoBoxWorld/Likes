///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.managers;


import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.logs.LogEntry;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.events.DislikeAddEvent;
import world.bentobox.likes.events.DislikeRemoveEvent;
import world.bentobox.likes.events.LikeAddEvent;
import world.bentobox.likes.events.LikeRemoveEvent;
import world.bentobox.likes.events.StarsAddEvent;
import world.bentobox.likes.events.StarsRemoveEvent;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;
import world.bentobox.likes.utils.collections.IndexedTreeSet;


/**
 * This class manages Likes addon data.
 */
public class LikesManager
{
    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------


    /**
     * Default constructor.
     *
     * @param addon Likes Addon instance
     */
    public LikesManager(LikesAddon addon)
    {
        this.addon = addon;

        this.likesDatabase = new Database<>(addon, LikesObject.class);
        this.likesCache = new ConcurrentHashMap<>();

        // Save memory, by adding values to set if they are necessary.
        switch (this.addon.getSettings().getMode())
        {
            case LIKES -> this.sortedLikeCache = new ConcurrentHashMap<>();
            case LIKES_DISLIKES -> {
                this.sortedDislikeCache = new ConcurrentHashMap<>();
                this.sortedLikeCache = new ConcurrentHashMap<>();
                this.sortedRankCache = new ConcurrentHashMap<>();
            }
            case STARS -> this.sortedStarsCache = new ConcurrentHashMap<>();
        }

        this.load();
    }


    // ---------------------------------------------------------------------
    // Section: Load Methods
    // ---------------------------------------------------------------------


    /**
     * This method loads all like objects.
     */
    public void load()
    {
        this.likesCache.clear();

        switch (this.addon.getSettings().getMode())
        {
            case LIKES -> this.sortedLikeCache.clear();
            case LIKES_DISLIKES -> {
                this.sortedLikeCache.clear();
                this.sortedDislikeCache.clear();
                this.sortedRankCache.clear();
            }
            case STARS -> this.sortedStarsCache.clear();
        }

        this.addon.getLogger().info("Loading likes...");

        this.likesDatabase.loadObjects().forEach(this::load);
    }


    /**
     * This method loads given likeObject inside cache.
     *
     * @param likesObject Object that must be added to cache.
     */
    private void load(LikesObject likesObject)
    {
        // Add object into Island to LikeObject cache.
        this.likesCache.put(likesObject.getUniqueId(), likesObject);

        switch (this.addon.getSettings().getMode())
        {
            case LIKES -> {
                // Add object into GameMode to sorted by likes cache.
                this.sortedLikeCache.computeIfAbsent(likesObject.getGameMode(),
                        gameMode -> new IndexedTreeSet<>(LIKES_COMPARATOR)).
                    add(likesObject);
            }
            case LIKES_DISLIKES -> {
                // Add object into GameMode to sorted by likes cache.
                this.sortedLikeCache.computeIfAbsent(likesObject.getGameMode(),
                        gameMode -> new IndexedTreeSet<>(LIKES_DISLIKES_COMPARATOR)).
                    add(likesObject);

                // Add object into GameMode to sorted by dislikes cache.
                this.sortedDislikeCache.computeIfAbsent(likesObject.getGameMode(),
                        gameMode -> new IndexedTreeSet<>(DISLIKES_LIKES_COMPARATOR)).
                    add(likesObject);

                // Add object into GameMode to sorted by rank cache.
                this.sortedRankCache.computeIfAbsent(likesObject.getGameMode(),
                        gameMode -> new IndexedTreeSet<>(RANK_COMPARATOR)).
                    add(likesObject);
            }
            case STARS -> {
                // Add object into GameMode to sorted by likes cache.
                this.sortedStarsCache.computeIfAbsent(likesObject.getGameMode(),
                        gameMode -> new IndexedTreeSet<>(STARS_COMPARATOR)).
                    add(likesObject);
            }
        }
    }


    /**
     * This method checks if likes object with given UniqueId exist in database. If not, it creates new object for
     * that.
     *
     * @param uniqueID - uniqueID to add
     * @param gameMode - gameMode for like object.
     */
    @NonNull
    private LikesObject getIslandLikes(@NonNull String uniqueID, String gameMode)
    {
        if (this.likesCache.containsKey(uniqueID))
        {
            return this.likesCache.get(uniqueID);
        }

        // The player is not in the cache
        // Check if the player exists in the database

        if (this.likesDatabase.objectExists(uniqueID))
        {
            // Load player from database
            LikesObject data = this.likesDatabase.loadObject(uniqueID);
            // Store in cache
            if (data != null)
            {
                this.load(data);
                return data;
            }
            this.addon.logError("Could not load NULL likes data object for " + uniqueID + " in " + gameMode);
        }

        return newPlayer(gameMode, uniqueID);
    }


    private LikesObject newPlayer(String gameMode, @NonNull String uniqueID)
    {
        // Create the likes data
        LikesObject likesObject = new LikesObject();
        likesObject.setUniqueId(uniqueID);
        likesObject.setGameMode(gameMode);

        this.likesDatabase.saveObjectAsync(likesObject);
        // Add to cache
        this.load(likesObject);

        return likesObject;
    }


    /**
     * This method returns Island Likes Object from cache or create new one.
     *
     * @param islandId IslandId which LikesObject must be returned.
     * @param world Target world.
     * @return Likes Object for given island.
     */
    public LikesObject getIslandLikes(String islandId, World world)
    {
        return this.getIslandLikes(islandId, Utils.getGameMode(world));
    }


    /**
     * This method returns Island Likes Object from cache or create new one.
     *
     * @param islandId IslandId which LikesObject must be returned.
     * @param gameModeAddon Target GameMode addon.
     * @return Likes Object for given island.
     */
    public LikesObject getIslandLikes(String islandId, GameModeAddon gameModeAddon)
    {
        return this.getIslandLikes(islandId, gameModeAddon.getDescription().getName());
    }


    /**
     * This method skips creating new island objects and return existing ones.
     *
     * @param uniqueId Island Unique ID.
     * @return Likes Object for current island or null.
     */
    public LikesObject getExistingIslandLikes(String uniqueId)
    {
        return this.likesCache.getOrDefault(uniqueId, null);
    }


    // ---------------------------------------------------------------------
    // Section: Save methods
    // ---------------------------------------------------------------------


    /**
     * This method saves all cached values into database.
     */
    public void save()
    {
        this.likesCache.values().stream().filter(LikesObject::isChanged).forEach(this.likesDatabase::saveObjectAsync);
    }


    /**
     * Save likes object.
     *
     * @param object the object
     */
    private void saveLikesObject(LikesObject object)
    {
        if (object.isChanged())
        {
            object.setChanged(false);
            this.likesDatabase.saveObjectAsync(object);
        }
    }


    // ---------------------------------------------------------------------
    // Section: Wipe methods
    // ---------------------------------------------------------------------


    /**
     * This method removes all data from database that referee to given world.
     */
    public void wipeData(World world)
    {
        String gameMode = Utils.getGameMode(world);

        // Empty sorted cache

        switch (this.addon.getSettings().getMode())
        {
            case LIKES -> this.sortedLikeCache.remove(gameMode);
            case LIKES_DISLIKES -> {
                this.sortedLikeCache.remove(gameMode);
                this.sortedDislikeCache.remove(gameMode);
                this.sortedRankCache.remove(gameMode);
            }
            case STARS -> this.sortedStarsCache.remove(gameMode);
        }

        // Remove from database
        this.likesDatabase.loadObjects().stream().
            filter(likesObject -> gameMode.equalsIgnoreCase(likesObject.getGameMode())).
            forEach(likesObject -> {
                this.likesDatabase.deleteObject(likesObject);
                this.likesCache.remove(likesObject.getUniqueId());
            });
    }

    // ---------------------------------------------------------------------
    // Section: Like Methods
    // ---------------------------------------------------------------------


    /**
     * This method adds like from given user to target island, in given world.
     *
     * @param user User who adds like.
     * @param island Island which receive like.
     * @param world World where island is located.
     */
    public void addLike(User user, Island island, World world)
    {
        String gameMode = Utils.getGameMode(world);
        LikesObject object = this.getIslandLikes(island.getUniqueId(), gameMode);

        if (!object.hasLiked(user.getUniqueId()))
        {
            object.addLike(user.getUniqueId());

            // Log history
            if (this.addon.getSettings().isLogHistory())
            {
                object.addLogRecord(new LogEntry.Builder("ADD_LIKE").
                    data("user-id", user.toString()).
                    build());
            }

            String name = island.getName() == null || island.getName().isEmpty() ?
                this.addon.getPlayers().getName(island.getOwner()) : island.getName();

            Utils.sendMessage(user,
                user.getTranslation(Constants.CONVERSATIONS + "add-like", Constants.PARAMETER_NAME, name));

            // Send message to users
            if (this.addon.getSettings().isInformPlayers())
            {
                island.getMemberSet().stream().
                    map(User::getInstance).
                    filter(User::isOnline).
                    forEach(member -> Utils.sendMessage(member,
                        member.getTranslation(Constants.CONVERSATIONS + "player-add-like",
                            Constants.PARAMETER_NAME, user.getName())));
            }

            // Fire event
            this.addon.callEvent(new LikeAddEvent(user.getUniqueId(), island.getUniqueId()));

            // Save object
            this.saveLikesObject(object);
        }
    }


    /**
     * This method removes like from given user to target island, in given world.
     *
     * @param user User who removes like.
     * @param island Island which lost like.
     * @param world World where island is located.
     */
    public void removeLike(User user, Island island, World world)
    {
        String gameMode = Utils.getGameMode(world);
        LikesObject object = this.getIslandLikes(island.getUniqueId(), gameMode);

        if (object.hasLiked(user.getUniqueId()))
        {
            object.removeLike(user.getUniqueId());

            // Log history
            if (this.addon.getSettings().isLogHistory())
            {
                object.addLogRecord(new LogEntry.Builder("REMOVE_LIKE").
                    data("user-id", user.toString()).
                    build());
            }

            String name = island.getName() == null || island.getName().isEmpty() ?
                this.addon.getPlayers().getName(island.getOwner()) : island.getName();

            Utils.sendMessage(user,
                user.getTranslation(Constants.CONVERSATIONS + "remove-like", Constants.PARAMETER_NAME, name));

            // Send message to users
            if (this.addon.getSettings().isInformPlayers())
            {
                island.getMemberSet().stream().
                    map(User::getInstance).
                    filter(User::isOnline).
                    forEach(member -> Utils.sendMessage(member,
                        member.getTranslation(Constants.CONVERSATIONS + "player-remove-like",
                            Constants.PARAMETER_NAME, user.getName())));
            }

            // Fire event
            this.addon.callEvent(new LikeRemoveEvent(user.getUniqueId(), island.getUniqueId()));

            // Save object
            this.saveLikesObject(object);
        }
    }


    /**
     * This method returns if given player has liked target island, in given world.
     *
     * @param user User which need to be checked.
     * @param islandId Island which need to be checked.
     * @param world World where island is located.
     */
    public boolean hasLiked(UUID user, String islandId, World world)
    {
        return this.getIslandLikes(islandId, Utils.getGameMode(world)).hasLiked(user);
    }


    /**
     * This method adds dislike from given user to target island, in given world.
     *
     * @param user User who adds dislike.
     * @param island Island which receive dislike.
     * @param world World where island is located.
     */
    public void addDislike(User user, Island island, World world)
    {
        String gameMode = Utils.getGameMode(world);
        LikesObject object = this.getIslandLikes(island.getUniqueId(), gameMode);

        if (!object.hasDisliked(user.getUniqueId()))
        {
            object.addDislike(user.getUniqueId());

            // Log history
            if (this.addon.getSettings().isLogHistory())
            {
                object.addLogRecord(new LogEntry.Builder("ADD_DISLIKE").
                    data("user-id", user.toString()).
                    build());
            }

            String name = island.getName() == null || island.getName().isEmpty() ?
                this.addon.getPlayers().getName(island.getOwner()) : island.getName();

            Utils.sendMessage(user,
                user.getTranslation(Constants.CONVERSATIONS + "add-dislike", Constants.PARAMETER_NAME, name));

            // Send message to users
            if (this.addon.getSettings().isInformPlayers())
            {
                island.getMemberSet().stream().
                    map(User::getInstance).
                    filter(User::isOnline).
                    forEach(member -> Utils.sendMessage(member,
                        member.getTranslation(Constants.CONVERSATIONS + "player-add-dislike",
                            Constants.PARAMETER_NAME, user.getName())));
            }

            // Fire event
            this.addon.callEvent(new DislikeAddEvent(user.getUniqueId(), island.getUniqueId()));

            // Save object
            this.saveLikesObject(object);
        }
    }


    /**
     * This method removes dislike from given user to target island, in given world.
     *
     * @param user User who adds dislike.
     * @param island Island which lost dislike.
     * @param world World where island is located.
     */
    public void removeDislike(User user, Island island, World world)
    {
        String gameMode = Utils.getGameMode(world);
        LikesObject object = this.getIslandLikes(island.getUniqueId(), gameMode);

        if (object.hasDisliked(user.getUniqueId()))
        {
            object.removeDislike(user.getUniqueId());

            // Log history
            if (this.addon.getSettings().isLogHistory())
            {
                object.addLogRecord(new LogEntry.Builder("REMOVE_DISLIKE").
                    data("user-id", user.toString()).
                    build());
            }

            String name = island.getName() == null || island.getName().isEmpty() ?
                this.addon.getPlayers().getName(island.getOwner()) : island.getName();

            Utils.sendMessage(user,
                user.getTranslation(Constants.CONVERSATIONS + "remove-dislike", Constants.PARAMETER_NAME, name));

            // Send message to users
            if (this.addon.getSettings().isInformPlayers())
            {
                island.getMemberSet().stream().
                    map(User::getInstance).
                    filter(User::isOnline).
                    forEach(member -> Utils.sendMessage(member,
                        member.getTranslation(Constants.CONVERSATIONS + "player-remove-dislike",
                            Constants.PARAMETER_NAME, user.getName())));
            }

            // Fire event
            this.addon.callEvent(new DislikeRemoveEvent(user.getUniqueId(), island.getUniqueId()));

            // Save object
            this.saveLikesObject(object);
        }
    }


    /**
     * This method returns if given player has disliked target island, in given world.
     *
     * @param user User which need to be checked.
     * @param islandId Island which need to be checked.
     * @param world World where island is located.
     */
    public boolean hasDisliked(UUID user, String islandId, World world)
    {
        return this.getIslandLikes(islandId, Utils.getGameMode(world)).hasDisliked(user);
    }


    /**
     * This method adds number of stars from given user to target island, in given world.
     *
     * @param user User who adds stars.
     * @param value Value of the stars.
     * @param island Island which receive stars.
     * @param world World where island is located.
     */
    public void addStars(User user, int value, Island island, World world)
    {
        String gameMode = Utils.getGameMode(world);
        LikesObject object = this.getIslandLikes(island.getUniqueId(), gameMode);

        if (!object.hasStarred(user.getUniqueId()))
        {
            object.addStars(user.getUniqueId(), value);

            // Log history
            if (this.addon.getSettings().isLogHistory())
            {
                object.addLogRecord(new LogEntry.Builder("ADD_STARS").
                    data("user-id", user.toString()).
                    data("value", Integer.toString(value)).
                    build());
            }

            String name = island.getName() == null || island.getName().isEmpty() ?
                this.addon.getPlayers().getName(island.getOwner()) : island.getName();

            Utils.sendMessage(user,
                user.getTranslation(Constants.CONVERSATIONS + "add-stars",
                Constants.PARAMETER_NAME, name,
                Constants.PARAMETER_NUMBER, String.valueOf(value)));

            // Send message to users
            if (this.addon.getSettings().isInformPlayers())
            {
                island.getMemberSet().stream().
                    map(User::getInstance).
                    filter(User::isOnline).
                    forEach(member -> Utils.sendMessage(member,
                        member.getTranslation(Constants.CONVERSATIONS + "player-add-stars",
                            Constants.PARAMETER_NAME, user.getName(),
                            Constants.PARAMETER_NUMBER, Integer.toString(value))));
            }

            // Fire event
            this.addon.callEvent(new StarsAddEvent(user.getUniqueId(), value, island.getUniqueId()));

            // Save object
            this.saveLikesObject(object);
        }
    }


    /**
     * This method removes like from given user to target island, in given world.
     *
     * @param user User who removes like.
     * @param island Island which lost like.
     * @param world World where island is located.
     */
    public void removeStars(User user, Island island, World world)
    {
        String gameMode = Utils.getGameMode(world);
        LikesObject object = this.getIslandLikes(island.getUniqueId(), gameMode);

        if (object.hasStarred(user.getUniqueId()))
        {
            object.removeStars(user.getUniqueId());

            // Log history
            if (this.addon.getSettings().isLogHistory())
            {
                object.addLogRecord(new LogEntry.Builder("REMOVE_STARS").
                    data("user-id", user.toString()).
                    build());
            }

            String name = island.getName() == null || island.getName().isEmpty() ?
                this.addon.getPlayers().getName(island.getOwner()) : island.getName();

            Utils.sendMessage(user,
                user.getTranslation(Constants.CONVERSATIONS + "remove-stars", Constants.PARAMETER_NAME, name));

            // Send message to users
            if (this.addon.getSettings().isInformPlayers())
            {
                island.getMemberSet().stream().
                    map(User::getInstance).
                    filter(User::isOnline).
                    forEach(member -> Utils.sendMessage(member,
                        member.getTranslation(Constants.CONVERSATIONS + "player-remove-stars",
                            Constants.PARAMETER_NAME, user.getName())));
            }

            // Fire event
            this.addon.callEvent(new StarsRemoveEvent(user.getUniqueId(), island.getUniqueId()));

            // Save object
            this.saveLikesObject(object);
        }
    }


    /**
     * This method returns if given player has liked target island, in given world.
     *
     * @param user User which need to be checked.
     * @param islandId Island which need to be checked.
     * @param world World where island is located.
     */
    public boolean hasStarred(UUID user, String islandId, World world)
    {
        return this.getIslandLikes(islandId, Utils.getGameMode(world)).hasStarred(user);
    }


    /**
     * This method returns if given player has added starts to target island, in given world.
     *
     * @param user User which need to be checked.
     * @param islandId Island which need to be checked.
     * @param world World where island is located.
     * @return integer from 0-5 with star count.
     */
    public int getStarred(UUID user, String islandId, World world)
    {
        return this.getIslandLikes(islandId, Utils.getGameMode(world)).getStarred(user);
    }


    /**
     * This method resets likes and dislikes for current island.
     *
     * @param user User who reset island.
     * @param islandId Island Id.
     * @param world World where island is located.
     */
    public void resetLikes(UUID user, String islandId, World world)
    {
        String gameMode = Utils.getGameMode(world);
        LikesObject object = this.getIslandLikes(islandId, gameMode);

        object.setLikes(0L);
        object.setDislikes(0L);
        object.setStars(0L);

        object.clearCollections();

        if (this.addon.getSettings().isLogHistory())
        {
            object.addLogRecord(new LogEntry.Builder("RESET_ISLAND").
                data("user-id", user.toString()).
                build());
        }

        // Save object
        this.saveLikesObject(object);
    }


    /**
     * Remove object from database.
     *
     * @param islandId the island id
     */
    public void removeObject(String islandId)
    {
        LikesObject object = this.getExistingIslandLikes(islandId);

        if (object != null)
        {
            this.likesCache.remove(islandId);
            this.likesDatabase.deleteObject(object);
        }
    }


    // ---------------------------------------------------------------------
    // Section: Methods to get data
    // ---------------------------------------------------------------------


    /**
     * This method returns top 10 islands by likes.
     *
     * @param world World where top list must be found.
     * @return List that contains max 10 elements where ordered by likes.
     */
    public List<LikesObject> getTopByLikes(World world)
    {
        return this.getSortedLikes(world).stream().
//            filter(LikesObject::isNotEmpty).
            limit(10).
            collect(Collectors.toList());
    }


    /**
     * This method returns top 10 islands by dislikes.
     *
     * @param world World where top list must be found.
     * @return List that contains max 10 elements where ordered by dislikes.
     */
    public List<LikesObject> getTopByDislikes(World world)
    {
        return this.getSortedDislikes(world).stream().
//            filter(LikesObject::isNotEmpty).
            limit(10).
            collect(Collectors.toList());
    }


    /**
     * This method returns top 10 islands by rank.
     *
     * @param world World where top list must be found.
     * @return List that contains max 10 elements where ordered by rank.
     */
    public List<LikesObject> getTopByRank(World world)
    {
        return this.getSortedRank(world).stream().
//            filter(LikesObject::isNotEmpty).
            limit(10).
            collect(Collectors.toList());
    }


    /**
     * This method returns top 10 islands by stars.
     *
     * @param world World where top list must be found.
     * @return List that contains max 10 elements where ordered by stars.
     */
    public List<LikesObject> getTopByStars(World world)
    {
        return this.getSortedStars(world).stream().
//            filter(LikesObject::isNotEmpty).
            limit(10).
            collect(Collectors.toList());
    }


    /**
     * This method returns Indexed Tree Set with Likes Object ordered by like count.
     *
     * @param world Target world
     * @return Indexed Tree Set where likes objects are ordered by like count.
     */
    public IndexedTreeSet<LikesObject> getSortedLikes(World world)
    {
        return this.getSortedLikes(Utils.getGameMode(world));
    }


    /**
     * This method returns Indexed Tree Set with Likes Object ordered by like count.
     *
     * @param gameMode Target GameMode addon name.
     * @return Indexed Tree Set where likes objects are ordered by like count.
     */
    public IndexedTreeSet<LikesObject> getSortedLikes(String gameMode)
    {
        return !this.addon.getSettings().getMode().equals(Settings.LikeMode.STARS) &&
            this.sortedLikeCache.containsKey(gameMode) ?
            this.sortedLikeCache.get(gameMode) :
            new IndexedTreeSet<>(Comparator.
                comparingLong(LikesObject::getLikes).
                thenComparing(LikesObject::getUniqueId).
                reversed());
    }


    /**
     * This method returns Indexed Tree Set with Likes Object ordered by dislike count.
     *
     * @param world Target world
     * @return Indexed Tree Set where likes objects are ordered by dislike count.
     */
    public IndexedTreeSet<LikesObject> getSortedDislikes(World world)
    {
        return this.getSortedDislikes(Utils.getGameMode(world));
    }


    /**
     * This method returns Indexed Tree Set with Likes Object ordered by dislike count.
     *
     * @param gameMode Target GameMode addon name.
     * @return Indexed Tree Set where likes objects are ordered by dislike count.
     */
    public IndexedTreeSet<LikesObject> getSortedDislikes(String gameMode)
    {
        return this.addon.getSettings().getMode().equals(Settings.LikeMode.LIKES_DISLIKES) &&
            this.sortedDislikeCache.containsKey(gameMode) ?
            this.sortedDislikeCache.get(gameMode) :
            new IndexedTreeSet<>(Comparator.
                comparingLong(LikesObject::getDislikes).
                thenComparing(Comparator.comparingLong(LikesObject::getLikes).reversed()).
                thenComparing(LikesObject::getUniqueId).
                reversed());
    }


    /**
     * This method returns Indexed Tree Set with Likes Object ordered by rank.
     *
     * @param world Target world
     * @return Indexed Tree Set where likes objects are ordered by rank.
     */
    public IndexedTreeSet<LikesObject> getSortedRank(World world)
    {
        return this.getSortedRank(Utils.getGameMode(world));
    }


    /**
     * This method returns Indexed Tree Set with Likes Object ordered by rank.
     *
     * @param gameMode Target GameMode addon name.
     * @return Indexed Tree Set where likes objects are ordered by rank.
     */
    public IndexedTreeSet<LikesObject> getSortedRank(String gameMode)
    {
        return this.addon.getSettings().getMode().equals(Settings.LikeMode.LIKES_DISLIKES) &&
            this.sortedRankCache.containsKey(gameMode) ?
            this.sortedRankCache.get(gameMode) :
            new IndexedTreeSet<>(Comparator.
                comparingLong(LikesObject::getRank).
                thenComparingLong(LikesObject::getLikes).
                thenComparing(Comparator.comparingLong(LikesObject::getDislikes).reversed()).
                thenComparing(LikesObject::getUniqueId).
                reversed());
    }


    /**
     * This method returns Indexed Tree Set with Likes Object ordered by stars.
     *
     * @param world Target world
     * @return Indexed Tree Set where likes objects are ordered by stars.
     */
    public IndexedTreeSet<LikesObject> getSortedStars(World world)
    {
        return this.getSortedStars(Utils.getGameMode(world));
    }


    /**
     * This method returns Indexed Tree Set with Likes Object ordered by stars value.
     *
     * @param gameMode Target GameMode addon name.
     * @return Indexed Tree Set where likes objects are ordered by stars value.
     */
    public IndexedTreeSet<LikesObject> getSortedStars(String gameMode)
    {
        return this.addon.getSettings().getMode().equals(Settings.LikeMode.STARS) &&
            this.sortedStarsCache.containsKey(gameMode) ?
            this.sortedStarsCache.get(gameMode) :
            new IndexedTreeSet<>(Comparator.
                comparingDouble(LikesObject::getStarsValue).
                thenComparingInt(LikesObject::numberOfStars).
                thenComparing(LikesObject::getUniqueId).
                reversed());
    }


    /**
     * Gets island rank by likes.
     *
     * @param world the world
     * @param likesObject the likes object
     * @return the island rank by likes
     */
    public int getIslandRankByLikes(World world, LikesObject likesObject)
    {
        IndexedTreeSet<LikesObject> sorted = this.getSortedLikes(world);
        return sorted.contains(likesObject) ? sorted.entryIndex(likesObject) + 1 : -1;
    }


    /**
     * Gets island rank by dislikes.
     *
     * @param world the world
     * @param likesObject the likes object
     * @return the island rank by dislikes
     */
    public int getIslandRankByDislikes(World world, LikesObject likesObject)
    {
        IndexedTreeSet<LikesObject> sorted = this.getSortedDislikes(world);
        return sorted.contains(likesObject) ? sorted.entryIndex(likesObject) + 1 : -1;
    }


    /**
     * Gets island rank by rank.
     *
     * @param world the world
     * @param likesObject the likes object
     * @return the island rank by rank
     */
    public int getIslandRankByRank(World world, LikesObject likesObject)
    {
        IndexedTreeSet<LikesObject> sorted = this.getSortedRank(world);
        return sorted.contains(likesObject) ? sorted.entryIndex(likesObject) + 1 : -1;
    }


    /**
     * Gets island rank by stars.
     *
     * @param world the world
     * @param likesObject the likes object
     * @return the island rank by stars
     */
    public int getIslandRankByStars(World world, LikesObject likesObject)
    {
        IndexedTreeSet<LikesObject> sorted = this.getSortedStars(world);
        return sorted.contains(likesObject) ? sorted.entryIndex(likesObject) + 1 : -1;
    }


    // ---------------------------------------------------------------------
    // Section: Instance Variables
    // ---------------------------------------------------------------------


    /**
     * Likes Addon instance.
     */
    private final LikesAddon addon;

    /**
     * This config object stores all likes objects.
     */
    private final Database<LikesObject> likesDatabase;

    /**
     * This map contains all likes object linked to their reference island. This will be faster way how to find out if
     * island has or has not likes.
     */
    private final Map<String, LikesObject> likesCache;

    /**
     * This map links GameMode's to liked islands sorted by likes. It should be cached, because of PlaceHolders.
     */
    private Map<String, IndexedTreeSet<LikesObject>> sortedLikeCache;

    /**
     * This map links GameMode's to liked islands sorted by dislikes. It should be cached, because of PlaceHolders.
     */
    private Map<String, IndexedTreeSet<LikesObject>> sortedDislikeCache;

    /**
     * This map links GameMode's to liked islands sorted by rank. It should be cached, because of PlaceHolders.
     */
    private Map<String, IndexedTreeSet<LikesObject>> sortedRankCache;

    /**
     * This map links GameMode's to liked islands sorted by rank. It should be cached, because of PlaceHolders.
     */
    private Map<String, IndexedTreeSet<LikesObject>> sortedStarsCache;

    // ---------------------------------------------------------------------
    // Section: Static variables
    // ---------------------------------------------------------------------

    /**
     * This comparator is used to sort by highest likes and smallest dislikes number.
     */
    public static final Comparator<LikesObject> LIKES_DISLIKES_COMPARATOR = (i1, i2) -> {
        int likes = Long.compare(i2.getLikes(), i1.getLikes());

        if (likes != 0)
        {
            return likes;
        }

        int dislikes = Long.compare(i1.getDislikes(), i2.getDislikes());

        if (dislikes != 0)
        {
            return dislikes;
        }

        return i1.getUniqueId().compareTo(i2.getUniqueId());
    };

    /**
     * This comparator is used to sort by highest dislikes and smallest likes number.
     */
    public static final Comparator<LikesObject> DISLIKES_LIKES_COMPARATOR = (i1, i2) -> {
        int dislikes = Long.compare(i2.getDislikes(), i1.getDislikes());

        if (dislikes != 0)
        {
            return dislikes;
        }

        int likes = Long.compare(i1.getLikes(), i2.getLikes());

        if (likes != 0)
        {
            return likes;
        }

        return i1.getUniqueId().compareTo(i2.getUniqueId());
    };

    /**
     * This comparator is used to sort by highest rank and likes and smallest dislikes number.
     */
    public static final Comparator<LikesObject> RANK_COMPARATOR = (i1, i2) -> {
        int rank = Long.compare(i2.getRank(), i1.getRank());

        if (rank != 0)
        {
            return rank;
        }

        int likes = Long.compare(i2.getLikes(), i1.getLikes());

        if (likes != 0)
        {
            return likes;
        }

        int dislikes = Long.compare(i1.getDislikes(), i2.getDislikes());

        if (dislikes != 0)
        {
            return dislikes;
        }

        return i1.getUniqueId().compareTo(i2.getUniqueId());
    };

    /**
     * This comparator is used to sort by highest likes number.
     */
    public static final Comparator<LikesObject> LIKES_COMPARATOR = (i1, i2) -> {
        int likes = Long.compare(i2.getLikes(), i1.getLikes());

        if (likes != 0)
        {
            return likes;
        }

        return i1.getUniqueId().compareTo(i2.getUniqueId());
    };

    /**
     * This comparator is used to sort by highest stars number.
     */
    public static final Comparator<LikesObject> STARS_COMPARATOR = (i1, i2) -> {
        int stars = Double.compare(i2.getStarsValue(), i1.getStarsValue());

        if (stars != 0)
        {
            return stars;
        }

        int count = Long.compare(i2.getStars(), i1.getStars());

        if (count != 0)
        {
            return count;
        }

        return i1.getUniqueId().compareTo(i2.getUniqueId());
    };
}
