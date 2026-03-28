package dev.aegis.client.module.player;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Freecam extends Module implements EventListener {

    private double flySpeed = 2.0;
    private Vec3d savedPos = null;
    private float savedYaw, savedPitch;

    public Freecam() {
        super("Freecam", "Detach camera and fly around freely while your body stays in place", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            savedPos = mc.player.getPos();
            savedYaw = mc.player.getYaw();
            savedPitch = mc.player.getPitch();
        }
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;
        if (packetEvent.getDirection() != PacketEvent.Direction.SEND) return;

        // block all outgoing movement packets to keep server-side position frozen
        if (packetEvent.getPacket() instanceof PlayerMoveC2SPacket) {
            packetEvent.cancel();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // enable noclip so we can fly through blocks
        mc.player.noClip = true;
        mc.player.setNoGravity(true);

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double rad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double motionX = 0, motionY = 0, motionZ = 0;

        if (forward != 0 || strafe != 0) {
            motionX = (-Math.sin(rad) * forward + Math.cos(rad) * strafe) * flySpeed * 0.05;
            motionZ = (Math.cos(rad) * forward + Math.sin(rad) * strafe) * flySpeed * 0.05;
            motionY = -Math.sin(pitchRad) * forward * flySpeed * 0.05;
        }

        if (mc.options.jumpKey.isPressed()) {
            motionY += flySpeed * 0.05;
        }
        if (mc.options.sneakKey.isPressed()) {
            motionY -= flySpeed * 0.05;
        }

        mc.player.setVelocity(motionX, motionY, motionZ);
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            if (savedPos != null) {
                mc.player.setPos(savedPos.x, savedPos.y, savedPos.z);
            }
            mc.player.noClip = false;
            mc.player.setNoGravity(false);
            mc.player.setVelocity(Vec3d.ZERO);
        }
        savedPos = null;
    }
}
