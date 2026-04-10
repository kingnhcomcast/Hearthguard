package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class ConfigSetCommand {
    public static final String NAME = "set";
    private static final String VALUE_ARG = "value";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(NAME);

        ConfigCommandUtil.addSettingNodes(
                root,
                (field, setting) -> Commands.literal(setting.value()).then(valueArgumentFor(field))
        );

        return root;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> valueArgumentFor(Field field) {
        Class<?> type = field.getType();

        if (type == int.class || type == Integer.class) {
            return Commands.argument(VALUE_ARG, IntegerArgumentType.integer())
                    .executes(context -> runSet(context, field, IntegerArgumentType.getInteger(context, VALUE_ARG)));
        }

        if (type == double.class || type == Double.class) {
            return Commands.argument(VALUE_ARG, DoubleArgumentType.doubleArg())
                    .executes(context -> runSet(context, field, DoubleArgumentType.getDouble(context, VALUE_ARG)));
        }

        return Commands.argument(VALUE_ARG, StringArgumentType.greedyString())
                .executes(context -> runSet(
                        context,
                        field,
                        ConfigCommandUtil.parseStringLikeFieldValue(field, StringArgumentType.getString(context, VALUE_ARG))
                ));
    }

    private static int runSet(CommandContext<CommandSourceStack> context, Field field, Object parsedValue) {
        String validationError = ConfigCommandUtil.validateRange(field, parsedValue);
        if (validationError != null) {
            context.getSource().sendFailure(Component.literal(validationError));
            return 0;
        }

        HearthguardConfig config = HearthguardConfig.getInstance();

        ConfigCommandUtil.setConfigValue(config, field, parsedValue);
        ConfigCommandUtil.saveAndSync(context, config);

        ConfigCommandUtil.sendFieldValue(context, config, field);
        return Command.SINGLE_SUCCESS;
    }
}
