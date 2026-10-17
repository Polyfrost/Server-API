package org.polyfrost.oneclient.common.protocol;

import java.nio.charset.StandardCharsets;

public final class PacketReader {
    private final byte[] data;
    private int position;

    public PacketReader(byte[] data) {
        this.data = data;
    }

    public int readVarInt() {
        int value = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            byte b = readByte();
            value |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return value;
            }
        }
        throw new ProtocolException("VarInt is too long");
    }

    public String readString(int maxBytes) {
        int length = readVarInt();
        if (length < 0 || length > maxBytes) {
            throw new ProtocolException("String length " + length + " exceeds limit of " + maxBytes);
        }
        if (length > remaining()) {
            throw new ProtocolException("String length " + length + " exceeds remaining " + remaining() + " bytes");
        }
        String value = new String(data, position, length, StandardCharsets.UTF_8);
        position += length;
        return value;
    }

    public int remaining() {
        return data.length - position;
    }

    private byte readByte() {
        if (position >= data.length) {
            throw new ProtocolException("Unexpected end of packet");
        }
        return data[position++];
    }
}
