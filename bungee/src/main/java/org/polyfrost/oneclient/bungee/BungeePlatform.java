package org.polyfrost.oneclient.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.polyfrost.oneclient.common.PlatformLogger;
import org.polyfrost.oneclient.common.ServerPlatform;
import org.polyfrost.oneclient.common.protocol.Protocol;

import java.util.UUID;

final class BungeePlatform implements ServerPlatform {
    private final Plugin plugin;
    private final PlatformLogger logger;

    BungeePlatform(Plugin plugin) {
        this.plugin = plugin;
        this.logger = PlatformLogger.of(plugin.getLogger());
    }

    @Override
    public PlatformLogger getLogger() {
        return logger;
    }

    @Override
    public String getApiVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public void sendPluginMessage(UUID uniqueId, byte[] data) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uniqueId);
        if (player != null) {
            player.sendData(Protocol.CHANNEL, data);
        }
    }
}
