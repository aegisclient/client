package dev.aegis.client.module.combat;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public class BackTrack extends Module implements EventListener {

    private final Deque<Packet<?>> delayedPackets = new ArrayDeque<>();
    private int maxDelay = 200; // ms
    private long lastRelease = 0;

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

            // delay entity movement packets
            long now = System.currentTimeMillis();
            if (now - lastRelease < maxDelay) {
                delayedPackets.add(packet);
                packetEvent.cancel();
            }
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRelease >= maxDelay) {
            lastRelease = now;
            delayedPackets.clear();
        }
    }

    @Override
    protected void onDisable() {
        delayedPackets.clear();
    }
}
