package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketReader;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientboundSetOptions implements Packet {
    private static final int MAX_STRING_VALUE_LENGTH = 32767;

    private final Map<String, Object> values;

    public ClientboundSetOptions(Map<String, ?> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public static ClientboundSetOptions read(PacketReader reader) {
        int count = reader.readCount(ServerboundCapabilities.MAX_KEYS);
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            values.put(reader.readString(ServerboundCapabilities.MAX_KEY_LENGTH), reader.readValue(MAX_STRING_VALUE_LENGTH));
        }
        return new ClientboundSetOptions(values);
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(values.size());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            writer.writeString(entry.getKey()).writeValue(entry.getValue());
        }
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
