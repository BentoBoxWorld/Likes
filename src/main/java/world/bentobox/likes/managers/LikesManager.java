//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.managers;


import org.bukkit.World;
import org.eclipse.jdt.annotation.NonNull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.logs.LogEntry;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.events.DislikeAddEvent;
import world.bentobox.likes.events.DislikeRemoveEvent;
import world.bentobox.likes.events.LikeAddEvent;
import world.bentobox.likes.events.LikeRemoveEvent;
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
	 * @param addon Likes Addon instance
	 */
	public LikesManager(LikesAddon addon)
	{
		this.addon = addon;

		this.likesDatabase = new Database<>(addon, LikesObject.class);
		this.likesCache = new HashMap<>();

		this.sortedDislikeCache = new HashMap<>();
		this.sortedLikeCache = new HashMap<>();
		this.sortedRankCache = new HashMap<>();

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

		this.sortedLikeCache.clear();
		this.sortedDislikeCache.clear();
		this.sortedRankCache.clear();

		this.addon.getLogger().info("Loading likes...");

		this.likesDatabase.loadObjects().forEach(this::load);
	}


	/**
	 * This method loads given likeObject inside cache.
	 * @param likesObject Object that must be added to cache.
	 */
	private void load(LikesObject likesObject)
	{
		// Add object into Island to LikeObject cache.
		this.likesCache.put(likesObject.getUniqueId(), likesObject);

		// Add object into GameMode to sorted by likes cache.
		this.sortedLikeCache.computeIfAbsent(likesObject.getGameMode(),
			gameMode -> new IndexedTreeSet<>(Comparator.comparing(LikesObject::getLikes).reversed().
				thenComparing(LikesObject::getDislikes).
				thenComparing(LikesObject::getUniqueId))).add(likesObject);

		// Add object into GameMode to sorted by dislikes cache.
		this.sortedDislikeCache.computeIfAbsent(likesObject.getGameMode(),
			gameMode -> new IndexedTreeSet<>(Comparator.comparing(LikesObject::getDislikes).reversed().
				thenComparing(LikesObject::getLikes).
				thenComparing(LikesObject::getUniqueId))).add(likesObject);

		// Add object into GameMode to sorted by rank cache.
		this.sortedRankCache.computeIfAbsent(likesObject.getGameMode(),
			gameMode -> new IndexedTreeSet<>(Comparator.comparing(LikesObject::getRank).reversed().
				thenComparing(LikesObject::getLikes).reversed().
				thenComparing(LikesObject::getDislikes).reversed().
				thenComparing(LikesObject::getUniqueId))).add(likesObject);
	}



	/**
	 * This method checks if likes object with given UniqueId exist in database.
	 * If not, it creates new object for that.
	 *
	 * @param uniqueID - uniqueID to add
	 * @param gameMode - gameMode for like object.
	 */
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
			}
			else
			{
				this.addon.logError("Could not load NULL likes data object.");
			}

			return data;
		}
		else
		{
			// Create the likes data
			LikesObject likesObject = new LikesObject();
			likesObject.setUniqueId(uniqueID);
			likesObject.setGameMode(gameMode);

			this.likesDatabase.saveObject(likesObject);
			// Add to cache
			this.load(likesObject);

			return likesObject;
		}
	}


	/**
	 * This method returns Island Likes Object from cache or create new one.
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
		this.likesCache.values().forEach(this.likesDatabase::saveObject);
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
		this.sortedLikeCache.remove(gameMode);
		this.sortedDislikeCache.remove(gameMode);
		this.sortedRankCache.remove(gameMode);

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

			user.sendMessage(user.getTranslation(Constants.MESSAGE + "add-like", "[island]", name));

			// Send message to users
			if (this.addon.getSettings().isInformPlayers())
			{
				island.getMemberSet().stream().
					map(User::getInstance).
					filter(User::isOnline).
					forEach(member -> member.sendMessage(
						member.getTranslation(Constants.MESSAGE + "player-add-like", "[user]", user.getName())));
			}

			// Fire event
			this.addon.callEvent(new LikeAddEvent(user.getUniqueId(), island.getUniqueId()));
		}
	}


	/**
	 * This method removes like from given user to target island, in given world.
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

			user.sendMessage(user.getTranslation(Constants.MESSAGE + "remove-like", "[island]", name));

			// Send message to users
			if (this.addon.getSettings().isInformPlayers())
			{
				island.getMemberSet().stream().
					map(User::getInstance).
					filter(User::isOnline).
					forEach(member -> member.sendMessage(
						member.getTranslation(Constants.MESSAGE + "player-remove-like", "[user]", user.getName())));
			}

			// Fire event
			this.addon.callEvent(new LikeRemoveEvent(user.getUniqueId(), island.getUniqueId()));
		}
	}


	/**
	 * This method returns if given player has liked target island, in given world.
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

			user.sendMessage(user.getTranslation(Constants.MESSAGE + "add-dislike", "[island]", name));

			// Send message to users
			if (this.addon.getSettings().isInformPlayers())
			{
				island.getMemberSet().stream().
					map(User::getInstance).
					filter(User::isOnline).
					forEach(member -> member.sendMessage(
						member.getTranslation(Constants.MESSAGE + "player-add-dislike", "[user]", user.getName())));
			}

			// Fire event
			this.addon.callEvent(new DislikeAddEvent(user.getUniqueId(), island.getUniqueId()));
		}
	}


	/**
	 * This method removes dislike from given user to target island, in given world.
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

			user.sendMessage(user.getTranslation(Constants.MESSAGE + "remove-dislike", "[island]", name));

			// Send message to users
			if (this.addon.getSettings().isInformPlayers())
			{
				island.getMemberSet().stream().
					map(User::getInstance).
					filter(User::isOnline).
					forEach(member -> member.sendMessage(
						member.getTranslation(Constants.MESSAGE + "player-remove-dislike", "[user]", user.getName())));
			}

			// Fire event
			this.addon.callEvent(new DislikeRemoveEvent(user.getUniqueId(), island.getUniqueId()));
		}
	}


	/**
	 * This method returns if given player has disliked target island, in given world.
	 * @param user User which need to be checked.
	 * @param islandId Island which need to be checked.
	 * @param world World where island is located.
	 */
	public boolean hasDisliked(UUID user, String islandId, World world)
	{
		return this.getIslandLikes(islandId, Utils.getGameMode(world)).hasDisliked(user);
	}


	/**
	 * This method resets likes and dislikes for current island.
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

		if (this.addon.getSettings().isLogHistory())
		{
			object.addLogRecord( new LogEntry.Builder("RESET_ISLAND").
				data("user-id", user.toString()).
				build());
		}
	}


// ---------------------------------------------------------------------
// Section: Methods to get data
// ---------------------------------------------------------------------


	/**
	 * This method returns top 10 islands by likes.
	 * @param world World where top list must be found.
	 * @return List that contains max 10 elements where ordered by likes.
	 */
	public List<LikesObject> getTopByLikes(World world)
	{
		return this.getSortedLikes(world).stream().limit(10).filter(LikesObject::isNotEmpty).collect(Collectors.toList());
	}


	/**
	 * This method returns top 10 islands by dislikes.
	 * @param world World where top list must be found.
	 * @return List that contains max 10 elements where ordered by dislikes.
	 */
	public List<LikesObject> getTopByDislikes(World world)
	{
		return this.getSortedDislikes(world).stream().limit(10).filter(LikesObject::isNotEmpty).collect(Collectors.toList());
	}


	/**
	 * This method returns top 10 islands by rank.
	 * @param world World where top list must be found.
	 * @return List that contains max 10 elements where ordered by rank.
	 */
	public List<LikesObject> getTopByRank(World world)
	{
		return this.getSortedRank(world).stream().limit(10).filter(LikesObject::isNotEmpty).collect(Collectors.toList());
	}


	/**
	 * This method returns Indexed Tree Set with Likes Object ordered by like count.
	 * @param world Target world
	 * @return Indexed Tree Set where likes objects are ordered by like count.
	 */
	public IndexedTreeSet<LikesObject> getSortedLikes(World world)
	{
		return this.getSortedLikes(Utils.getGameMode(world));
	}


	/**
	 * This method returns Indexed Tree Set with Likes Object ordered by like count.
	 * @param gameMode Target GameMode addon name.
	 * @return Indexed Tree Set where likes objects are ordered by like count.
	 */
	public IndexedTreeSet<LikesObject> getSortedLikes(String gameMode)
	{
		return this.sortedLikeCache.containsKey(gameMode) ?
			this.sortedLikeCache.get(gameMode) :
			new IndexedTreeSet<>();
	}


	/**
	 * This method returns Indexed Tree Set with Likes Object ordered by dislike count.
	 * @param world Target world
	 * @return Indexed Tree Set where likes objects are ordered by dislike count.
	 */
	public IndexedTreeSet<LikesObject> getSortedDislikes(World world)
	{
		return this.getSortedDislikes(Utils.getGameMode(world));
	}


	/**
	 * This method returns Indexed Tree Set with Likes Object ordered by dislike count.
	 * @param gameMode Target GameMode addon name.
	 * @return Indexed Tree Set where likes objects are ordered by dislike count.
	 */
	public IndexedTreeSet<LikesObject> getSortedDislikes(String gameMode)
	{
		return this.sortedDislikeCache.containsKey(gameMode) ?
			this.sortedDislikeCache.get(gameMode) :
			new IndexedTreeSet<>();
	}


	/**
	 * This method returns Indexed Tree Set with Likes Object ordered by rank.
	 * @param world Target world
	 * @return Indexed Tree Set where likes objects are ordered by rank.
	 */
	public IndexedTreeSet<LikesObject> getSortedRank(World world)
	{
		return this.getSortedRank(Utils.getGameMode(world));
	}


	/**
	 * This method returns Indexed Tree Set with Likes Object ordered by rank.
	 * @param gameMode Target GameMode addon name.
	 * @return Indexed Tree Set where likes objects are ordered by rank.
	 */
	public IndexedTreeSet<LikesObject> getSortedRank(String gameMode)
	{
		return this.sortedRankCache.containsKey(gameMode) ?
			this.sortedRankCache.get(gameMode) :
			new IndexedTreeSet<>();
	}


// ---------------------------------------------------------------------
// Section: Instance Variables
// ---------------------------------------------------------------------


	/**
	 * Likes Addon instance.
	 */
	private LikesAddon addon;

	/**
	 * This config object stores all likes objects.
	 */
	private Database<LikesObject> likesDatabase;

	/**
	 * This map contains all likes object linked to their reference island.
	 * This will be faster way how to find out if island has or has not likes.
	 */
	private Map<String, LikesObject> likesCache;

	/**
	 * This map links GameMode's to liked islands sorted by likes.
	 * It should be cached, because of PlaceHolders.
	 */
	private Map<String, IndexedTreeSet<LikesObject>> sortedLikeCache;

	/**
	 * This map links GameMode's to liked islands sorted by dislikes.
	 * It should be cached, because of PlaceHolders.
	 */
	private Map<String, IndexedTreeSet<LikesObject>> sortedDislikeCache;

	/**
	 * This map links GameMode's to liked islands sorted by rank.
	 * It should be cached, because of PlaceHolders.
	 */
	private Map<String, IndexedTreeSet<LikesObject>> sortedRankCache;
}
