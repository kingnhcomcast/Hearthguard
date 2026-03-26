package io.drahlek.hearthguard.networking;

import io.drahlek.hearthguard.Constants;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class HearthGuardNetworking {
    public static void init() {
        PayloadTypeRegistry.playC2S().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ConfigPayload.ID, ConfigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                HearthguardConfig.setInstance(payload.config());
                HearthguardConfig.getInstance().save();
                for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                    sendToClient(player);
                }
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Constants.LOG.info("Config sync: JOIN for {}", handler.player.getName().getString());
            sendToClient(handler.player);
        });
    }

    public static void sendToClient(ServerPlayer player) {
        if (player == null) {
            Constants.LOG.warn("Config sync: sendToClient called with null player");
            return;
        }
        Constants.LOG.info("Config sync: sending to {}", player.getName().getString());
        ServerPlayNetworking.send(player, new ConfigPayload(HearthguardConfig.getInstance()));
    }
}
