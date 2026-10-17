package org.polyfrost.oneclient.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class SessionPermissions {
    private final Plugin plugin;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();
    private String node = "";

    SessionPermissions(Plugin plugin) {
        this.plugin = plugin;
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
        if (player == null || node.isEmpty() || attachments.containsKey(uniqueId)) {
            return;
        }
        attachments.put(uniqueId, player.addAttachment(plugin, node, true));
    }

    void revoke(UUID uniqueId) {
        PermissionAttachment attachment = attachments.remove(uniqueId);
        if (attachment != null) {
            attachment.remove();
        }
    }

    void revokeAll() {
        for (UUID uniqueId : new HashMap<>(attachments).keySet()) {
            revoke(uniqueId);
        }
    }
}
