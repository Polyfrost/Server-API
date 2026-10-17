package org.polyfrost.oneclient.common.protocol;

public interface Packet {
    void write(PacketWriter writer);
}
