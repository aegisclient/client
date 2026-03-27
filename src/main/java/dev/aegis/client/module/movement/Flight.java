package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Flight extends Module {

    private double flySpeed = 0.48;
    private int tickCounter = 0;
    private double lastY;

    public Flight() {
        super("Flight", "Allows you to fly in survival mode", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            lastY = mc.player.getY();
            tickCounter = 0;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        tickCounter++;

        float yaw = mc.player.getYaw();
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double motionX = 0, motionY = 0, motionZ = 0;

        // vertical movement
        if (mc.options.jumpKey.isPressed()) {
            motionY = 0.062;
        } else if (mc.options.sneakKey.isPressed()) {
            motionY = -0.062;
        } else {
            // glide mode: small downward to look legit, reset with micro-jump every 4 ticks
            if (tickCounter % 4 == 0) {
                motionY = 0.04;
            } else {
                motionY = -0.04;
            }
        }

        // horizontal movement within sprint speed limits
        if (forward != 0 || strafe != 0) {
            double rad = Math.toRadians(yaw);
            double mx = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
            double mz = Math.cos(rad) * forward + Math.sin(rad) * strafe;

            double magnitude = Math.sqrt(mx * mx + mz * mz);
            if (magnitude > 0) {
                mx = (mx / magnitude) * flySpeed;
                mz = (mz / magnitude) * flySpeed;
            }

            motionX = mx;
            motionZ = mz;
        }

        mc.player.setVelocity(motionX, motionY, motionZ);

        // spoof ground packets every few ticks to prevent kick
        if (tickCounter % 10 == 0) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y - 0.04, z, true)
            );
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().allowFlying = mc.player.isCreative();
            mc.player.setVelocity(mc.player.getVelocity().x, -0.04, mc.player.getVelocity().z);
        }
        tickCounter = 0;
    }

    public void setFlySpeed(double speed) { this.flySpeed = speed; }
}
