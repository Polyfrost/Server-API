package org.polyfrost.oneclient.api;

import java.util.UUID;

public interface OneClientPlayer {
    UUID getUniqueId();

    String getName();

    int getProtocolVersion();

    String getClientVersion();

    String getMinecraftVersion();

    long getJoinedAtMillis();
}
