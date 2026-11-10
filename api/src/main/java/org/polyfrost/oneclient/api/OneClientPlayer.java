package org.polyfrost.oneclient.api;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface OneClientPlayer {
    UUID getUniqueId();

    String getName();

    int getProtocolVersion();

    String getClientVersion();

    String getMinecraftVersion();

    long getJoinedAtMillis();

    Set<String> getCapabilities();

    boolean supports(String key);

    void setOption(String key, Object value);

    void setOptions(Map<String, ?> values);

    void resetOptions();

    void sendNotification(String titleJson, String descriptionJson, Duration duration, String icon);

    void resetNotifications();
}
