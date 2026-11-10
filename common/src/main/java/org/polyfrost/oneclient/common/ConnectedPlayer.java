package org.polyfrost.oneclient.common;

import org.polyfrost.oneclient.api.OneClientPlayer;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundNotification;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundResetNotifications;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundResetOptions;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundSetOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class ConnectedPlayer implements OneClientPlayer {
    private static final String NOTIFICATION = "notification";

    private final OneClientServer server;
    private final UUID uniqueId;
    private final String name;
    private final int protocolVersion;
    private final String clientVersion;
    private final String minecraftVersion;
    private final Set<String> capabilities;
    private final long joinedAtMillis = System.currentTimeMillis();

    ConnectedPlayer(OneClientServer server, UUID uniqueId, String name, int protocolVersion, String clientVersion,
                    String minecraftVersion, Set<String> capabilities) {
        this.server = server;
        this.uniqueId = uniqueId;
        this.name = name;
        this.protocolVersion = protocolVersion;
        this.clientVersion = clientVersion;
        this.minecraftVersion = minecraftVersion;
        this.capabilities = Collections.unmodifiableSet(capabilities);
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String getClientVersion() {
        return clientVersion;
    }

    @Override
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public long getJoinedAtMillis() {
        return joinedAtMillis;
    }

    @Override
    public Set<String> getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean supports(String key) {
        return capabilities.contains(key);
    }

    @Override
    public void setOption(String key, Object value) {
        setOptions(Collections.singletonMap(key, value));
    }

    @Override
    public void setOptions(Map<String, ?> values) {
        Map<String, Object> supported = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "key");
            Object value = entry.getValue();
            if (value != null && !(value instanceof Boolean || value instanceof Number || value instanceof String)) {
                throw new IllegalArgumentException("Unsupported value type for " + key + ": " + value.getClass().getName());
            }
            if (supports(key)) {
                supported.put(key, value);
            } else {
                server.logger().debug("Dropping option " + key + " for " + name + ": not supported by the client");
            }
        }
        if (!supported.isEmpty()) {
            server.send(this, new ClientboundSetOptions(supported));
        }
    }

    @Override
    public void resetOptions() {
        if (!capabilities.isEmpty()) {
            server.send(this, ClientboundResetOptions.INSTANCE);
        }
    }

    @Override
    public void sendNotification(String titleJson, String descriptionJson, Duration duration, String icon) {
        Objects.requireNonNull(titleJson, "titleJson");
        Objects.requireNonNull(descriptionJson, "descriptionJson");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Negative notification duration " + duration);
        }
        if (!supports(NOTIFICATION)) {
            server.logger().debug("Dropping notification for " + name + ": not supported by the client");
            return;
        }
        int millis = (int) Math.min(duration.toMillis(), Integer.MAX_VALUE);
        server.send(this, new ClientboundNotification(titleJson, descriptionJson, millis, icon == null ? "" : icon));
    }

    @Override
    public void resetNotifications() {
        if (supports(NOTIFICATION)) {
            server.send(this, ClientboundResetNotifications.INSTANCE);
        }
    }
}
