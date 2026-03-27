package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Speed extends Module {

    private double speedMultiplier = 1.8;

    public Speed() {
        super("Speed", "Increases movement speed beyond normal limits", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward == 0 && strafe == 0) return;

        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);

        double motionX = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
        double motionZ = Math.cos(rad) * forward + Math.sin(rad) * strafe;

        double magnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (magnitude > 0) {
            motionX = (motionX / magnitude) * (0.2873 * speedMultiplier);
            motionZ = (motionZ / magnitude) * (0.2873 * speedMultiplier);
        }

        // bunny hop to maintain speed
        if (mc.player.isOnGround()) {
            mc.player.jump();
        }

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(motionX, vel.y, motionZ);
    }

    public void setSpeedMultiplier(double mult) { this.speedMultiplier = mult; }
}
