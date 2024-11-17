package world.bentobox.likes;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

/**
 *
 */
public class LikesPladdon extends Pladdon {

    private Addon addon;
    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new LikesAddon();
        }
        return addon;
    }
}
