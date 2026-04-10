package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.ConfigSetting;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.commands.CommandSourceStack;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ConfigShowCommand {
    public static final String NAME = "show";

    public static int run(CommandContext<CommandSourceStack> context) {
        CommandUtil.sendString(context, "HearthGuard Config:");
        printAnnotatedFields(context, HearthguardConfig.getInstance());
        return Command.SINGLE_SUCCESS;
    }

    private static void printAnnotatedFields(CommandContext<CommandSourceStack> context, Object instance) {
        Class<?> clazz = instance.getClass();

        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ConfigSetting.class))
                .forEach(field -> readAndPrint(context, instance, field));
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
