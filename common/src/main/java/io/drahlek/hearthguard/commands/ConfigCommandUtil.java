package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.ConfigSetting;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.ConfigPayload;
import io.drahlek.hearthguard.platform.Services;
import io.drahlek.hearthguard.util.MobUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

final class ConfigCommandUtil {
    private ConfigCommandUtil() {
    }

    static Stream<Field> configSettingFields() {
        return Arrays.stream(HearthguardConfig.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ConfigSetting.class));
    }

    static void addSettingNodes(
            LiteralArgumentBuilder<CommandSourceStack> root,
            BiFunction<Field, ConfigSetting, ArgumentBuilder<CommandSourceStack, ?>> nodeFactory
    ) {
        configSettingFields()
                .forEach(field -> root.then(nodeFactory.apply(field, field.getAnnotation(ConfigSetting.class))));
    }

    static void sendFieldValue(CommandContext<CommandSourceStack> context, Object instance, Field field) {
        try {
            field.setAccessible(true);
            ConfigSetting setting = field.getAnnotation(ConfigSetting.class);
            Object value = field.get(instance);
            CommandUtil.sendString(context, String.format("  %s: %s", setting.value(), value));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + field.getName(), e);
        }
    }

    static void setConfigValue(HearthguardConfig config, Field field, Object value) {
        try {
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

    static String validateRange(Field field, Object value) {
        ConfigSetting setting = field.getAnnotation(ConfigSetting.class);
        if (setting == null || !(value instanceof Number number)) {
            return null;
        }

        double numericValue = number.doubleValue();
        if (numericValue < setting.min() || numericValue > setting.max()) {
            return String.format(
                    "Value for '%s' must be between %s and %s (got %s)",
                    setting.value(),
                    setting.min(),
                    setting.max(),
                    numericValue
            );
        }

        return null;
    }

    static Object parseDefaultValueForField(Field field, String rawDefaultValue) {
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

    static Object parseStringLikeFieldValue(Field field, String raw) {
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

    static void saveAndSync(CommandContext<CommandSourceStack> context, HearthguardConfig config) {
        config.save();
        syncToPlayers(context.getSource().getServer(), config);
    }

    static void syncToPlayers(MinecraftServer server, HearthguardConfig config) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Services.SERVER_NETWORK.sendToClient(player, new ConfigPayload(config));
        }
    }

    static ArgumentBuilder<CommandSourceStack, ?> mobActionRoot(
            String name,
            BiFunction<CommandContext<CommandSourceStack>, String, Integer> command
    ) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(name);

        for (String mobId : MobUtil.getMonsterMobIds()) {
            root.then(Commands.literal(mobId).executes(context -> command.apply(context, mobId)));
        }

        return root;
    }

    static int updateMob(String mob, BiConsumer<Set<String>, String> updater) {
        HearthguardConfig config = HearthguardConfig.getInstance();
        updater.accept(config.getMobs(), mob.toLowerCase(Locale.ROOT));
        config.save();
        return Command.SINGLE_SUCCESS;
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
}
