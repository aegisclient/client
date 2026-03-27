package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Speed extends Module {

    private int stage = 0;
    private double moveSpeed = 0.2873;
    private double lastDist = 0;
    private int ticksSinceJump = 0;

    public Speed() {
        super("Speed", "Increases movement speed beyond normal limits", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        stage = 0;
        moveSpeed = 0.2873;
        lastDist = 0;
        ticksSinceJump = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward == 0 && strafe == 0) {
            stage = 0;
            moveSpeed = 0.2873;
            return;
        }

        // strafe speed - stays within NCP limits by using vanilla bhop timing
        double xDist = mc.player.getX() - mc.player.prevX;
        double zDist = mc.player.getZ() - mc.player.prevZ;
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

        ticksSinceJump++;

        if (mc.player.isOnGround()) {
            stage = 0;

            // NCP-safe jump + strafe
            if (forward != 0 || strafe != 0) {
                mc.player.jump();
                ticksSinceJump = 0;

                // apply speed boost only on ground, within limits
                // vanilla sprint speed is ~0.2873, we go up to ~0.35 which NCP usually allows
                moveSpeed = 0.34 + Math.random() * 0.02; // slight randomization
            }
        } else {
            stage++;

            // air friction matching vanilla expectations
            if (stage == 1) {
                // first air tick after jump - can be slightly faster
                moveSpeed = lastDist - 0.01 * (lastDist - 0.2873);
            } else {
                // decay toward vanilla air speed
                moveSpeed *= 0.98;
            }

            // floor to vanilla minimum
            if (moveSpeed < 0.2873) {
                moveSpeed = 0.2873;
            }

            // cap to avoid flags - NCP typically flags above ~0.38 sustained
            if (moveSpeed > 0.36) {
                moveSpeed = 0.36;
            }
        }

        // apply movement
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);

        double motionX = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
        double motionZ = Math.cos(rad) * forward + Math.sin(rad) * strafe;

        double magnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (magnitude > 0) {
            motionX = (motionX / magnitude) * moveSpeed;
            motionZ = (motionZ / magnitude) * moveSpeed;
        }

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(motionX, vel.y, motionZ);

        // auto sprint for speed consistency
        mc.player.setSprinting(true);
    }

    @Override
    protected void onDisable() {
        moveSpeed = 0.2873;
        stage = 0;
    }

    public void setSpeedMultiplier(double mult) { this.moveSpeed = 0.2873 * mult; }
}
