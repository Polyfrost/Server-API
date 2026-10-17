package org.polyfrost.oneclient.common;

import org.polyfrost.oneclient.api.OneClientPlayer;

import java.util.UUID;

final class ConnectedPlayer implements OneClientPlayer {
    private final UUID uniqueId;
    private final String name;
    private final int protocolVersion;
    private final String clientVersion;
    private final String minecraftVersion;
    private final long joinedAtMillis = System.currentTimeMillis();

    ConnectedPlayer(UUID uniqueId, String name, int protocolVersion, String clientVersion, String minecraftVersion) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.protocolVersion = protocolVersion;
        this.clientVersion = clientVersion;
        this.minecraftVersion = minecraftVersion;
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
}
