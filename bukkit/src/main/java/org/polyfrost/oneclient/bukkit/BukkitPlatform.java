package org.polyfrost.oneclient.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.polyfrost.oneclient.common.PlatformLogger;
import org.polyfrost.oneclient.common.ServerPlatform;
import org.polyfrost.oneclient.common.protocol.Protocol;

import java.util.UUID;

final class BukkitPlatform implements ServerPlatform {
    private final JavaPlugin plugin;

    BukkitPlatform(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PlatformLogger getLogger() {
        return PlatformLogger.of(plugin.getLogger());
    }

    @Override
    public String getApiVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public void sendPluginMessage(UUID uniqueId, byte[] data) {
        Player player = plugin.getServer().getPlayer(uniqueId);
        if (player != null) {
            player.sendPluginMessage(plugin, Protocol.CHANNEL, data);
        }
    }
}
