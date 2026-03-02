package dev.aegis.client.event.events;

import dev.aegis.client.event.Event;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends Event {

    private final Packet<?> packet;
    private final Direction direction;

    public PacketEvent(Packet<?> packet, Direction direction) {
        this.packet = packet;
        this.direction = direction;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        SEND,
        RECEIVE
    }
}
