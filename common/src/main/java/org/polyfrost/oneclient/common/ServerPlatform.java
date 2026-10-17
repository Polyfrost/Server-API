package org.polyfrost.oneclient.common;

import java.util.UUID;
import java.util.logging.Logger;

public interface ServerPlatform {
    Logger getLogger();

    String getApiVersion();

    void sendPluginMessage(UUID player, byte[] data);
}
