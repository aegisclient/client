package dev.aegis.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerHelper {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static float[] getRotationsTo(Entity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = (target.getEyeY()) - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        return new float[]{
                MathHelper.wrapDegrees(yaw),
                MathHelper.clamp(pitch, -90, 90)
        };
    }

    public static float[] getRotationsTo(BlockPos pos) {
        Vec3d center = pos.toCenterPos();
        double dx = center.x - mc.player.getX();
        double dy = center.y - mc.player.getEyeY();
        double dz = center.z - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        return new float[]{
                MathHelper.wrapDegrees(yaw),
                MathHelper.clamp(pitch, -90, 90)
        };
    }

    public static boolean isInRange(Entity entity, double range) {
        return mc.player.distanceTo(entity) <= range;
    }

    public static boolean canSee(Entity entity) {
        return mc.player.canSee(entity);
    }

    public static float getHealth(LivingEntity entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static double getSpeed() {
        Vec3d vel = mc.player.getVelocity();
        return Math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20; // blocks per second
    }
}
