package io.drahlek.hearthguard.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface IServerNetworking {
    void sendToClient(ServerPlayer player, CustomPacketPayload payload);
}
