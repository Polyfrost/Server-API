package org.polyfrost.oneclient.common.protocol.packet;

import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.PacketReader;
import org.polyfrost.oneclient.common.protocol.PacketWriter;

public final class ClientboundNotification implements Packet {
    private static final int MAX_TEXT_LENGTH = 262144;
    private static final int MAX_ICON_LENGTH = 256;

    private final String titleJson;
    private final String descriptionJson;
    private final int durationMillis;
    private final String icon;

    public ClientboundNotification(String titleJson, String descriptionJson, int durationMillis, String icon) {
        this.titleJson = titleJson;
        this.descriptionJson = descriptionJson;
        this.durationMillis = durationMillis;
        this.icon = icon;
    }

    public static ClientboundNotification read(PacketReader reader) {
        return new ClientboundNotification(
            reader.readString(MAX_TEXT_LENGTH),
            reader.readString(MAX_TEXT_LENGTH),
            reader.readVarInt(),
            reader.readString(MAX_ICON_LENGTH)
        );
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeString(titleJson)
            .writeString(descriptionJson)
            .writeVarInt(durationMillis)
            .writeString(icon);
    }

    public String getTitleJson() {
        return titleJson;
    }

    public String getDescriptionJson() {
        return descriptionJson;
    }

    public int getDurationMillis() {
        return durationMillis;
    }

    public String getIcon() {
        return icon;
    }
}
