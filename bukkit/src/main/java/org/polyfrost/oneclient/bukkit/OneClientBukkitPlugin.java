package org.polyfrost.oneclient.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.polyfrost.oneclient.api.OneClient;
import org.polyfrost.oneclient.api.OneClientPlayer;
import org.polyfrost.oneclient.api.event.OneClientPlayerJoinEvent;
import org.polyfrost.oneclient.api.event.OneClientPlayerQuitEvent;
import org.polyfrost.oneclient.common.OneClientServer;
import org.polyfrost.oneclient.common.protocol.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class OneClientBukkitPlugin extends JavaPlugin implements Listener, PluginMessageListener {
    private OneClientServer server;
    private SessionPermissions permissions;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        server = new OneClientServer(new BukkitPlatform(this));
        permissions = new SessionPermissions(this, PlayerTasks.create(this));
        server.getEventBus().subscribe(OneClientPlayerJoinEvent.class, event -> permissions.grant(event.getPlayer().getUniqueId()));
        server.getEventBus().subscribe(OneClientPlayerQuitEvent.class, event -> permissions.revoke(event.getPlayer().getUniqueId()));
        reload();

        getServer().getMessenger().registerIncomingPluginChannel(this, Protocol.CHANNEL, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, Protocol.CHANNEL);
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("oneclient").setExecutor(new OneClientCommand(this));

        OneClient.register(server);
    }

    @Override
    public void onDisable() {
        if (server == null) {
            return;
        }
        OneClient.unregister();
        permissions.revokeAll();
        server = null;
    }

    void reload() {
        reloadConfig();

        List<UUID> holders = new ArrayList<>();
        for (OneClientPlayer player : server.getPlayers()) {
            holders.add(player.getUniqueId());
        }
        permissions.setNode(getConfig().getString("permission", ""), holders);
    }

    OneClientServer server() {
        return server;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (Protocol.CHANNEL.equals(channel)) {
            server.handlePluginMessage(player.getUniqueId(), player.getName(), message);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        server.handleQuit(event.getPlayer().getUniqueId());
    }
}
