package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketReader;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ServerboundCapabilities implements Packet {
    public static final int MAX_KEYS = 4096;
    public static final int MAX_KEY_LENGTH = 256;

    private final Set<String> keys;

    public ServerboundCapabilities(Set<String> keys) {
        this.keys = Collections.unmodifiableSet(new LinkedHashSet<>(keys));
    }

    public static ServerboundCapabilities read(PacketReader reader) {
        int count = reader.readCount(MAX_KEYS);
        Set<String> keys = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            keys.add(reader.readString(MAX_KEY_LENGTH));
        }
        return new ServerboundCapabilities(keys);
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(keys.size());
        for (String key : keys) {
            writer.writeString(key);
        }
    }

    public Set<String> getKeys() {
        return keys;
    }
}
