package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.Set;

public class ConfigMobsAddCommand {
    public static final String NAME = "add";

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return ConfigCommandUtil.mobActionRoot(NAME, ConfigMobsAddCommand::run);
    }

    public static int run(CommandContext<CommandSourceStack> context, String mob) {
        return ConfigCommandUtil.updateMob(mob, Set::add);
    }
}
