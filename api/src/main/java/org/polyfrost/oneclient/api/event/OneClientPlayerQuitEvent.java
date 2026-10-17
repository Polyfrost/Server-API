package org.polyfrost.oneclient.api.event;

import org.polyfrost.oneclient.api.OneClientPlayer;

public final class OneClientPlayerQuitEvent implements OneClientEvent {
    private final OneClientPlayer player;

    public OneClientPlayerQuitEvent(OneClientPlayer player) {
        this.player = player;
    }

    public OneClientPlayer getPlayer() {
        return player;
    }
}
