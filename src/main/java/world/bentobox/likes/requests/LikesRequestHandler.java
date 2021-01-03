///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.requests;


import org.bukkit.Bukkit;
import org.bukkit.World;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.database.objects.LikesObject;


/**
 * This method returns all likes data about requested island in requested world.
 */
public class LikesRequestHandler extends AddonRequestHandler
{
    /**
     * Constructor LikesRequestHandler creates a new LikesRequestHandler instance.
     *
     * @param addon of type LikesAddon
     */
    public LikesRequestHandler(LikesAddon addon)
    {
        super("island-likes");
        this.addon = addon;
    }


    @Override
    public Object handle(Map<String, Object> map)
    {
        /*
            What we need in the map:

            0. "world-name" -> String
            1. "island" -> String

            What we will return:

            - Empty map if invalid input
            - the map that contains all island values.
         */

        if (map == null || map.isEmpty() ||
            map.get("world-name") == null || !(map.get("world-name") instanceof String) ||
            map.get("island") == null || !(map.get("island") instanceof String) ||
            Bukkit.getWorld((String) map.get("world-name")) == null)
        {
            return Collections.emptyMap();
        }

        World world = Bukkit.getWorld((String) map.get("world-name"));

        LikesObject likesObject = this.addon.getAddonManager().getIslandLikes((String) map.get("island"), world);

        Map<String, Object> returnMap = new HashMap<>(8);
        returnMap.put("likes", likesObject.getLikes());
        returnMap.put("dislikes", likesObject.getDislikes());
        returnMap.put("rank", likesObject.getRank());
        returnMap.put("stars", likesObject.getStarsValue());

        returnMap.put("placeByLikes", this.addon.getAddonManager().getSortedLikes(world).entryIndex(likesObject));
        returnMap.put("placeByDislikes", this.addon.getAddonManager().getSortedDislikes(world).entryIndex(likesObject));
        returnMap.put("placeByRank", this.addon.getAddonManager().getSortedRank(world).entryIndex(likesObject));
        returnMap.put("placeByStars", this.addon.getAddonManager().getSortedStars(world).entryIndex(likesObject));

        returnMap.put("likedBy", likesObject.getLikedBy());
        returnMap.put("dislikedBy", likesObject.getDislikedBy());
        returnMap.put("staredBy", likesObject.getStarredBy());

        return returnMap;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Likes addon instance.
     */
    private final LikesAddon addon;
}
