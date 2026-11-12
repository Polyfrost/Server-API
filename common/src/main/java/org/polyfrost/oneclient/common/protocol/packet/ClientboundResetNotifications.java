package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

public final class ClientboundResetNotifications implements Packet {
    public static final ClientboundResetNotifications INSTANCE = new ClientboundResetNotifications();

    private ClientboundResetNotifications() {
    }

    @Override
    public void write(PacketWriter writer) {
    }
}
