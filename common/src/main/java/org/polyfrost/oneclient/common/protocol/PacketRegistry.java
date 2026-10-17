package org.polyfrost.oneclient.common.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PacketRegistry {
    private final Map<Integer, Function<PacketReader, ? extends Packet>> decoders = new HashMap<>();
    private final Map<Class<? extends Packet>, Integer> ids = new HashMap<>();

    public <P extends Packet> PacketRegistry register(int id, Class<P> type, Function<PacketReader, P> decoder) {
        if (ids.containsValue(id)) {
            throw new IllegalArgumentException("Packet id " + id + " is already registered");
        }
        ids.put(type, id);
        if (decoder != null) {
            decoders.put(id, decoder);
        }
        return this;
    }

    public Packet decode(byte[] data) {
        PacketReader reader = new PacketReader(data);
        Function<PacketReader, ? extends Packet> decoder = decoders.get(reader.readVarInt());
        return decoder == null ? null : decoder.apply(reader);
    }

    public byte[] encode(Packet packet) {
        Integer id = ids.get(packet.getClass());
        if (id == null) {
            throw new IllegalArgumentException("Unregistered packet " + packet.getClass().getName());
        }
        PacketWriter writer = new PacketWriter().writeVarInt(id);
        packet.write(writer);
        return writer.toByteArray();
    }
}
