package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class LongJump extends Module {

    private double boostMultiplier = 2.5;
    private boolean jumped = false;

    public LongJump() {
        super("LongJump", "Launch forward with extra momentum when jumping", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.isOnGround()) {
            jumped = false;
        }

        // detect jump start
        if (!mc.player.isOnGround() && mc.player.getVelocity().y > 0.2 && !jumped) {
            if (mc.player.input.movementForward > 0) {
                jumped = true;

                float yaw = (float) Math.toRadians(mc.player.getYaw());
                double boostX = -Math.sin(yaw) * boostMultiplier * 0.2;
                double boostZ = Math.cos(yaw) * boostMultiplier * 0.2;

                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x + boostX, vel.y + 0.1, vel.z + boostZ);
            }
        }
    }
}
