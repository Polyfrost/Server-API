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

    public int readCount(int max) {
        int count = readVarInt();
        if (count < 0 || count > max) {
            throw new ProtocolException("Count " + count + " exceeds limit of " + max);
        }
        return count;
    }

    public long readLong() {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (readByte() & 0xFF);
        }
        return value;
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public Object readValue(int maxStringBytes) {
        byte tag = readByte();
        switch (tag) {
            case 0:
                return null;
            case 1:
                return readByte() != 0;
            case 2:
                int zigzag = readVarInt();
                return (zigzag >>> 1) ^ -(zigzag & 1);
            case 3:
                return readDouble();
            case 4:
                return readString(maxStringBytes);
            default:
                throw new ProtocolException("Unknown value tag " + tag);
        }
    }

    public int remaining() {
        return data.length - position;
    }

    public byte readByte() {
        if (position >= data.length) {
            throw new ProtocolException("Unexpected end of packet");
        }
        return data[position++];
    }
}
