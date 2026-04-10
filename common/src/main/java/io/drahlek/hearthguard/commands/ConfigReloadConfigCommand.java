package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.ConfigPayload;
import io.drahlek.hearthguard.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;


public class ConfigReloadConfigCommand {
    public static final String NAME = "reload";

    public static int run(CommandContext<CommandSourceStack> context) {
        HearthguardConfig.load();
        CommandUtil.sendString(context,"[HearthGuard] Config reloaded.");

        // Sync to all players
        MinecraftServer server = context.getSource().getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Services.SERVER_NETWORK.sendToClient(player, new ConfigPayload(HearthguardConfig.getInstance()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
