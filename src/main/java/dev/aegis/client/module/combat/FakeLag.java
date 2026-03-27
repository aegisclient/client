package dev.aegis.client.module.combat;

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

public class FakeLag extends Module implements EventListener {

    private final Deque<Packet<?>> heldPackets = new ArrayDeque<>();
    private int delayTicks = 4;
    private int tickCounter = 0;

    public FakeLag() {
        super("FakeLag", "Holds outgoing movement packets to create artificial lag", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;
        if (packetEvent.getDirection() != PacketEvent.Direction.SEND) return;

        if (packetEvent.getPacket() instanceof PlayerMoveC2SPacket) {
            heldPackets.add(packetEvent.getPacket());
            packetEvent.cancel();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        tickCounter++;
        if (tickCounter >= delayTicks) {
            tickCounter = 0;
            // flush all held packets
            while (!heldPackets.isEmpty()) {
                mc.getNetworkHandler().sendPacket(heldPackets.poll());
            }
        }
    }

    @Override
    protected void onDisable() {
        // flush remaining packets
        if (mc.getNetworkHandler() != null) {
            while (!heldPackets.isEmpty()) {
                mc.getNetworkHandler().sendPacket(heldPackets.poll());
            }
        }
        tickCounter = 0;
    }
}
