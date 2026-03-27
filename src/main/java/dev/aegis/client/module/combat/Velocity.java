package dev.aegis.client.module.combat;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.lwjgl.glfw.GLFW;

public class Velocity extends Module implements EventListener {

    private double horizontal = 0.0;
    private double vertical = 0.0;

    public Velocity() {
        super("Velocity", "Reduces or cancels knockback from hits and explosions", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;
        if (packetEvent.getDirection() != PacketEvent.Direction.RECEIVE) return;

        if (packetEvent.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (mc.player != null && packet.getId() == mc.player.getId()) {
                if (horizontal == 0 && vertical == 0) {
                    // full anti-kb: cancel the packet entirely
                    packetEvent.cancel();
                }
            }
        }

        if (packetEvent.getPacket() instanceof ExplosionS2CPacket) {
            if (horizontal == 0 && vertical == 0) {
                packetEvent.cancel();
            }
        }
    }

    @Override
    public void onTick() {
        // backup method: zero out velocity every tick
        if (mc.player == null) return;
        // only intervene if we just got hit (hurtTime > 0)
        if (mc.player.hurtTime > 0 && horizontal == 0 && vertical == 0) {
            mc.player.setVelocity(
                    mc.player.getVelocity().x * horizontal,
                    mc.player.getVelocity().y * vertical,
                    mc.player.getVelocity().z * horizontal
            );
        }
    }
}
