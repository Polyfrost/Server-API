package org.polyfrost.oneclient.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class SessionPermissions {
    private final Plugin plugin;
    private final PlayerTasks tasks;
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();
    private volatile String node = "";

    SessionPermissions(Plugin plugin, PlayerTasks tasks) {
        this.plugin = plugin;
        this.tasks = tasks;
    }

    void setNode(String node, Iterable<UUID> holders) {
        revokeAll();
        this.node = node == null ? "" : node.trim();
        if (!this.node.isEmpty() && plugin.getServer().getPluginManager().getPermission(this.node) == null) {
            plugin.getServer().getPluginManager().addPermission(new Permission(this.node, PermissionDefault.FALSE));
        }
        for (UUID uniqueId : holders) {
            grant(uniqueId);
        }
    }

    void grant(UUID uniqueId) {
        Player player = plugin.getServer().getPlayer(uniqueId);
        String node = this.node;
        if (player == null || node.isEmpty()) {
            return;
        }
        tasks.run(player, () -> {
            if (player.isOnline() && node.equals(this.node) && !attachments.containsKey(uniqueId)) {
                attachments.put(uniqueId, player.addAttachment(plugin, node, true));
            }
        });
    }

    void revoke(UUID uniqueId) {
        PermissionAttachment attachment = attachments.remove(uniqueId);
        Player player = plugin.getServer().getPlayer(uniqueId);
        if (attachment != null && player != null) {
            tasks.run(player, attachment::remove);
        }
    }

    void revokeAll() {
        for (UUID uniqueId : new ArrayList<>(attachments.keySet())) {
            revoke(uniqueId);
        }
    }
}
