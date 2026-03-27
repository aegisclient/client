package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class Phase extends Module {

    public Phase() {
        super("Phase", "Allows you to clip through blocks by disabling client collision", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // disable collision
        mc.player.noClip = true;

        // gravity off while phasing
        mc.player.setNoGravity(true);

        double speed = 0.3;
        float yaw = (float) Math.toRadians(mc.player.getYaw());

        double motionX = 0, motionY = 0, motionZ = 0;
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward != 0 || strafe != 0) {
            motionX = (-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * speed;
            motionZ = (Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * speed;
        }

        if (mc.options.jumpKey.isPressed()) {
            motionY = speed;
        } else if (mc.options.sneakKey.isPressed()) {
            motionY = -speed;
        }

        mc.player.setVelocity(motionX, motionY, motionZ);
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.noClip = false;
            mc.player.setNoGravity(false);
        }
    }
}
