package io.drahlek.hearthguard;

import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.ConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class HearthguardClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.ID, (payload, context) -> {
            context.client().execute(() -> HearthguardConfig.setInstance(payload.config()));
        });
    }
}
