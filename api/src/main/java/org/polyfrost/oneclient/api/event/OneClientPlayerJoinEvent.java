package org.polyfrost.oneclient.api.event;

import org.polyfrost.oneclient.api.OneClientPlayer;

public final class OneClientPlayerJoinEvent implements OneClientEvent {
    private final OneClientPlayer player;

    public OneClientPlayerJoinEvent(OneClientPlayer player) {
        this.player = player;
    }

    public OneClientPlayer getPlayer() {
        return player;
    }
}
