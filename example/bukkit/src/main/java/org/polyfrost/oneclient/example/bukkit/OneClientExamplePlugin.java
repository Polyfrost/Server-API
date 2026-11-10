package org.polyfrost.oneclient.example.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.polyfrost.oneclient.api.OneClient;
import org.polyfrost.oneclient.api.OneClientPlayer;
import org.polyfrost.oneclient.api.event.EventBus;
import org.polyfrost.oneclient.api.event.OneClientPlayerJoinEvent;

import java.time.Duration;

public final class OneClientExamplePlugin extends JavaPlugin {
    private EventBus.Subscription subscription;

    @Override
    public void onEnable() {
        subscription = OneClient.getApi().getEventBus().subscribe(OneClientPlayerJoinEvent.class, event -> {
            OneClientPlayer oneClient = event.getPlayer();
            Player player = getServer().getPlayer(oneClient.getUniqueId());
            if (player != null) {
                player.sendMessage("You are on OneClient");
            }

            oneClient.setOption("fov.default-fov", 90);
            oneClient.sendNotification(
                "{\"text\":\"Welcome\"}",
                "{\"text\":\"This server set your FOV to 90.\"}",
                Duration.ofSeconds(5),
                null
            );
        });
    }

    @Override
    public void onDisable() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
