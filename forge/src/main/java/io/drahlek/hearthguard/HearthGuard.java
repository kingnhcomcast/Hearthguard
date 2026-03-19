package io.drahlek.hearthguard;

import io.drahlek.hearthguard.client.ForgeClientInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class HearthGuard {

    public HearthGuard() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ForgeClientInit::init);

    }
}
