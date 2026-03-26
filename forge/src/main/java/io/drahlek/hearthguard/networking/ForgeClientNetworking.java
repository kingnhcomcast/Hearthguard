package io.drahlek.hearthguard.networking;

import net.minecraftforge.network.PacketDistributor;

public class ForgeClientNetworking implements IClientNetworking {
    @Override
    public void send(ConfigPayload payload) {
        HearthGuardNetworking.CHANNEL.send(payload, PacketDistributor.SERVER.noArg());
    }
}
