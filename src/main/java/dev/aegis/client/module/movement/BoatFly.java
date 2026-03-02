package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class BoatFly extends Module {

    private double boatSpeed = 3.0;

    public BoatFly() {
        super("BoatFly", "Fly while riding a boat", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof BoatEntity boat)) return;

        boat.setNoGravity(true);

        double motionY = 0;
        if (mc.options.jumpKey.isPressed()) {
            motionY = boatSpeed * 0.5;
        } else if (mc.options.sneakKey.isPressed()) {
            motionY = -boatSpeed * 0.5;
        }

        float yaw = mc.player.getYaw();
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double rad = Math.toRadians(yaw);
        double motionX = 0, motionZ = 0;

        if (forward != 0 || strafe != 0) {
            motionX = -Math.sin(rad) * forward * boatSpeed;
            motionZ = Math.cos(rad) * forward * boatSpeed;
        }

        boat.setVelocity(motionX, motionY, motionZ);
    }

    @Override
    protected void onDisable() {
        if (mc.player != null && mc.player.getVehicle() instanceof BoatEntity boat) {
            boat.setNoGravity(false);
        }
    }
}
