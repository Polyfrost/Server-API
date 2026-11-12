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
        return writeString(value, Integer.MAX_VALUE);
    }

    public PacketWriter writeString(String value, int maxBytes) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxBytes) {
            throw new IllegalArgumentException("String is " + bytes.length + " UTF-8 bytes, limit is " + maxBytes);
        }
        writeVarInt(bytes.length);
        out.write(bytes, 0, bytes.length);
        return this;
    }

    public PacketWriter writeByte(int value) {
        out.write(value);
        return this;
    }

    public PacketWriter writeLong(long value) {
        for (int shift = 56; shift >= 0; shift -= 8) {
            out.write((int) (value >>> shift));
        }
        return this;
    }

    public PacketWriter writeDouble(double value) {
        return writeLong(Double.doubleToLongBits(value));
    }

    public PacketWriter writeValue(Object value, int maxStringBytes) {
        if (value == null) {
            return writeByte(0);
        }
        if (value instanceof Boolean) {
            return writeByte(1).writeByte((Boolean) value ? 1 : 0);
        }
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
            int n = ((Number) value).intValue();
            return writeByte(2).writeVarInt((n << 1) ^ (n >> 31));
        }
        if (value instanceof Number) {
            return writeByte(3).writeDouble(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            return writeByte(4).writeString((String) value, maxStringBytes);
        }
        throw new IllegalArgumentException("Unsupported option value type " + value.getClass().getName());
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}
