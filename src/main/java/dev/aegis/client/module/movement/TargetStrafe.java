package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class TargetStrafe extends Module {

    private double strafeRadius = 2.5;
    private boolean clockwise = true;
    private int switchTimer = 0;

    public TargetStrafe() {
        super("TargetStrafe", "Strafes around the current combat target in a circle", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) return; // only in air (during bhop)

        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(LivingEntity::isAlive)
                .filter(p -> mc.player.distanceTo(p) <= 6.0)
                .findFirst()
                .orElse(null);

        if (target == null) return;

        switchTimer++;
        if (switchTimer > 40) {
            clockwise = !clockwise;
            switchTimer = 0;
        }

        // reverse on wall collision
        if (mc.player.horizontalCollision) {
            clockwise = !clockwise;
            switchTimer = 0;
        }

        double dx = mc.player.getX() - target.getX();
        double dz = mc.player.getZ() - target.getZ();
        double angle = Math.atan2(dz, dx);

        double strafeAngle = clockwise ? angle + Math.PI / 2 : angle - Math.PI / 2;

        Vec3d vel = mc.player.getVelocity();
        double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        double mx = Math.cos(strafeAngle) * speed;
        double mz = Math.sin(strafeAngle) * speed;

        mc.player.setVelocity(mx, vel.y, mz);
    }

    @Override
    protected void onDisable() {
        switchTimer = 0;
    }
}
