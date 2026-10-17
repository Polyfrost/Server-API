package org.polyfrost.oneclient.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class FoliaPlayerTasks implements PlayerTasks {
    private final Plugin plugin;

    public FoliaPlayerTasks(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Player player, Runnable task) {
        if (Bukkit.isOwnedByCurrentRegion(player)) {
            task.run();
        } else if (plugin.isEnabled()) {
            player.getScheduler().run(plugin, scheduled -> task.run(), null);
        }
    }
}
