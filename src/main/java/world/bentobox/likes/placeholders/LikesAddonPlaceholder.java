///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.placeholders;


import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.placeholders.PlaceholderReplacer;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;


/**
 * This class loads all Likes Addon Placeholders.
 */
public class LikesAddonPlaceholder implements PlaceholderReplacer
{
    /**
     * Default constructor for likes addon placeholder.
     *
     * @param gameModeAddon Target GameMode addon.
     * @param type Likes Addon Placeholder Type.
     */
    public LikesAddonPlaceholder(LikesAddon addon, GameModeAddon gameModeAddon, LikesAddonPlaceholderType type)
    {
        this.addon = addon;
        this.gameModeAddon = gameModeAddon;
        this.type = type;
    }


    /**
     * (non-Javadoc)
     *
     * @see world.bentobox.bentobox.api.placeholders.PlaceholderReplacer#onReplace(world.bentobox.bentobox.api.user.User)
     */
    @NonNull
    @Override
    public String onReplace(@Nullable User user)
    {
        if (user == null)
        {
            return "";
        }

        return this.type.getReplacer().onReplace(this.addon, this.gameModeAddon, user);
    }


    /**
     * Target GameMode addon.
     */
    private final GameModeAddon gameModeAddon;

    /**
     * Likes addon instance
     */
    private final LikesAddon addon;

    /**
     * Current placeholder type
     */
    private final LikesAddonPlaceholderType type;
}
