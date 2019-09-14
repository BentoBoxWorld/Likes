//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.listeners;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.likes.LikesAddon;


/**
 * This listener resets all likes for given player island
 */
public class ResetListener implements Listener
{
	/**
	 * Default constructor.
	 * @param addon Likes Addon
	 */
	public ResetListener(LikesAddon addon)
	{
		this.addon = addon;
	}


	/**
	 * Island reset event catcher.
	 * @param event Island Event
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onIslandReset(IslandEvent event)
	{
		if (event.getReason().equals(IslandEvent.Reason.CREATED) ||
			(this.addon.getSettings().isResetLikes() && event.getReason().equals(IslandEvent.Reason.RESETTED)))
		{
			this.addon.getManager().resetLikes(event.getPlayerUUID(),
				event.getIsland().getUniqueId(),
				event.getIsland().getWorld());
		}
	}


// ---------------------------------------------------------------------
// Section: Instance Variables
// ---------------------------------------------------------------------


	/**
	 * Likes addon instance
	 */
	private LikesAddon addon;
}
