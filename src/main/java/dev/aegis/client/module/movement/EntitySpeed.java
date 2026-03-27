package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import org.lwjgl.glfw.GLFW;

public class EntitySpeed extends Module {

    private double speedMultiplier = 2.5;

    public EntitySpeed() {
        super("EntitySpeed", "Makes ridden entities move faster (horses, pigs, boats, minecarts)", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        float yaw = mc.player.getYaw();
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward == 0 && strafe == 0) return;

        double rad = Math.toRadians(yaw);
        double speed = speedMultiplier * 0.05;

        double motionX = (-Math.sin(rad) * forward + Math.cos(rad) * strafe) * speed;
        double motionZ = (Math.cos(rad) * forward + Math.sin(rad) * strafe) * speed;

        vehicle.setVelocity(
                vehicle.getVelocity().x + motionX,
                vehicle.getVelocity().y,
                vehicle.getVelocity().z + motionZ
        );
    }
}
