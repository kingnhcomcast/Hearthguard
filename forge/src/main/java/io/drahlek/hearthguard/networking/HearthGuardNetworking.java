package io.drahlek.hearthguard.networking;

import io.drahlek.hearthguard.Constants;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.payload.PayloadFlow;

public final class HearthGuardNetworking {
    private static final Identifier CHANNEL_ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "main");

    private static final PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> PLAY_FLOW = ChannelBuilder
            .named(CHANNEL_ID)
            .networkProtocolVersion(1)
            .payloadChannel()
            .play()
            .serverbound();

    static {
        PLAY_FLOW.addMain(ConfigPayload.ID, ConfigPayload.CODEC, (payload, context) -> {
            HearthguardConfig.setInstance(payload.config());
            HearthguardConfig.getInstance().save();
            broadcastConfig();
        });
        PLAY_FLOW.clientbound().addMain(ConfigPayload.ID, ConfigPayload.CODEC,
                (payload, context) -> HearthguardConfig.setInstance(payload.config()));
    }

    public static final Channel<CustomPacketPayload> CHANNEL = PLAY_FLOW.build();

    private HearthGuardNetworking() {
    }

    public static void init() {
        // Ensures class loading and channel registration.
    }

    public static void sendToClient(net.minecraft.server.level.ServerPlayer player) {
        if (player == null) {
            return;
        }
        CHANNEL.send(new ConfigPayload(HearthguardConfig.getInstance()),
                PacketDistributor.PLAYER.with(player));
    }

    private static void broadcastConfig() {
        CHANNEL.send(new ConfigPayload(HearthguardConfig.getInstance()), PacketDistributor.ALL.noArg());
    }
}
