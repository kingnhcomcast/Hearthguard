package io.drahlek.hearthguard;


import io.drahlek.hearthguard.config.HearthguardConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;

import static io.drahlek.hearthguard.Constants.MOD_ID;

@Mod(MOD_ID)
public class HearthGuard {

    public HearthGuard(IEventBus eventBus, ModContainer container) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        HearthguardConfig.init(FMLPaths.CONFIGDIR.get().resolve(MOD_ID));
    }
}
