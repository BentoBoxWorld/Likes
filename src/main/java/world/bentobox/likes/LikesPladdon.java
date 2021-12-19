package world.bentobox.likes;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

/**
 *
 * @author YellowZaki
 */
public class LikesPladdon extends Pladdon {

    @Override
    public Addon getAddon() {
        return new LikesAddon();
    }
}
