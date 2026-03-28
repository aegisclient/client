package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class Flight extends Module {

    public enum Mode { VANILLA, PACKET, GLIDE }

    private Mode mode = Mode.VANILLA;
    private double flySpeed = 2.0;
    private int tickCounter = 0;
    private final Random random = new Random();
    private boolean wasFlying = false;
    private boolean wasAllowFlying = false;

    public Flight() {
        super("Flight", "Keybind: F | Creative-like flight in survival", Category.MOVEMENT, GLFW.GLFW_KEY_F);
    }

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            wasFlying = mc.player.getAbilities().flying;
            wasAllowFlying = mc.player.getAbilities().allowFlying;
            tickCounter = 0;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        tickCounter++;

        switch (mode) {
            case VANILLA -> doVanilla();
            case PACKET -> doPacket();
            case GLIDE -> doGlide();
        }
    }

    private void doVanilla() {
        // Enable creative-like flight
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed((float) (flySpeed * 0.05f));

        // Anti-kick: small downward jitter every 40 ticks
        if (tickCounter % 40 == 0) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            mc.player.getX(), mc.player.getY() - 0.04, mc.player.getZ(), false
                    )
            );
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            mc.player.getX(), mc.player.getY(), mc.player.getZ(), true
                    )
            );
        }
    }

    private void doPacket() {
        float yaw = mc.player.getYaw();
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double motionX = 0, motionY = 0, motionZ = 0;

        // Vertical
        if (mc.options.jumpKey.isPressed()) {
            motionY = 0.5;
        } else if (mc.options.sneakKey.isPressed()) {
            motionY = -0.5;
        } else {
            // Small oscillation for anti-detection
            motionY = Math.sin(tickCounter * 0.7) * 0.03;
        }

        // Horizontal
        if (forward != 0 || strafe != 0) {
            double rad = Math.toRadians(yaw);
            double mx = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
            double mz = Math.cos(rad) * forward + Math.sin(rad) * strafe;

            double magnitude = Math.sqrt(mx * mx + mz * mz);
            if (magnitude > 0) {
                double speed = flySpeed + (random.nextDouble() - 0.5) * 0.05;
                mx = (mx / magnitude) * speed;
                mz = (mz / magnitude) * speed;
            }

            motionX = mx;
            motionZ = mz;
        }

        mc.player.setVelocity(motionX, motionY, motionZ);

        // Ground spoof every 8 ticks
        if (tickCounter % 8 == 0) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y - 0.04, z, true)
            );
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false)
            );
        }

        // Anti-kick every 40 ticks
        if (tickCounter % 40 == 0) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y - 0.08, z, false)
            );
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true)
            );
        }
    }

    private void doGlide() {
        Vec3d vel = mc.player.getVelocity();
        float yaw = mc.player.getYaw();
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double targetY;
        if (mc.options.jumpKey.isPressed()) {
            targetY = 0.1;
        } else if (mc.options.sneakKey.isPressed()) {
            targetY = -0.3;
        } else {
            targetY = -0.005;
        }

        double motionX = vel.x;
        double motionZ = vel.z;

        if (forward != 0 || strafe != 0) {
            double rad = Math.toRadians(yaw);
            double mx = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
            double mz = Math.cos(rad) * forward + Math.sin(rad) * strafe;
            double magnitude = Math.sqrt(mx * mx + mz * mz);
            if (magnitude > 0) {
                double speed = flySpeed * 0.3;
                mx = (mx / magnitude) * speed;
                mz = (mz / magnitude) * speed;
            }
            motionX = motionX * 0.6 + mx * 0.4;
            motionZ = motionZ * 0.6 + mz * 0.4;
        } else {
            motionX *= 0.85;
            motionZ *= 0.85;
        }

        mc.player.setVelocity(motionX, targetY, motionZ);
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = wasFlying;
            mc.player.getAbilities().allowFlying = wasAllowFlying || mc.player.getAbilities().creativeMode;
            mc.player.getAbilities().setFlySpeed(0.05f);
            mc.player.setVelocity(mc.player.getVelocity().x * 0.3, -0.04, mc.player.getVelocity().z * 0.3);
        }
        tickCounter = 0;
    }

    public void setMode(Mode mode) { this.mode = mode; }
    public Mode getMode() { return mode; }
    public void setFlySpeed(double speed) { this.flySpeed = speed; }
}
