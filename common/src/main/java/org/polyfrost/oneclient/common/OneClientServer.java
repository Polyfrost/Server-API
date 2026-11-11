package org.polyfrost.oneclient.common;

import org.polyfrost.oneclient.api.OneClientApi;
import org.polyfrost.oneclient.api.OneClientPlayer;
import org.polyfrost.oneclient.api.event.EventBus;
import org.polyfrost.oneclient.api.event.OneClientPlayerJoinEvent;
import org.polyfrost.oneclient.api.event.OneClientPlayerQuitEvent;
import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.Protocol;
import org.polyfrost.oneclient.common.protocol.ProtocolException;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundRestrictions;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundWelcome;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundHello;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OneClientServer implements OneClientApi {
    private final ServerPlatform platform;
    private final SimpleEventBus eventBus;
    private final Map<UUID, ConnectedPlayer> players = new ConcurrentHashMap<>();
    private volatile List<String> restrictedMods = Collections.emptyList();
    private volatile List<String> restrictedHuds = Collections.emptyList();

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
        }
    }

    public void handleQuit(UUID uniqueId) {
        ConnectedPlayer player = players.remove(uniqueId);
        if (player != null) {
            eventBus.post(new OneClientPlayerQuitEvent(player));
        }
    }

    public void sendPacket(UUID uniqueId, Packet packet) {
        platform.sendPluginMessage(uniqueId, Protocol.CLIENTBOUND.encode(packet));
    }

    private void handleHello(UUID uniqueId, String name, ServerboundHello hello) {
        ConnectedPlayer player = new ConnectedPlayer(
            uniqueId,
            name,
            hello.getProtocolVersion(),
            hello.getClientVersion(),
            hello.getMinecraftVersion()
        );
        if (players.putIfAbsent(uniqueId, player) != null) {
            return;
        }

        sendState(uniqueId);
        eventBus.post(new OneClientPlayerJoinEvent(player));
    }

    /**
     * Sends the state again once the client has registered the channel, for platforms that drop plugin
     * messages on channels the client has not registered yet
     */
    public void handleChannelRegistered(UUID uniqueId) {
        if (players.containsKey(uniqueId)) {
            sendState(uniqueId);
        }
    }

    private void sendState(UUID uniqueId) {
        sendPacket(uniqueId, new ClientboundWelcome(Protocol.VERSION, platform.getApiVersion()));
        if (!restrictedMods.isEmpty() || !restrictedHuds.isEmpty()) {
            sendPacket(uniqueId, new ClientboundRestrictions(restrictedMods, restrictedHuds));
        }
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

    @Override
    public void setRestrictions(Collection<String> mods, Collection<String> huds) {
        List<String> nextMods = ids(mods);
        List<String> nextHuds = ids(huds);
        if (nextMods.equals(restrictedMods) && nextHuds.equals(restrictedHuds)) {
            return;
        }
        restrictedMods = nextMods;
        restrictedHuds = nextHuds;
        for (UUID uniqueId : players.keySet()) {
            sendPacket(uniqueId, new ClientboundRestrictions(nextMods, nextHuds));
        }
    }

    @Override
    public Collection<String> getRestrictedMods() {
        return restrictedMods;
    }

    @Override
    public Collection<String> getRestrictedHuds() {
        return restrictedHuds;
    }

    private static List<String> ids(Collection<String> ids) {
        List<String> out = new ArrayList<>();
        for (String id : ids) {
            String trimmed = id == null ? "" : id.trim();
            if (trimmed.isEmpty() || out.contains(trimmed)) {
                continue;
            }
            if (trimmed.getBytes(StandardCharsets.UTF_8).length > ClientboundRestrictions.MAX_ID_BYTES) {
                throw new IllegalArgumentException("Id is longer than " + ClientboundRestrictions.MAX_ID_BYTES + " bytes: " + trimmed);
            }
            out.add(trimmed);
        }
        if (out.size() > ClientboundRestrictions.MAX_ENTRIES) {
            throw new IllegalArgumentException("More than " + ClientboundRestrictions.MAX_ENTRIES + " ids");
        }
        return Collections.unmodifiableList(out);
    }
}
