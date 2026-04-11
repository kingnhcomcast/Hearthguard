package io.drahlek.hearthguard;

import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.HearthGuardNetworking;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public final class NeoForgeEventHandlers {
    private NeoForgeEventHandlers() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            HearthGuardNetworking.sendToClient(player);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        HearthguardConfig.setActiveServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        HearthguardConfig.clearActiveServer(event.getServer());
    }
}
