//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.database.objects;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.api.logs.LogEntry;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.adapters.Adapter;
import world.bentobox.bentobox.database.objects.adapters.LogEntryListAdapter;


/**
 * Object that holds number of likes and dislikes.
 */
public class LikesObject implements DataObject
{
	/**
	 * Constructor LikesObject creates a new LikesObject instance.
	 */
	public LikesObject()
	{
		// Empty constructor
	}


// ---------------------------------------------------------------------
// Section: Process methods
// ---------------------------------------------------------------------


	/**
	 * This method adds like in current object.
	 * @param user User who liked island.
	 */
	public void addLike(UUID user)
	{
		this.removeDislike(user);

		this.likedBy.add(user);
		this.likes++;
	}


	/**
	 * This method removes like from current object.
	 * @param user User who remove like from island.
	 */
	public void removeLike(UUID user)
	{
		if (this.likedBy.remove(user))
		{
			// Reduce only if player is in likedBy set.
			this.likes--;
		}
	}


	/**
	 * This method adds dislike in current object.
	 * @param user User who disliked island.
	 */
	public void addDislike(UUID user)
	{
		this.removeLike(user);

		this.dislikedBy.add(user);
		this.dislikes++;
	}


	/**
	 * This method removes dislike from current object.
	 * @param user User who remove dislike from island.
	 */
	public void removeDislike(UUID user)
	{
		if (this.dislikedBy.remove(user))
		{
			// Reduce only if player is in dislikedBy set.
			this.dislikes--;
		}
	}


	/**
	 * This method adds given log entry to history.
	 * @param entry Log Entry that should be added to history.
	 */
	public void addLogRecord(LogEntry entry)
	{
		this.history.add(entry);
	}


	/**
	 * This method returns if given user is already liked this island.
	 * @param user User UUID which need to check.
	 * @return {@code true} if user has already liked this island, {@code false} - otherwise.
	 */
	public boolean hasLiked(UUID user)
	{
		return this.likedBy.contains(user);
	}


	/**
	 * This method returns if given user is already disliked this island.
	 * @param user User UUID which need to check.
	 * @return {@code true} if user has already disliked this island, {@code false} - otherwise.
	 */
	public boolean hasDisliked(UUID user)
	{
		return this.dislikedBy.contains(user);
	}


	/**
	 * This method returns if current likes object is not empty - At least one likes or dislikes.
	 * @return {@code true} if current object has at least one likes or dislikes, {@code false} - otherwise.
	 */
	public boolean isNotEmpty()
	{
		return this.likes != 0 || this.dislikes != 0;
	}


// ---------------------------------------------------------------------
// Section: Getters and Setters
// ---------------------------------------------------------------------


	/**
	 * @return the uniqueId
	 */
	@Override
	public String getUniqueId()
	{
		return this.uniqueId;
	}


	/**
	 * @param uniqueId - unique ID the uniqueId to set
	 */
	@Override
	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}


	/**
	 * Method LikesObject#getLikes returns the likes of this object.
	 *
	 * @return the likes (type long) of this object.
	 */
	public long getLikes()
	{
		return likes;
	}


	/**
	 * Method LikesObject#setLikes sets new value for the likes of this object.
	 * @param likes new value for this object.
	 *
	 */
	public void setLikes(long likes)
	{
		this.likes = likes;
	}


	/**
	 * Method LikesObject#getDislikes returns the dislikes of this object.
	 *
	 * @return the dislikes (type long) of this object.
	 */
	public long getDislikes()
	{
		return dislikes;
	}


	/**
	 * Method LikesObject#setDislikes sets new value for the dislikes of this object.
	 * @param dislikes new value for this object.
	 *
	 */
	public void setDislikes(long dislikes)
	{
		this.dislikes = dislikes;
	}


	/**
	 * Method LikesObject#getLikedBy returns the likedBy of this object.
	 *
	 * @return the likedBy (type Set<UUID>) of this object.
	 */
	public Set<UUID> getLikedBy()
	{
		return likedBy;
	}


	/**
	 * Method LikesObject#setLikedBy sets new value for the likedBy of this object.
	 * @param likedBy new value for this object.
	 *
	 */
	public void setLikedBy(Set<UUID> likedBy)
	{
		this.likedBy = likedBy;
	}


	/**
	 * Method LikesObject#getDislikedBy returns the dislikedBy of this object.
	 *
	 * @return the dislikedBy (type Set<UUID>) of this object.
	 */
	public Set<UUID> getDislikedBy()
	{
		return dislikedBy;
	}


	/**
	 * Method LikesObject#setDislikedBy sets new value for the dislikedBy of this object.
	 * @param dislikedBy new value for this object.
	 *
	 */
	public void setDislikedBy(Set<UUID> dislikedBy)
	{
		this.dislikedBy = dislikedBy;
	}


	/**
	 * Method LikesObject#getGameMode returns the gameMode of this object.
	 *
	 * @return the gameMode (type String) of this object.
	 */
	public String getGameMode()
	{
		return gameMode;
	}


	/**
	 * Method LikesObject#setGameMode sets new value for the gameMode of this object.
	 * @param gameMode new value for this object.
	 *
	 */
	public void setGameMode(String gameMode)
	{
		this.gameMode = gameMode;
	}


	/**
	 * Method LikesObject#getHistory returns the history of this object.
	 *
	 * @return the history (type List<LogEntry>) of this object.
	 */
	public List<LogEntry> getHistory()
	{
		return history;
	}


	/**
	 * Method LikesObject#setHistory sets new value for the history of this object.
	 * @param history new value for this object.
	 *
	 */
	public void setHistory(List<LogEntry> history)
	{
		this.history = history;
	}


	/**
	 * This method returns difference between likes and dislikes.
	 * @return Likes - dislikes.
	 */
	public long getRank()
	{
		return this.likes - this.dislikes;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Likes of the island object.
	 */
	@Expose
	private long likes;

	/**
	 * Dislikes of the island.
	 */
	@Expose
	private long dislikes;

	/**
	 * GameMode where current object operates.
	 */
	@Expose
	private String gameMode;

	/**
	 * Likes object id. Island ID;
	 */
	@Expose
	private String uniqueId;

	/**
	 * Set that contains all players who clicked on like for current island.
	 */
	@Expose
	private Set<UUID> likedBy = new HashSet<>();

	/**
	 * Set that contains all players who clicked on dislike for current island.
	 */
	@Expose
	private Set<UUID> dislikedBy = new HashSet<>();

	/**
	 * Stores history about likes changes.
	 */
	@Adapter(LogEntryListAdapter.class)
	@Expose
	private List<LogEntry> history = new LinkedList<>();
}
