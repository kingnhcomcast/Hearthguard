package io.drahlek.hearthguard.networking;

import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeClientNetworking implements IClientNetworking {
    @Override
    public void sendToServer(ConfigPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
