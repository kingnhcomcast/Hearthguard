package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.ConfigSetting;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.ConfigPayload;
import io.drahlek.hearthguard.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConfigMobsResetCommand {
    public static final String NAME = "reset";

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
        reset(context, field);
        return Command.SINGLE_SUCCESS;
    }

    private static void reset(CommandContext<CommandSourceStack> context, Field field) {
        try {
            field.setAccessible(true);
            ConfigSetting setting = field.getAnnotation(ConfigSetting.class);
            Object defaultValue = parseDefaultValueForField(field, setting.defaultValue());
            setConfigValue(field, defaultValue);
            HearthguardConfig.getInstance().save();
            syncToPlayers(context.getSource().getServer(), HearthguardConfig.getInstance());
            CommandUtil.sendString(context, String.format("  %s: %s", setting.value(), defaultValue));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + field.getName(), e);
        }
    }

    private static void setConfigValue(Field field, Object value) {
        try {
            HearthguardConfig config = HearthguardConfig.getInstance();
            Method setter = findSetter(field);
            if (setter != null) {
                setter.invoke(config, value);
                return;
            }

            field.setAccessible(true);
            field.set(config, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set " + field.getName(), e);
        }
    }

    private static void syncToPlayers(MinecraftServer server, HearthguardConfig config) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Services.SERVER_NETWORK.sendToClient(player, new ConfigPayload(config));
        }
    }

    private static Method findSetter(Field field) {
        String fieldName = field.getName();
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        Class<?> type = field.getType();
        try {
            return HearthguardConfig.class.getMethod(setterName, type);
        } catch (NoSuchMethodException ignored) {
        }

        if (type.isPrimitive()) {
            Class<?> boxed = box(type);
            if (boxed != null) {
                try {
                    return HearthguardConfig.class.getMethod(setterName, boxed);
                } catch (NoSuchMethodException ignored) {
                }
            }
        } else {
            Class<?> unboxed = unbox(type);
            if (unboxed != null) {
                try {
                    return HearthguardConfig.class.getMethod(setterName, unboxed);
                } catch (NoSuchMethodException ignored) {
                }
            }
        }

        return null;
    }

    private static Class<?> unbox(Class<?> boxed) {
        if (boxed == Integer.class) return int.class;
        if (boxed == Double.class) return double.class;
        if (boxed == Boolean.class) return boolean.class;
        if (boxed == Long.class) return long.class;
        if (boxed == Float.class) return float.class;
        if (boxed == Short.class) return short.class;
        if (boxed == Byte.class) return byte.class;
        if (boxed == Character.class) return char.class;
        return null;
    }

    private static Class<?> box(Class<?> primitive) {
        if (primitive == int.class) return Integer.class;
        if (primitive == double.class) return Double.class;
        if (primitive == boolean.class) return Boolean.class;
        if (primitive == long.class) return Long.class;
        if (primitive == float.class) return Float.class;
        if (primitive == short.class) return Short.class;
        if (primitive == byte.class) return Byte.class;
        if (primitive == char.class) return Character.class;
        return null;
    }

    private static Object parseDefaultValueForField(Field field, String rawDefaultValue) {
        if (rawDefaultValue == null || rawDefaultValue.isBlank()) {
            try {
                field.setAccessible(true);
                Object classDefault = field.get(new HearthguardConfig());
                if (classDefault instanceof Set<?> setDefault) {
                    return new HashSet<>(setDefault);
                }
                return classDefault;
            } catch (Exception e) {
                throw new RuntimeException("Failed to resolve default for " + field.getName(), e);
            }
        }

        Class<?> type = field.getType();

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(rawDefaultValue);
        }

        if (type == double.class || type == Double.class) {
            return Double.parseDouble(rawDefaultValue);
        }

        return parseStringLikeFieldValue(field, rawDefaultValue);
    }

    private static Object parseStringLikeFieldValue(Field field, String raw) {
        if (Set.class.isAssignableFrom(field.getType())) {
            String normalized = raw.trim();
            if (normalized.startsWith("[") && normalized.endsWith("]") && normalized.length() >= 2) {
                normalized = normalized.substring(1, normalized.length() - 1);
            }

            if (normalized.isEmpty()) {
                return new HashSet<String>();
            }

            Set<String> values = new HashSet<>();
            for (String part : normalized.split(",")) {
                String value = part.trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
            return values;
        }

        return raw;
    }
}
