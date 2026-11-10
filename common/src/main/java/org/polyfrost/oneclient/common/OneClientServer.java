package org.polyfrost.oneclient.common;

import org.polyfrost.oneclient.api.OneClientApi;
import org.polyfrost.oneclient.api.OneClientPlayer;
import org.polyfrost.oneclient.api.event.EventBus;
import org.polyfrost.oneclient.api.event.OneClientPlayerJoinEvent;
import org.polyfrost.oneclient.api.event.OneClientPlayerQuitEvent;
import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.Protocol;
import org.polyfrost.oneclient.common.protocol.ProtocolException;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundWelcome;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundCapabilities;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundHello;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OneClientServer implements OneClientApi {
    private static final int FIRST_CAPABILITIES_VERSION = 2;

    private final ServerPlatform platform;
    private final SimpleEventBus eventBus;
    private final Map<UUID, ServerboundHello> pending = new ConcurrentHashMap<>();
    private final Map<UUID, ConnectedPlayer> players = new ConcurrentHashMap<>();

    public OneClientServer(ServerPlatform platform) {
        this.platform = platform;
        this.eventBus = new SimpleEventBus(platform.getLogger());
    }

    public void handlePluginMessage(UUID uniqueId, String name, byte[] data) {
        Packet packet;
        try {
            packet = Protocol.SERVERBOUND.decode(data);
        } catch (ProtocolException e) {
            platform.getLogger().debug("Ignoring malformed OneClient packet from " + name + ": " + e.getMessage());
            return;
        }

        if (packet instanceof ServerboundHello) {
            handleHello(uniqueId, name, (ServerboundHello) packet);
        } else if (packet instanceof ServerboundCapabilities) {
            ServerboundHello hello = pending.remove(uniqueId);
            if (hello != null) {
                join(uniqueId, name, hello, ((ServerboundCapabilities) packet).getKeys());
            }
        }
    }

    public void handleQuit(UUID uniqueId) {
        pending.remove(uniqueId);
        ConnectedPlayer player = players.remove(uniqueId);
        if (player != null) {
            eventBus.post(new OneClientPlayerQuitEvent(player));
        }
    }

    public void sendPacket(UUID uniqueId, Packet packet) {
        platform.sendPluginMessage(uniqueId, Protocol.CLIENTBOUND.encode(packet));
    }

    void send(ConnectedPlayer player, Packet packet) {
        if (players.get(player.getUniqueId()) == player) {
            sendPacket(player.getUniqueId(), packet);
        }
    }

    PlatformLogger logger() {
        return platform.getLogger();
    }

    private void handleHello(UUID uniqueId, String name, ServerboundHello hello) {
        handleQuit(uniqueId);
        if (hello.getProtocolVersion() >= FIRST_CAPABILITIES_VERSION) {
            pending.put(uniqueId, hello);
        }
        sendPacket(uniqueId, new ClientboundWelcome(Protocol.VERSION, platform.getApiVersion()));
        if (hello.getProtocolVersion() < FIRST_CAPABILITIES_VERSION) {
            join(uniqueId, name, hello, Collections.<String>emptySet());
        }
    }

    private void join(UUID uniqueId, String name, ServerboundHello hello, Set<String> capabilities) {
        ConnectedPlayer player = new ConnectedPlayer(this, uniqueId, name, hello.getProtocolVersion(),
            hello.getClientVersion(), hello.getMinecraftVersion(), capabilities);
        players.put(uniqueId, player);
        eventBus.post(new OneClientPlayerJoinEvent(player));
    }

    @Override
    public Optional<OneClientPlayer> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(players.get(uniqueId));
    }

    @Override
    public boolean isOneClient(UUID uniqueId) {
        return players.containsKey(uniqueId);
    }

    @Override
    public Collection<OneClientPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
}
