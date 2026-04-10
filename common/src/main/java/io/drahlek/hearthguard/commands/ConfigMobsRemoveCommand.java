package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.util.MobUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Locale;

public class ConfigMobsRemoveCommand {
    public static final String NAME = "remove";

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(NAME);

        for (String mobId : MobUtil.getMonsterMobIds()) {
            root.then(Commands.literal(mobId).executes(ctx -> ConfigMobsRemoveCommand.run(ctx, mobId)));
        }

        return root;
    }

    public static int run(CommandContext<CommandSourceStack> context, String mob) {
        HearthguardConfig.getInstance().getMobs().remove(mob.toLowerCase(Locale.ROOT));
        HearthguardConfig.getInstance().save();
        return Command.SINGLE_SUCCESS;
    }
}
