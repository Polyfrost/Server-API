package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

public final class ClientboundWelcome implements Packet {
    private final int protocolVersion;
    private final String serverApiVersion;

    public ClientboundWelcome(int protocolVersion, String serverApiVersion) {
        this.protocolVersion = protocolVersion;
        this.serverApiVersion = serverApiVersion;
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(protocolVersion).writeString(serverApiVersion);
    }
}
