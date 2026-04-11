package io.drahlek.hearthguard.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class FabricCommands {
    private FabricCommands() {
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(HearthGuardCommandTree.root()));
    }
}
