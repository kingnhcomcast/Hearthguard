package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.Set;

public class ConfigMobsRemoveCommand {
    public static final String NAME = "remove";

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return ConfigCommandUtil.mobActionRoot(NAME, ConfigMobsRemoveCommand::run);
    }

    public static int run(CommandContext<CommandSourceStack> context, String mob) {
        return ConfigCommandUtil.updateMob(mob, Set::remove);
    }
}
