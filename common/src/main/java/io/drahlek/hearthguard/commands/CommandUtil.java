package io.drahlek.hearthguard.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.network.chat.Component.literal;

public class CommandUtil {
    public static void sendString(CommandContext<CommandSourceStack> context, String string) {
        context.getSource().sendSuccess(
                () -> Component.literal(string),
                false
        );
    }

}
