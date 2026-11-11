package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

import java.util.Collection;

/** Mod ids and HUD ids the client switches off while connected, replacing the ones sent before */
public final class ClientboundRestrictions implements Packet {
    public static final int MAX_ENTRIES = 256;
    public static final int MAX_ID_BYTES = 256;

    private final Collection<String> mods;
    private final Collection<String> huds;

    public ClientboundRestrictions(Collection<String> mods, Collection<String> huds) {
        this.mods = mods;
        this.huds = huds;
    }

    @Override
    public void write(PacketWriter writer) {
        writeIds(writer, mods);
        writeIds(writer, huds);
    }

    private static void writeIds(PacketWriter writer, Collection<String> ids) {
        writer.writeVarInt(ids.size());
        for (String id : ids) {
            writer.writeString(id);
        }
    }
}
