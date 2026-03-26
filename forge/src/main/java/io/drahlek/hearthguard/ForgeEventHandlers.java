package io.drahlek.hearthguard;

import io.drahlek.hearthguard.networking.HearthGuardNetworking;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public final class ForgeEventHandlers {
    private ForgeEventHandlers() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            HearthGuardNetworking.sendToClient(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // No-op. This keeps the class multi-listener so Forge's migration helper accepts it.
    }
}
