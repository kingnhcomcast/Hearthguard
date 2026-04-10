package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.ConfigSetting;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ConfigGetCommand {
    public static final String NAME = "get";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(NAME);

        Arrays.stream(HearthguardConfig.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ConfigSetting.class))
                .forEach(field -> {
                    ConfigSetting setting = field.getAnnotation(ConfigSetting.class);
                    root.then(Commands.literal(setting.value()).executes(context -> run(context, field)));
                });

        return root;
    }

    private static int run(CommandContext<CommandSourceStack> context, Field field) {
        HearthguardConfig instance = HearthguardConfig.getInstance();
        readAndPrint(context, instance, field);
        return Command.SINGLE_SUCCESS;
    }

    private static void readAndPrint(CommandContext<CommandSourceStack> context, Object instance, Field field) {
        try {
            field.setAccessible(true);
            ConfigSetting setting = field.getAnnotation(ConfigSetting.class);
            Object value = field.get(instance);
            CommandUtil.sendString(context, String.format("  %s: %s", setting.value(), value));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + field.getName(), e);
        }
    }
}
