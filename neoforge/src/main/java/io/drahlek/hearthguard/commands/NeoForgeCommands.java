package io.drahlek.hearthguard.commands;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class NeoForgeCommands {
    private NeoForgeCommands() {
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(NeoForgeCommands::register);
    }

    private static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(HearthGuardCommandTree.root());
    }
}
