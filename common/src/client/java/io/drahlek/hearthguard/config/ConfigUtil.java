package io.drahlek.hearthguard.config;


import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.Permissions;

public class ConfigUtil {
    public static boolean canEditConfig() {
        Minecraft minecraft = Minecraft.getInstance();

        // Singleplayer always editable (some flags only flip after world fully loads)
        if (minecraft.isSingleplayer()
                || minecraft.hasSingleplayerServer()
                || minecraft.getSingleplayerServer() != null
                || minecraft.isLocalServer()) {
            return true;
        }

        // Multiplayer: require operator-level permissions (level 2+)
        if (minecraft.player == null) {
            return false;
        }

        return minecraft.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }
}

