package io.drahlek.hearthguard;

import io.drahlek.hearthguard.networking.HearthGuardNetworking;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class NeoForgeEventHandlers {
    private NeoForgeEventHandlers() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            HearthGuardNetworking.sendToClient(player);
        }
    }
}
