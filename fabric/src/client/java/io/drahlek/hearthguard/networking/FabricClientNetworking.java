package io.drahlek.hearthguard.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClientNetworking implements IClientNetworking {
    @Override
    public void sendToServer(ConfigPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
