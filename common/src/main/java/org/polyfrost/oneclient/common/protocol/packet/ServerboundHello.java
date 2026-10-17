package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketReader;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

public final class ServerboundHello implements Packet {
    private static final int MAX_VERSION_LENGTH = 64;

    private final int protocolVersion;
    private final String clientVersion;
    private final String minecraftVersion;

    public ServerboundHello(int protocolVersion, String clientVersion, String minecraftVersion) {
        this.protocolVersion = protocolVersion;
        this.clientVersion = clientVersion;
        this.minecraftVersion = minecraftVersion;
    }

    public static ServerboundHello read(PacketReader reader) {
        return new ServerboundHello(
            reader.readVarInt(),
            reader.readString(MAX_VERSION_LENGTH),
            reader.readString(MAX_VERSION_LENGTH)
        );
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(protocolVersion)
            .writeString(clientVersion)
            .writeString(minecraftVersion);
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }
}
