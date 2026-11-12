package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

public final class ClientboundResetOptions implements Packet {
    public static final ClientboundResetOptions INSTANCE = new ClientboundResetOptions();

    private ClientboundResetOptions() {
    }

    @Override
    public void write(PacketWriter writer) {
    }
}
