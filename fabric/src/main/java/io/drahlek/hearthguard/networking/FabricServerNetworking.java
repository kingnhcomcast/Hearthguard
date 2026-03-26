package io.drahlek.hearthguard.networking;

import io.drahlek.hearthguard.Constants;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class FabricServerNetworking implements IServerNetworking{
    @Override
    public void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        if (player == null) {
            Constants.LOG.warn("Config sync: sendToClient called with null player");
            return;
        }
        Constants.LOG.info("Config sync: sending to {}", player.getName().getString());
        ServerPlayNetworking.send(player, payload);
    }

}
