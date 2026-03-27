package dev.aegis.client.module.player;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public class Blink extends Module implements EventListener {

    private final Deque<Packet<?>> packets = new ArrayDeque<>();

    public Blink() {
        super("Blink", "Queues all outgoing movement packets and releases on disable", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;
        if (packetEvent.getDirection() != PacketEvent.Direction.SEND) return;

        if (packetEvent.getPacket() instanceof PlayerMoveC2SPacket) {
            packets.add(packetEvent.getPacket());
            packetEvent.cancel();
        }
    }

    @Override
    public void onTick() {
        // just hold packets while enabled
    }

    @Override
    protected void onDisable() {
        if (mc.getNetworkHandler() != null) {
            while (!packets.isEmpty()) {
                mc.getNetworkHandler().sendPacket(packets.poll());
            }
        }
    }
}
