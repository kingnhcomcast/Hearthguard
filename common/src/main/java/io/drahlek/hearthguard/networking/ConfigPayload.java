package io.drahlek.hearthguard.networking;

import com.google.gson.Gson;
import io.drahlek.hearthguard.Constants;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigPayload(HearthguardConfig config) implements CustomPacketPayload {
    public static Gson GSON = new Gson();

    public static final ResourceLocation UPDATE_CONFIG_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "update_config");
    public static final CustomPacketPayload.Type<ConfigPayload> ID = new CustomPacketPayload.Type<>(UPDATE_CONFIG_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    payload -> GSON.toJson(payload.config()),
                    json -> new ConfigPayload(GSON.fromJson(json, HearthguardConfig.class))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
