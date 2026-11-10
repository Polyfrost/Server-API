package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketReader;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

public final class ClientboundWelcome implements Packet {
    private final int protocolVersion;
    private final String serverApiVersion;

    public ClientboundWelcome(int protocolVersion, String serverApiVersion) {
        this.protocolVersion = protocolVersion;
        this.serverApiVersion = serverApiVersion;
    }

    public static ClientboundWelcome read(PacketReader reader) {
        return new ClientboundWelcome(reader.readVarInt(), reader.readString(64));
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(protocolVersion).writeString(serverApiVersion);
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getServerApiVersion() {
        return serverApiVersion;
    }
}
