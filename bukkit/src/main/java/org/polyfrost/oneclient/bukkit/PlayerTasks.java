package org.polyfrost.oneclient.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

interface PlayerTasks {
    void run(Player player, Runnable task);

    static PlayerTasks create(Plugin plugin) {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        } catch (ClassNotFoundException e) {
            return (player, task) -> {
                if (plugin.getServer().isPrimaryThread()) {
                    task.run();
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, task);
                }
            };
        }
        try {
            return (PlayerTasks) Class.forName("org.polyfrost.oneclient.bukkit.FoliaPlayerTasks")
                .getConstructor(Plugin.class)
                .newInstance(plugin);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set up Folia scheduling", e);
        }
    }
}
