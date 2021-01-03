///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.events;


import org.bukkit.event.HandlerList;
import java.util.UUID;

import world.bentobox.bentobox.api.events.BentoBoxEvent;


/**
 * This event is fired when player adds dislike to someones island.
 */
public class DislikeAddEvent extends BentoBoxEvent
{
// ---------------------------------------------------------------------
// Section: Constructor
// ---------------------------------------------------------------------


    /**
     * Constructor LikeAddEvent creates a new LikeAddEvent instance.
     *
     * @param user of type User
     * @param islandId of type String
     */
    public DislikeAddEvent(UUID user, String islandId)
    {
        this.user = user;
        this.islandId = islandId;
    }


// ---------------------------------------------------------------------
// Section: Getters and Setters
// ---------------------------------------------------------------------


    /**
     * Method LikeAddEvent#getUser returns the user of this object.
     *
     * @return the user (type UUID) of this object.
     */
    public UUID getUser()
    {
        return user;
    }


    /**
     * Method LikeAddEvent#setUser sets new value for the user of this object.
     *
     * @param user new value for this object.
     */
    public void setUser(UUID user)
    {
        this.user = user;
    }


    /**
     * Method LikeAddEvent#getIslandId returns the islandId of this object.
     *
     * @return the islandId (type String) of this object.
     */
    public String getIslandId()
    {
        return islandId;
    }


    /**
     * Method LikeAddEvent#setIslandId sets new value for the islandId of this object.
     *
     * @param islandId new value for this object.
     */
    public void setIslandId(String islandId)
    {
        this.islandId = islandId;
    }


// ---------------------------------------------------------------------
// Section: Handler methods
// ---------------------------------------------------------------------


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    @Override
    public HandlerList getHandlers()
    {
        return DislikeAddEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return DislikeAddEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Instance Variables
// ---------------------------------------------------------------------


    /**
     * User who triggered event.
     */
    private UUID user;

    /**
     * Island that was affected.
     */
    private String islandId;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
