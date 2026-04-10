package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.lang.reflect.Field;

public class ConfigGetCommand {
    public static final String NAME = "get";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(NAME);

        ConfigCommandUtil.addSettingNodes(
                root,
                (field, setting) -> Commands.literal(setting.value()).executes(context -> run(context, field))
        );

        return root;
    }

    private static int run(CommandContext<CommandSourceStack> context, Field field) {
        HearthguardConfig instance = HearthguardConfig.getInstance();
        ConfigCommandUtil.sendFieldValue(context, instance, field);
        return Command.SINGLE_SUCCESS;
    }
}
