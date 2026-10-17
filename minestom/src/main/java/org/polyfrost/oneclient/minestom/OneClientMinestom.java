package org.polyfrost.oneclient.minestom;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.polyfrost.oneclient.api.OneClient;
import org.polyfrost.oneclient.api.OneClientApi;
import org.polyfrost.oneclient.common.OneClientServer;
import org.polyfrost.oneclient.common.PlatformLogger;
import org.polyfrost.oneclient.common.ServerPlatform;
import org.polyfrost.oneclient.common.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class OneClientMinestom {
    private OneClientMinestom() {
    }

    public static OneClientApi init() {
        OneClientServer server = new OneClientServer(new MinestomPlatform());
        EventNode<PlayerEvent> node = EventNode.type("oneclient", EventFilter.PLAYER)
            .addListener(PlayerPluginMessageEvent.class, event -> {
                if (Protocol.CHANNEL.equals(event.getIdentifier())) {
                    Player player = event.getPlayer();
                    server.handlePluginMessage(player.getUuid(), player.getUsername(), event.getMessage());
                }
            })
            .addListener(PlayerSpawnEvent.class, event -> {
                if (event.isFirstSpawn()) {
                    event.getPlayer().sendPluginMessage("minecraft:register", Protocol.CHANNEL.getBytes(StandardCharsets.UTF_8));
                }
            })
            .addListener(PlayerDisconnectEvent.class, event -> server.handleQuit(event.getPlayer().getUuid()));
        MinecraftServer.getGlobalEventHandler().addChild(node);

        OneClient.register(server);
        return server;
    }

    private static final class MinestomPlatform implements ServerPlatform {
        private static final Logger LOGGER = LoggerFactory.getLogger("OneClient");
        private static final PlatformLogger PLATFORM_LOGGER = new PlatformLogger() {
            @Override
            public void debug(String message) {
                LOGGER.debug(message);
            }

            @Override
            public void error(String message, Throwable thrown) {
                LOGGER.error(message, thrown);
            }
        };

        @Override
        public PlatformLogger getLogger() {
            return PLATFORM_LOGGER;
        }

        @Override
        public String getApiVersion() {
            String version = OneClientMinestom.class.getPackage().getImplementationVersion();
            return version == null ? "unknown" : version;
        }

        @Override
        public void sendPluginMessage(UUID uniqueId, byte[] data) {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uniqueId);
            if (player == null) {
                player = MinecraftServer.getConnectionManager().getConfigPlayers().stream()
                    .filter(candidate -> candidate.getUuid().equals(uniqueId))
                    .findFirst()
                    .orElse(null);
            }
            if (player != null) {
                player.sendPluginMessage(Protocol.CHANNEL, data);
            }
        }
    }
}
