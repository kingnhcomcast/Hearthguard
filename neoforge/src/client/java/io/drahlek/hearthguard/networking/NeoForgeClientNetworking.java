package io.drahlek.hearthguard.networking;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoForgeClientNetworking implements IClientNetworking {
    @Override
    public void sendToServer(ConfigPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
