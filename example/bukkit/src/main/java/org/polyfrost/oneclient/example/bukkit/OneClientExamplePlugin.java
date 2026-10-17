package org.polyfrost.oneclient.example.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.polyfrost.oneclient.api.OneClient;
import org.polyfrost.oneclient.api.event.EventBus;
import org.polyfrost.oneclient.api.event.OneClientPlayerJoinEvent;

public final class OneClientExamplePlugin extends JavaPlugin {
    private EventBus.Subscription subscription;

    @Override
    public void onEnable() {
        subscription = OneClient.getApi().getEventBus().subscribe(OneClientPlayerJoinEvent.class, event -> {
            Player player = getServer().getPlayer(event.getPlayer().getUniqueId());
            if (player != null) {
                player.sendMessage("You are on OneClient");
            }
        });
    }

    @Override
    public void onDisable() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
