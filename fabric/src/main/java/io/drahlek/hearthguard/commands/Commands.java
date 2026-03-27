package io.drahlek.hearthguard.commands;

import io.drahlek.hearthguard.Constants;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static net.minecraft.commands.Commands.literal;

public class Commands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal(Constants.MOD_ID)
                            .then(literal("reload")
                                .requires(source -> source.hasPermission(2))
                                .executes(ReloadConfigCommand::run)
                            )
            );
        });
    }

}
