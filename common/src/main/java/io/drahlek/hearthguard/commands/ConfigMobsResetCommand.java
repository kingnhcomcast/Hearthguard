package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.ConfigSetting;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.lang.reflect.Field;

public class ConfigMobsResetCommand {
    public static final String NAME = "reset";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(NAME);

        ConfigCommandUtil.addSettingNodes(
                root,
                (field, setting) -> Commands.literal(setting.value()).executes(context -> run(context, field))
        );

        return root;
    }

    private static int run(CommandContext<CommandSourceStack> context, Field field) {
        reset(context, field);
        return Command.SINGLE_SUCCESS;
    }

    private static void reset(CommandContext<CommandSourceStack> context, Field field) {
        try {
            ConfigSetting setting = field.getAnnotation(ConfigSetting.class);
            HearthguardConfig config = HearthguardConfig.getInstance();
            Object defaultValue = ConfigCommandUtil.parseDefaultValueForField(field, setting.defaultValue());
            ConfigCommandUtil.setConfigValue(config, field, defaultValue);
            ConfigCommandUtil.saveAndSync(context, config);
            ConfigCommandUtil.sendFieldValue(context, config, field);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + field.getName(), e);
        }
    }
}
