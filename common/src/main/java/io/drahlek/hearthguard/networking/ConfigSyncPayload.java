package io.drahlek.hearthguard.networking;

import com.google.gson.Gson;
import io.drahlek.hearthguard.Constants;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(HearthguardConfig config) implements CustomPacketPayload {
    public static Gson GSON = new Gson();

    public static final Identifier SYNC_CONFIG_ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sync_config");
    public static final CustomPacketPayload.Type<ConfigSyncPayload> ID = new CustomPacketPayload.Type<>(SYNC_CONFIG_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    payload -> GSON.toJson(payload.config()),
                    json -> new ConfigSyncPayload(GSON.fromJson(json, HearthguardConfig.class))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
