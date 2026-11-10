package org.polyfrost.oneclient.common.protocol;

import org.junit.jupiter.api.Test;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundNotification;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundResetNotifications;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundResetOptions;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundSetOptions;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundWelcome;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundCapabilities;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundHello;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProtocolRoundTripTest {
    @SuppressWarnings("unchecked")
    private static <P extends Packet> P roundTrip(PacketRegistry registry, P packet) {
        return (P) registry.decode(registry.encode(packet));
    }

    @Test
    void hello() {
        ServerboundHello hello = roundTrip(Protocol.SERVERBOUND, new ServerboundHello(2, "1.0.0", "1.21.11"));
        assertEquals(2, hello.getProtocolVersion());
        assertEquals("1.0.0", hello.getClientVersion());
        assertEquals("1.21.11", hello.getMinecraftVersion());
    }

    @Test
    void capabilities() {
        ServerboundCapabilities sent = new ServerboundCapabilities(
            new LinkedHashSet<>(Arrays.asList("fov.default-fov", "notification", "combat.disable-miss-penalty")));
        assertEquals(sent.getKeys(), roundTrip(Protocol.SERVERBOUND, sent).getKeys());
        assertEquals(Collections.emptySet(),
            roundTrip(Protocol.SERVERBOUND, new ServerboundCapabilities(Collections.<String>emptySet())).getKeys());
    }

    @Test
    void welcome() {
        ClientboundWelcome welcome = roundTrip(Protocol.CLIENTBOUND, new ClientboundWelcome(Protocol.VERSION, "0.1.0"));
        assertEquals(Protocol.VERSION, welcome.getProtocolVersion());
        assertEquals("0.1.0", welcome.getServerApiVersion());
    }

    @Test
    void setOptionsWithEveryValueTag() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("reset", null);
        values.put("bool.true", true);
        values.put("bool.false", false);
        values.put("int.zero", 0);
        values.put("int.negative", -1);
        values.put("int.min", Integer.MIN_VALUE);
        values.put("int.max", Integer.MAX_VALUE);
        values.put("double", 0.75);
        values.put("double.negative", -1e300);
        values.put("string", "ffffffff");

        assertEquals(values, roundTrip(Protocol.CLIENTBOUND, new ClientboundSetOptions(values)).getValues());
    }

    @Test
    void valueWireFormat() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("a", null);
        values.put("b", true);
        values.put("c", -2);
        values.put("d", 1.0);
        values.put("e", "x");
        byte[] expected = {
            0x01, 0x05,
            0x01, 'a', 0x00,
            0x01, 'b', 0x01, 0x01,
            0x01, 'c', 0x02, 0x03,
            0x01, 'd', 0x03, 0x3F, (byte) 0xF0, 0, 0, 0, 0, 0, 0,
            0x01, 'e', 0x04, 0x01, 'x'
        };
        assertArrayEquals(expected, Protocol.CLIENTBOUND.encode(new ClientboundSetOptions(values)));
    }

    @Test
    void otherNumbersEncodeAsIntOrDouble() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("short", (short) 5);
        values.put("long", 5L);
        values.put("float", 0.5f);
        Map<String, Object> decoded = roundTrip(Protocol.CLIENTBOUND, new ClientboundSetOptions(values)).getValues();
        assertEquals(5, decoded.get("short"));
        assertEquals(5.0, decoded.get("long"));
        assertEquals(0.5, decoded.get("float"));
    }

    @Test
    void unsupportedValueTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PacketWriter().writeValue(new Object()));
    }

    @Test
    void resets() {
        assertSame(ClientboundResetOptions.INSTANCE, roundTrip(Protocol.CLIENTBOUND, ClientboundResetOptions.INSTANCE));
        assertSame(ClientboundResetNotifications.INSTANCE,
            roundTrip(Protocol.CLIENTBOUND, ClientboundResetNotifications.INSTANCE));
        assertArrayEquals(new byte[]{0x02}, Protocol.CLIENTBOUND.encode(ClientboundResetOptions.INSTANCE));
        assertArrayEquals(new byte[]{0x04}, Protocol.CLIENTBOUND.encode(ClientboundResetNotifications.INSTANCE));
    }

    @Test
    void notification() {
        ClientboundNotification notification = roundTrip(Protocol.CLIENTBOUND, new ClientboundNotification(
            "{\"text\":\"Hi\"}", "{\"text\":\"Welcome\",\"color\":\"gold\"}", 5000, ""));
        assertEquals("{\"text\":\"Hi\"}", notification.getTitleJson());
        assertEquals("{\"text\":\"Welcome\",\"color\":\"gold\"}", notification.getDescriptionJson());
        assertEquals(5000, notification.getDurationMillis());
        assertEquals("", notification.getIcon());
    }

    @Test
    void malformedCapabilitiesAreRejected() {
        byte[] tooMany = new PacketWriter().writeVarInt(0x01).writeVarInt(ServerboundCapabilities.MAX_KEYS + 1).toByteArray();
        assertThrows(ProtocolException.class, () -> Protocol.SERVERBOUND.decode(tooMany));

        byte[] negativeCount = new PacketWriter().writeVarInt(0x01).writeVarInt(-1).toByteArray();
        assertThrows(ProtocolException.class, () -> Protocol.SERVERBOUND.decode(negativeCount));

        StringBuilder longKey = new StringBuilder();
        for (int i = 0; i <= ServerboundCapabilities.MAX_KEY_LENGTH; i++) {
            longKey.append('k');
        }
        byte[] keyTooLong = new PacketWriter().writeVarInt(0x01).writeVarInt(1).writeString(longKey.toString()).toByteArray();
        assertThrows(ProtocolException.class, () -> Protocol.SERVERBOUND.decode(keyTooLong));

        byte[] truncated = new PacketWriter().writeVarInt(0x01).writeVarInt(2).writeString("notification").toByteArray();
        assertThrows(ProtocolException.class, () -> Protocol.SERVERBOUND.decode(truncated));
    }

    @Test
    void malformedValuesAreRejected() {
        byte[] unknownTag = new PacketWriter().writeVarInt(0x01).writeVarInt(1).writeString("k").writeByte(5).toByteArray();
        assertThrows(ProtocolException.class, () -> Protocol.CLIENTBOUND.decode(unknownTag));

        byte[] shortDouble = new PacketWriter().writeVarInt(0x01).writeVarInt(1).writeString("k").writeByte(3).writeByte(0).toByteArray();
        assertThrows(ProtocolException.class, () -> Protocol.CLIENTBOUND.decode(shortDouble));
    }
}
