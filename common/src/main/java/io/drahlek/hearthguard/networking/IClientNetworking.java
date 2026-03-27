package io.drahlek.hearthguard.networking;

//TODO this is config specific, make generic
public interface IClientNetworking {
    void sendToServer(ConfigPayload payload);
}
