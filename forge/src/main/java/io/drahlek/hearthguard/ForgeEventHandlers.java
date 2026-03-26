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
}
