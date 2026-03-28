package io.drahlek.hearthguard.commands;

import io.drahlek.hearthguard.Constants;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static net.minecraft.commands.Commands.literal;

public class Commands {
    public static void init() {
        NeoForge.EVENT_BUS.addListener(Commands::register);
    }

    private static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal(Constants.MOD_ID)
                        .then(literal("reload")
                                .requires(source -> {
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        //console if no player, always allow as running on server
                                        return true;
                                    }
                                    return source.getServer()
                                            .getProfilePermissions(player.nameAndId())
                                            .hasPermission(Permissions.COMMANDS_MODERATOR);
                                })
                                .executes(ReloadConfigCommand::run)
                        )
        );
    }
}
