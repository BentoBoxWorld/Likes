package world.bentobox.likes.placeholders;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.LikesAddon;


public interface LikesAddonPlaceholderReplacer
{

    /**
     * @param addon the LikesAddon that registered the placeholder, cannot be null.
     * @param gameModeAddon the GameModeAddon that registered the placeholder, cannot be null.
     * @param island the Island of the User, can be null.
     * @return the String containing the requested value or an empty String.
     */
    @NonNull
    String onReplace(@NonNull LikesAddon addon, @NonNull GameModeAddon gameModeAddon, @Nullable Island island);
}
