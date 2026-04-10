package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.drahlek.hearthguard.Constants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

//TODO
//  v1
// -    cmd to add/rem single mob at a time
// -    cmd to "reset field" "reset-all"
// -    enforce min/max when setting
// -    /reload (w/o config) - do both
//  v2
// -    debug nearby
//          list mobs currently affected by fear
//          distance from player
//          which campfire is influencing them
// -    debug test
//          forces nearby mobs into fear state bypasses normal check
// -    debug radius
//          shows particles or markers around campfires
//          visualizes effective radius
// -    info
//          mod version, other info
// -    toggle - toggles enabled, perm/temp?

//reset to defaults, all or just one
public final class HearthGuardCommandTree {
    private HearthGuardCommandTree() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> root() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(Constants.MOD_ID)
                .requires(HearthGuardCommandTree::isOpPlayer);

        LiteralArgumentBuilder<CommandSourceStack> configRoot = Commands.literal("config");
        configRoot.then(Commands.literal(ReloadConfigCommand.NAME).executes(ReloadConfigCommand::run));
        configRoot.then(Commands.literal(ConfigShowCommand.NAME).executes(ConfigShowCommand::run));
        configRoot.then(ConfigGetCommand.register());
        configRoot.then(ConfigSetCommand.register());
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
