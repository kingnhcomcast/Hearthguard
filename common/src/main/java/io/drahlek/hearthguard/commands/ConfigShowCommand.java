package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.commands.CommandSourceStack;

public class ConfigShowCommand {
    public static final String NAME = "show";

    public static int run(CommandContext<CommandSourceStack> context) {
        CommandUtil.sendString(context, "HearthGuard Config:");
        printAnnotatedFields(context, HearthguardConfig.getInstance());
        return Command.SINGLE_SUCCESS;
    }

    private static void printAnnotatedFields(CommandContext<CommandSourceStack> context, Object instance) {
        ConfigCommandUtil.configSettingFields()
                .forEach(field -> ConfigCommandUtil.sendFieldValue(context, instance, field));
    }
}
