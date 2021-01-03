///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.likes.placeholders;


import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;


/**
 * The interface Likes addon placeholder replacer.
 */
public interface LikesAddonPlaceholderReplacer
{

    /**
     * @param addon the LikesAddon that registered the placeholder, cannot be null.
     * @param gameModeAddon the GameModeAddon that registered the placeholder, cannot be null.
     * @param user the User, cannot be null.
     * @return the String containing the requested value or an empty String.
     */
    @NonNull
    String onReplace(@NonNull LikesAddon addon, @NonNull GameModeAddon gameModeAddon, @NonNull User user);
}
