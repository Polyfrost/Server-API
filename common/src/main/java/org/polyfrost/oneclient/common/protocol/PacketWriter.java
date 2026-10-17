package org.polyfrost.oneclient.common.protocol;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class PacketWriter {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public PacketWriter writeVarInt(int value) {
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value);
        return this;
    }

    public PacketWriter writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        out.write(bytes, 0, bytes.length);
        return this;
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}
