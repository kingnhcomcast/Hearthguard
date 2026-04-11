package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.commands.CommandSourceStack;


public class ConfigReloadConfigCommand {
    public static final String NAME = "reload";

    public static int run(CommandContext<CommandSourceStack> context) {
        HearthguardConfig.load();
        CommandUtil.sendString(context, "[HearthGuard] Config reloaded.");

        ConfigCommandUtil.syncToPlayers(context.getSource().getServer(), HearthguardConfig.getInstance());

        return Command.SINGLE_SUCCESS;
    }
}
