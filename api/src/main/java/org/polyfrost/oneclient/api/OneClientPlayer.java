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

    /**
     * Option keys the client can apply, plus {@code "notification"} if it can show notifications.
     * Empty for protocol 1 clients.
     */
    Set<String> getCapabilities();

    boolean supports(String key);

    /**
     * Same as {@link #setOptions(Map)} with one entry.
     */
    void setOption(String key, Object value);

    /**
     * Overrides the player's mod options until they leave or you reset them. Values can be
     * {@code Boolean}, {@code Integer}, any other {@code Number} (sent as a double), {@code String},
     * or {@code null} to reset that one key. Keys the client didn't advertise are skipped.
     *
     * @throws IllegalArgumentException if a value has another type, a string value is over 4,096
     *     UTF-8 bytes, there are over 1,024 keys, or the packet is over 32,766 bytes
     */
    void setOptions(Map<String, ?> values);

    /**
     * Puts back every option this server changed.
     */
    void resetOptions();

    /**
     * Shows a notification if the client supports {@code "notification"}; otherwise does nothing.
     * The client renders the title and description as plain text, cut to 256 and 2,048 characters.
     * It shows the notification for 1 to 60 seconds, and a zero duration uses its default.
     *
     * @param titleJson a JSON text component
     * @param descriptionJson a JSON text component
     * @param icon sent to the client, but the current client ignores it; pass {@code null}
     * @throws IllegalArgumentException if the duration is negative, either text is over 16,384
     *     UTF-8 bytes, the icon is over 256 bytes, or the packet is over 32,766 bytes
     */
    void sendNotification(String titleJson, String descriptionJson, Duration duration, String icon);

    /**
     * Dismisses every notification this server sent.
     */
    void resetNotifications();
}
