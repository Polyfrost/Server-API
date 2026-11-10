package org.polyfrost.oneclient.common.protocol;

import org.polyfrost.oneclient.common.protocol.packet.ClientboundNotification;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundResetNotifications;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundResetOptions;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundSetOptions;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundWelcome;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundCapabilities;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundHello;

public final class Protocol {
    public static final String CHANNEL = "oneclient:api";
    public static final int VERSION = 2;

    public static final PacketRegistry SERVERBOUND = new PacketRegistry()
        .register(0x00, ServerboundHello.class, ServerboundHello::read)
        .register(0x01, ServerboundCapabilities.class, ServerboundCapabilities::read);

    public static final PacketRegistry CLIENTBOUND = new PacketRegistry()
        .register(0x00, ClientboundWelcome.class, ClientboundWelcome::read)
        .register(0x01, ClientboundSetOptions.class, ClientboundSetOptions::read)
        .register(0x02, ClientboundResetOptions.class, reader -> ClientboundResetOptions.INSTANCE)
        .register(0x03, ClientboundNotification.class, ClientboundNotification::read)
        .register(0x04, ClientboundResetNotifications.class, reader -> ClientboundResetNotifications.INSTANCE);
}
