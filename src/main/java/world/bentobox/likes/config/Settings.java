///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.config;


import org.bukkit.Material;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;


/**
 * Settings that implements ConfigObject is powerful and dynamic Config Objects that does not need custom parsing. If it
 * is correctly loaded, all its values will be available.
 * <p>
 * Without Getter and Setter this class will not work.
 * <p>
 * To specify location for config object to be stored, you should use @StoreAt(filename="{config file name}",
 * path="{Path to your addon}") To save comments in config file you should use @ConfigComment("{message}") that adds any
 * message you want to be in file.
 */
@StoreAt(filename = "config.yml", path = "addons/Likes")
@ConfigComment("LikesAddon Configuration [version]")
@ConfigComment("This config file is dynamic and saved when the server is shutdown.")
@ConfigComment("")
public class Settings implements ConfigObject
{
// ---------------------------------------------------------------------
// Section: Getters and Setters
// ---------------------------------------------------------------------


    /**
     * This method returns the disabledGameModes value.
     *
     * @return the value of disabledGameModes.
     */
    public Set<String> getDisabledGameModes()
    {
        return disabledGameModes;
    }


    /**
     * This method sets the disabledGameModes value.
     *
     * @param disabledGameModes the disabledGameModes new value.
     */
    public void setDisabledGameModes(Set<String> disabledGameModes)
    {
        this.disabledGameModes = disabledGameModes;
    }


    /**
     * Method Settings#getDefaultIcon returns the defaultIcon of this object.
     *
     * @return the defaultIcon (type Material) of this object.
     */
    public Material getDefaultIcon()
    {
        return defaultIcon;
    }


    /**
     * Method Settings#setDefaultIcon sets new value for the defaultIcon of this object.
     *
     * @param defaultIcon new value for this object.
     */
    public void setDefaultIcon(Material defaultIcon)
    {
        this.defaultIcon = defaultIcon;
    }


    /**
     * Method Settings#isLogHistory returns the logHistory of this object.
     *
     * @return the logHistory (type boolean) of this object.
     */
    public boolean isLogHistory()
    {
        return logHistory;
    }


    /**
     * Method Settings#setLogHistory sets new value for the logHistory of this object.
     *
     * @param logHistory new value for this object.
     */
    public void setLogHistory(boolean logHistory)
    {
        this.logHistory = logHistory;
    }


    /**
     * Method Settings#isResetLikes returns the resetLikes of this object.
     *
     * @return the resetLikes (type boolean) of this object.
     */
    public boolean isResetLikes()
    {
        return resetLikes;
    }


    /**
     * Method Settings#setResetLikes sets new value for the resetLikes of this object.
     *
     * @param resetLikes new value for this object.
     */
    public void setResetLikes(boolean resetLikes)
    {
        this.resetLikes = resetLikes;
    }


    /**
     * Method Settings#getLikeAddCost returns the likeAddCost of this object.
     *
     * @return the likeAddCost (type double) of this object.
     */
    public double getLikeAddCost()
    {
        return likeAddCost;
    }


    /**
     * Method Settings#setLikeAddCost sets new value for the likeAddCost of this object.
     *
     * @param likeAddCost new value for this object.
     */
    public void setLikeAddCost(double likeAddCost)
    {
        this.likeAddCost = likeAddCost;
    }


    /**
     * Method Settings#getLikeRemoveCost returns the likeRemoveCost of this object.
     *
     * @return the likeRemoveCost (type double) of this object.
     */
    public double getLikeRemoveCost()
    {
        return likeRemoveCost;
    }


    /**
     * Method Settings#setLikeRemoveCost sets new value for the likeRemoveCost of this object.
     *
     * @param likeRemoveCost new value for this object.
     */
    public void setLikeRemoveCost(double likeRemoveCost)
    {
        this.likeRemoveCost = likeRemoveCost;
    }


    /**
     * Method Settings#getDislikeAddCost returns the dislikeAddCost of this object.
     *
     * @return the dislikeAddCost (type double) of this object.
     */
    public double getDislikeAddCost()
    {
        return dislikeAddCost;
    }


    /**
     * Method Settings#setDislikeAddCost sets new value for the dislikeAddCost of this object.
     *
     * @param dislikeAddCost new value for this object.
     */
    public void setDislikeAddCost(double dislikeAddCost)
    {
        this.dislikeAddCost = dislikeAddCost;
    }


    /**
     * Method Settings#getDislikeRemoveCost returns the dislikeRemoveCost of this object.
     *
     * @return the dislikeRemoveCost (type double) of this object.
     */
    public double getDislikeRemoveCost()
    {
        return dislikeRemoveCost;
    }


    /**
     * Method Settings#setDislikeRemoveCost sets new value for the dislikeRemoveCost of this object.
     *
     * @param dislikeRemoveCost new value for this object.
     */
    public void setDislikeRemoveCost(double dislikeRemoveCost)
    {
        this.dislikeRemoveCost = dislikeRemoveCost;
    }


    /**
     * Method Settings#isInformPlayers returns the informPlayers of this object.
     *
     * @return the informPlayers (type boolean) of this object.
     */
    public boolean isInformPlayers()
    {
        return informPlayers;
    }


    /**
     * Method Settings#setInformPlayers sets new value for the informPlayers of this object.
     *
     * @param informPlayers new value for this object.
     */
    public void setInformPlayers(boolean informPlayers)
    {
        this.informPlayers = informPlayers;
    }


    /**
     * This method returns the mode value.
     *
     * @return the value of mode.
     */
    public LikeMode getMode()
    {
        return mode;
    }


    /**
     * This method sets the mode value.
     *
     * @param mode the mode new value.
     */
    public void setMode(LikeMode mode)
    {
        this.mode = mode;
    }


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


    /**
     * This enum holds all possible values for Addon Like Storage Mode.
     */
    public enum LikeMode
    {
        /**
         * Store only Likes. Dislikes and rank is disabled.
         */
        LIKES,
        /**
         * Stores Likes and Dislikes.
         */
        LIKES_DISLIKES,
        /**
         * Stores Stars which has value from 1 to 5.
         */
        STARS
    }

    /**
     * Allows to select different Modes for viewing or choosing tops
     */
    public enum VIEW_MODE
    {
        LIKES,
        DISLIKES,
        RANK,
        STARS;


        /**
         * This method returns stored parameter from string.
         *
         * @param parameter String of object that must be returned
         * @return CommandParameters object or null.
         */
        public static VIEW_MODE getMode(String parameter)
        {
            return BY_NAME.get(parameter);
        }


        /**
         * This map allows to access all enum values via their string.
         */
        private static final Map<String, VIEW_MODE> BY_NAME = new HashMap<>();

        /*
         * This static method populated BY_NAME map.
         */
        static
        {
            for (VIEW_MODE mode : VIEW_MODE.values())
            {
                BY_NAME.put(mode.name(), mode);
            }
        }
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    @ConfigComment("")
    @ConfigComment("Default icon for Top Island panel.")
    @ConfigComment("Should be valid Material")
    @ConfigEntry(path = "default-icon")
    private Material defaultIcon = Material.PLAYER_HEAD;

    @ConfigComment("")
    @ConfigComment("Allows to enable ability to send message to island members about someone")
    @ConfigComment("liking or disliking their island.")
    @ConfigEntry(path = "inform-members")
    private boolean informPlayers;

    @ConfigComment("")
    @ConfigComment("Allows to switch addon between 3 different modes:")
    @ConfigComment("  - LIKES: allows only adding a like to island.")
    @ConfigComment("  - LIKES_DISLIKES: allows only adding a like and dislike to island.")
    @ConfigComment("  - STARS: allows adding stars from 1-5 to island.")
    @ConfigEntry(path = "mode")
    private LikeMode mode = LikeMode.LIKES_DISLIKES;

    @ConfigComment("")
    @ConfigComment("Allows to define cost for player to add like.")
    @ConfigEntry(path = "costs.add-like")
    private double likeAddCost;

    @ConfigComment("")
    @ConfigComment("Allows to define cost for player to remove like.")
    @ConfigEntry(path = "costs.remove-like")
    private double likeRemoveCost;

    @ConfigComment("")
    @ConfigComment("Allows to define cost for player to add dislike.")
    @ConfigEntry(path = "costs.add-dislike")
    private double dislikeAddCost;

    @ConfigComment("")
    @ConfigComment("Allows to define cost for player to remove dislike.")
    @ConfigEntry(path = "costs.remove-dislike")
    private double dislikeRemoveCost;

    @ConfigComment("")
    @ConfigComment("Allows reset likes and dislikes after resetting island.")
    @ConfigEntry(path = "reset-on-reset")
    private boolean resetLikes;

    @ConfigComment("")
    @ConfigComment("Allows to store history data about added and removed likes and dislikes.")
    @ConfigEntry(path = "log-history")
    private boolean logHistory;

    @ConfigComment("")
    @ConfigComment("This list stores GameModes in which Likes addon should not work.")
    @ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
    @ConfigComment("disabled-gamemodes:")
    @ConfigComment(" - BSkyBlock")
    @ConfigEntry(path = "disabled-gamemodes")
    private Set<String> disabledGameModes = new HashSet<>();
}
