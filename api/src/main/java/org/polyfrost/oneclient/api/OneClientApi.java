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

    /**
     * Switches mods and HUDs off for every OneClient player, replacing what was set before
     * <p>
     * Players get the list when they join and whenever it changes, and their own settings come back when
     * they leave. Mods are named by mod id and HUDs by HUD id, at most 256 of each and 256 bytes per id
     *
     * @throws IllegalArgumentException if a list is too long or an id too long
     */
    void setRestrictions(Collection<String> mods, Collection<String> huds);

    Collection<String> getRestrictedMods();

    Collection<String> getRestrictedHuds();
}
