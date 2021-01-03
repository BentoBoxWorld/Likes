///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.listeners;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.likes.LikesAddon;


/**
 * This listener resets all likes for given player island
 */
public class ResetListener implements Listener
{
    /**
     * Default constructor.
     *
     * @param addon Likes Addon
     */
    public ResetListener(LikesAddon addon)
    {
        this.addon = addon;
    }


    /**
     * This method handles Island Created event.
     *
     * @param event Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreated(IslandCreatedEvent event)
    {
        this.addon.getAddonManager().resetLikes(event.getPlayerUUID(),
            event.getIsland().getUniqueId(),
            event.getIsland().getWorld());
    }


    /**
     * This method handles Island Resetted event.
     *
     * @param event Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreated(IslandResettedEvent event)
    {
        if (this.addon.getSettings().isResetLikes())
        {
            this.addon.getAddonManager().resetLikes(event.getPlayerUUID(),
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
    private final LikesAddon addon;
}
