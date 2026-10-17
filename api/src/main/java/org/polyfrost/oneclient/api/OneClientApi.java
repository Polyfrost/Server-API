package org.polyfrost.oneclient.api;

import org.polyfrost.oneclient.api.event.EventBus;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface OneClientApi {
    Optional<OneClientPlayer> getPlayer(UUID uniqueId);

    boolean isOneClient(UUID uniqueId);

    Collection<OneClientPlayer> getPlayers();

    EventBus getEventBus();
}
