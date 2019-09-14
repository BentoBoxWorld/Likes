package world.bentobox.likes.requests;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings.VIEW_MODE;


/**
 * This Request Handler allows other plugins to get access to top 10 island list per particular world.
 * Handler returns linked hashmap from TopTenData for particular world.
 */
public class TopTenRequestHandler extends AddonRequestHandler
{
    /**
     * This constructor creates a new TopTenRequestHandler instance.
     *
     * @param addon of type LikesAddon
     */
    public TopTenRequestHandler(LikesAddon addon)
    {
        super("top-ten-likes");
        this.addon = addon;
    }


    /**
     * @see {@link AddonRequestHandler#handle(Map)}
     */
    @Override
    public Object handle(Map<String, Object> map)
    {
        /*
            What we need in the map:

            "world-name" -> String,
            "type" -> String

            What we will return:

            - Empty map if invalid input
            - the map of top ten island id's and likes. Can be less then 10.
         */

        if (map == null ||
            map.isEmpty() ||
            map.get("world-name") == null ||
            !(map.get("world-name") instanceof String) ||
            Bukkit.getWorld((String) map.get("world-name")) == null)
        {
            return Collections.emptyMap();
        }

        Map<String, Long> returnMap = new LinkedHashMap<>(10);

        switch (VIEW_MODE.getMode((String) map.getOrDefault("type", "likes")))
        {
            case LIKES:
                this.addon.getManager().getTopByLikes(Bukkit.getWorld((String) map.get("world-name"))).
                    forEach(likesObject -> returnMap.put(likesObject.getUniqueId(), likesObject.getLikes()));
                break;
            case DISLIKES:
                this.addon.getManager().getTopByDislikes(Bukkit.getWorld((String) map.get("world-name"))).
                    forEach(likesObject -> returnMap.put(likesObject.getUniqueId(), likesObject.getDislikes()));
                break;
            case RANK:
                this.addon.getManager().getTopByRank(Bukkit.getWorld((String) map.get("world-name"))).
                    forEach(likesObject -> returnMap.put(likesObject.getUniqueId(), likesObject.getRank()));
                break;
        }

        return returnMap;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Likes addon instance.
     */
    private LikesAddon addon;
}
