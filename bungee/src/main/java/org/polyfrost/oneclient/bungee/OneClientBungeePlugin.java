package org.polyfrost.oneclient.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.polyfrost.oneclient.api.OneClient;
import org.polyfrost.oneclient.common.OneClientServer;
import org.polyfrost.oneclient.common.protocol.Protocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public final class OneClientBungeePlugin extends Plugin implements Listener {
    private OneClientServer server;
    private volatile String permission = "";

    @Override
    public void onEnable() {
        server = new OneClientServer(new BungeePlatform(this));
        reload();

        getProxy().registerChannel(Protocol.CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new OneClientCommand(this));

        OneClient.register(server);
    }

    @Override
    public void onDisable() {
        if (server == null) {
            return;
        }
        OneClient.unregister();
        getProxy().unregisterChannel(Protocol.CHANNEL);
        server = null;
    }

    void reload() {
        File file = new File(getDataFolder(), "config.yml");
        try {
            if (!file.exists()) {
                Files.createDirectories(getDataFolder().toPath());
                try (InputStream in = getResourceAsStream("config.yml")) {
                    Files.copy(in, file.toPath());
                }
            }
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            permission = config.getString("permission", "").trim();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + file, e);
        }
    }

    OneClientServer server() {
        return server;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!Protocol.CHANNEL.equals(event.getTag())) {
            return;
        }
        event.setCancelled(true);
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            server.handlePluginMessage(player.getUniqueId(), player.getName(), event.getData());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPermissionCheck(PermissionCheckEvent event) {
        if (event.getSender() instanceof ProxiedPlayer
            && event.getPermission().equals(permission)
            && server.isOneClient(((ProxiedPlayer) event.getSender()).getUniqueId())) {
            event.setHasPermission(true);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        server.handleQuit(event.getPlayer().getUniqueId());
    }
}
