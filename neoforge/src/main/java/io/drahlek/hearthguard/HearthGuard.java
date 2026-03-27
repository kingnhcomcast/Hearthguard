package io.drahlek.hearthguard;


import io.drahlek.hearthguard.commands.Commands;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.HearthGuardNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
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

        HearthguardConfig.load(FMLPaths.CONFIGDIR.get());
        Commands.init();
        eventBus.addListener(HearthGuardNetworking::registerPayloads);
        NeoForge.EVENT_BUS.register(NeoForgeEventHandlers.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            try {
                Class<?> clazz = Class.forName("io.drahlek.hearthguard.client.NeoForgeClientInit");
                clazz.getMethod("init", ModContainer.class).invoke(null, container);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to initialize NeoForge client hooks", e);
            }
        }
    }

}
