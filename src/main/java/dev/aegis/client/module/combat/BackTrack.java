package dev.aegis.client.module.combat;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public class BackTrack extends Module implements EventListener {

    private static class TimedPacket {
        final Packet<?> packet;
        final long timestamp;
        TimedPacket(Packet<?> packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }

    private final Deque<TimedPacket> delayedPackets = new ArrayDeque<>();
    private int maxDelay = 200; // ms

    public BackTrack() {
        super("BackTrack", "Delays incoming entity position packets to extend hit range", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;
        if (packetEvent.getDirection() != PacketEvent.Direction.RECEIVE) return;

        Packet<?> packet = packetEvent.getPacket();

        if (packet instanceof EntityPositionS2CPacket || packet instanceof EntityS2CPacket) {
            if (mc.player == null) return;

            // queue the packet and cancel it for now
            delayedPackets.add(new TimedPacket(packet, System.currentTimeMillis()));
            packetEvent.cancel();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // replay packets that have been held long enough
        long now = System.currentTimeMillis();
        while (!delayedPackets.isEmpty()) {
            TimedPacket timed = delayedPackets.peek();
            if (now - timed.timestamp >= maxDelay) {
                delayedPackets.poll();
                // replay by handling the packet through the network handler
                try {
                    handlePacket(timed.packet);
                } catch (Exception ignored) {}
            } else {
                break; // remaining packets are newer, not ready yet
            }
        }
    }

    @Override
    protected void onDisable() {
        // replay all remaining packets immediately
        if (mc.getNetworkHandler() != null) {
            while (!delayedPackets.isEmpty()) {
                TimedPacket timed = delayedPackets.poll();
                try {
                    handlePacket(timed.packet);
                } catch (Exception ignored) {}
            }
        }
        delayedPackets.clear();
    }

    @SuppressWarnings("unchecked")
    private void handlePacket(Packet<?> packet) {
        if (mc.getNetworkHandler() != null) {
            ((Packet<ClientPlayPacketListener>) packet).apply(mc.getNetworkHandler());
        }
    }

    public void setMaxDelay(int ms) { this.maxDelay = ms; }
    public int getMaxDelay() { return maxDelay; }
}
