package world.bentobox.likes;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

/**
 *
 * @author YellowZaki
 */
@Plugin(name="Pladdon", version="1.0")
@ApiVersion(ApiVersion.Target.v1_16)
@Dependency(value = "BentoBox")
public class LikesPladdon extends Pladdon {

    @Override
    public Addon getAddon() {
        return new LikesAddon();
    }
}
