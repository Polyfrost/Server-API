package org.polyfrost.oneclient.common;

import org.junit.jupiter.api.Test;
import org.polyfrost.oneclient.common.protocol.Packet;
import org.polyfrost.oneclient.common.protocol.Protocol;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundSetOptions;
import org.polyfrost.oneclient.common.protocol.packet.ClientboundWelcome;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundCapabilities;
import org.polyfrost.oneclient.common.protocol.packet.ServerboundHello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultOptionsTest {
    private static final UUID PLAYER = UUID.randomUUID();

    private final List<Packet> sent = new ArrayList<>();
    private final OneClientServer server = new OneClientServer(new ServerPlatform() {
        @Override
        public PlatformLogger getLogger() {
            return PlatformLogger.of(java.util.logging.Logger.getAnonymousLogger());
        }

        @Override
        public String getApiVersion() {
            return "test";
        }

        @Override
        public void sendPluginMessage(UUID player, byte[] data) {
            sent.add(Protocol.CLIENTBOUND.decode(data));
        }
    });

    private void receive(Packet packet) {
        server.handlePluginMessage(PLAYER, "player", Protocol.SERVERBOUND.encode(packet));
    }

    private static Map<String, Object> map(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put((String) entries[i], entries[i + 1]);
        }
        return map;
    }

    @Test
    void welcomeIsResentOnlyWhileWaitingForCapabilities() {
        server.handleChannelRegistered(PLAYER);
        assertTrue(sent.isEmpty());

        receive(new ServerboundHello(Protocol.VERSION, "1", "26.3"));
        server.handleChannelRegistered(PLAYER);
        assertEquals(2, sent.size());
        assertInstanceOf(ClientboundWelcome.class, sent.get(1));

        receive(new ServerboundCapabilities(Collections.singleton("a")));
        sent.clear();
        server.handleChannelRegistered(PLAYER);
        assertTrue(sent.isEmpty());
    }

    @Test
    void defaultsReachJoiningAndOnlinePlayers() {
        server.setDefaultOptions(map("a", false, "unsupported", 1));
        receive(new ServerboundHello(Protocol.VERSION, "1", "26.3"));
        receive(new ServerboundCapabilities(new LinkedHashSet<>(Arrays.asList("a", "b"))));
        assertEquals(map("a", false), ((ClientboundSetOptions) sent.get(1)).getValues());

        server.setDefaultOptions(map("b", 2.5));
        assertEquals(map("a", null, "b", 2.5), ((ClientboundSetOptions) sent.get(2)).getValues());
    }

    @Test
    void badValuesKeepThePreviousDefaults() {
        server.setDefaultOptions(map("a", true));
        Map<String, Object> bad = new HashMap<>();
        bad.put("a", Collections.emptyList());
        assertThrows(IllegalArgumentException.class, () -> server.setDefaultOptions(bad));
        assertEquals(map("a", true), server.getDefaultOptions());
    }
}
