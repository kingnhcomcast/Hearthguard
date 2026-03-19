package io.drahlek.hearthguard;


import io.drahlek.hearthguard.client.NeoForgeClientInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModContainer;

@Mod(Constants.MOD_ID)
public class HearthGuard {

    public HearthGuard(IEventBus eventBus, ModContainer container) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForgeClientInit.init(container);
        }

    }
}
