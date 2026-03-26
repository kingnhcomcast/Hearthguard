package io.drahlek.hearthguard;

import io.drahlek.hearthguard.client.ForgeClientInit;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.HearthGuardNetworking;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import static io.drahlek.hearthguard.Constants.MOD_ID;

@Mod(MOD_ID)
public class HearthGuard {

    public HearthGuard() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");

        HearthguardConfig.load(FMLPaths.CONFIGDIR.get());
        HearthGuardNetworking.init();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ForgeClientInit::init);
        MinecraftForge.EVENT_BUS.register(ForgeEventHandlers.class);

    }

}
