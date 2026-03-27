package io.drahlek.hearthguard.networking;

import io.drahlek.hearthguard.Constants;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

public class HearthGuardNetworking {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
        registrar.playBidirectional(
                ConfigPayload.ID,
                ConfigPayload.CODEC,
                new DirectionalPayloadHandler<>(
                        (payload, context) -> context.enqueueWork(() -> HearthguardConfig.setInstance(payload.config())),
                        (payload, context) -> context.enqueueWork(() -> {
                            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                                return;
                            }
                            if (!serverPlayer.hasPermissions(2)) {
                                Constants.LOG.warn("Config sync: denied update from {}", serverPlayer.getName().getString());
                                return;
                            }
                            HearthguardConfig.setInstance(payload.config());
                            HearthguardConfig.getInstance().save();
                            sendToAll();
                        })
                )
        );
    }

    public static void sendToClient(ServerPlayer player) {
        if (player == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new ConfigPayload(HearthguardConfig.getInstance()));
    }

    public static void sendToAll() {
        PacketDistributor.sendToAllPlayers(new ConfigPayload(HearthguardConfig.getInstance()));
    }
}
