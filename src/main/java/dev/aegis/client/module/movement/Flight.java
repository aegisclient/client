package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Flight extends Module {

    private double flySpeed = 2.5;

    public Flight() {
        super("Flight", "Allows you to fly in survival mode", Category.MOVEMENT, GLFW.GLFW_KEY_F);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        mc.player.getAbilities().flying = true;

        Vec3d velocity = mc.player.getVelocity();
        double motionY = 0;

        if (mc.options.jumpKey.isPressed()) {
            motionY = flySpeed * 0.5;
        } else if (mc.options.sneakKey.isPressed()) {
            motionY = -flySpeed * 0.5;
        }

        // calculate forward motion based on player look direction
        float yaw = mc.player.getYaw();
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, motionY, 0);
            return;
        }

        double rad = Math.toRadians(yaw);
        double motionX = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
        double motionZ = Math.cos(rad) * forward + Math.sin(rad) * strafe;

        // normalize and apply speed
        double magnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (magnitude > 0) {
            motionX = (motionX / magnitude) * flySpeed;
            motionZ = (motionZ / magnitude) * flySpeed;
        }

        mc.player.setVelocity(motionX, motionY, motionZ);
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().allowFlying = mc.player.isCreative();
        }
    }

    public void setFlySpeed(double speed) { this.flySpeed = speed; }
}
