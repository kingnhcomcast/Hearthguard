package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.drahlek.hearthguard.Constants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

//reset to defaults, all or just one
public final class HearthGuardCommandTree {
    private HearthGuardCommandTree() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> root() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(Constants.MOD_ID)
                .requires(HearthGuardCommandTree::isOpPlayer);

        LiteralArgumentBuilder<CommandSourceStack> configRoot = Commands.literal("config");
        configRoot.then(Commands.literal(ConfigReloadConfigCommand.NAME).executes(ConfigReloadConfigCommand::run));
        configRoot.then(Commands.literal(ConfigShowCommand.NAME).executes(ConfigShowCommand::run));
        configRoot.then(ConfigGetCommand.register());
        configRoot.then(ConfigSetCommand.register());
        configRoot.then(ConfigMobsResetCommand.register());
        LiteralArgumentBuilder<CommandSourceStack> mobsRoot = Commands.literal("mobs");
        mobsRoot.then(ConfigMobsAddCommand.register());
        mobsRoot.then(ConfigMobsRemoveCommand.register());

        configRoot.then(mobsRoot);

        root.then(configRoot);

        return root;
    }

    private static boolean isOpPlayer(CommandSourceStack source) {
        if (source.getPlayer() == null) {
            return false;
        }

        return source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
    }
}
