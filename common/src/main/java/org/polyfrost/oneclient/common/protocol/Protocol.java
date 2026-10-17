package org.polyfrost.oneclient.common.protocol;

import org.polyfrost.oneclient.common.protocol.packet.ClientboundWelcome;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundHello;

public final class Protocol {
    public static final String CHANNEL = "oneclient:api";
    public static final int VERSION = 1;

    public static final PacketRegistry SERVERBOUND = new PacketRegistry()
        .register(0x00, ServerboundHello.class, ServerboundHello::read);

    public static final PacketRegistry CLIENTBOUND = new PacketRegistry()
        .register(0x00, ClientboundWelcome.class, null);
}
