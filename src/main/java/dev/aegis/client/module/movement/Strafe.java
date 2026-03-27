package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Strafe extends Module {

    private double strength = 1.0;

    public Strafe() {
        super("Strafe", "Improves air control and strafing movement", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward == 0 && strafe == 0) return;

        Vec3d vel = mc.player.getVelocity();
        double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);

        double motionX = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
        double motionZ = Math.cos(rad) * forward + Math.sin(rad) * strafe;

        double magnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (magnitude > 0) {
            motionX = (motionX / magnitude) * speed * strength;
            motionZ = (motionZ / magnitude) * speed * strength;
        }

        mc.player.setVelocity(motionX, vel.y, motionZ);
    }
}
