package org.polyfrost.oneclient.common;

import java.util.UUID;

public interface ServerPlatform {
    PlatformLogger getLogger();

    String getApiVersion();

    void sendPluginMessage(UUID player, byte[] data);
}
