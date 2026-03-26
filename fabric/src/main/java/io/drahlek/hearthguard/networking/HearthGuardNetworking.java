package io.drahlek.hearthguard.networking;

import io.drahlek.hearthguard.Constants;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

//TODO not all of this should be here. keep just networking
public class HearthGuardNetworking {
    private static final IServerNetworking serverNetwork = new FabricServerNetworking();

    public static void init() {
        //register payloads
        PayloadTypeRegistry.playC2S().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ConfigPayload.ID, ConfigPayload.CODEC);

        //handler for receiving a ConfigPayload
        ServerPlayNetworking.registerGlobalReceiver(ConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                HearthguardConfig.setInstance(payload.config());
                HearthguardConfig.getInstance().save();
                //send config to all other connected clients
                for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                    serverNetwork.sendToClient(player, new ConfigPayload(HearthguardConfig.getInstance()));
                }
            });
        });

        //send config to player when he joins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Constants.LOG.info("Config sync: JOIN for {}", handler.player.getName().getString());
            serverNetwork.sendToClient(handler.player, new ConfigPayload(HearthguardConfig.getInstance()));
        });
    }
}
