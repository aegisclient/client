package dev.aegis.client.module.premium;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class HypixelSpeed extends Module {

    // Watchdog thresholds - 0.281 horizontal speed cap for strafe
    private static final double MAX_SPEED = 0.281;
    private static final double BASE_SPEED = 0.2873;
    private static final double LOW_HOP_Y = 0.31; // lower jump = faster ground return
    private static final double GROUND_BOOST = 1.08; // small ground multiplier under Watchdog limit
    private static final double AIR_FRICTION = 0.9865;

    // state
    private int stage = 0;
    private double moveSpeed = BASE_SPEED;
    private double lastDist = 0;
    private int airTicks = 0;
    private int groundTicks = 0;
    private boolean wasOnGround = true;

    // anti-flag
    private int safetyTicks = 0; // ticks to slow down after potential flag
    private int totalTicks = 0;

    private final Random random = new Random();

    public HypixelSpeed() {
        super("HypixelSpeed", "Hypixel-safe speed with low-ground bhop under Watchdog thresholds", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        stage = 0;
        moveSpeed = BASE_SPEED;
        lastDist = 0;
        airTicks = 0;
        groundTicks = 0;
        wasOnGround = true;
        safetyTicks = 0;
        totalTicks = 0;
    }

    @Override
    protected void onDisable() {
        moveSpeed = BASE_SPEED;
        stage = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        totalTicks++;

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        // no input = reset
        if (forward == 0 && strafe == 0) {
            stage = 0;
            moveSpeed = BASE_SPEED;
            return;
        }

        // safety slowdown - if we might have been flagged, play it safe
        if (safetyTicks > 0) {
            safetyTicks--;
            moveSpeed = BASE_SPEED * 0.9;
            applyStrafe(forward, strafe, moveSpeed);
            mc.player.setSprinting(true);
            return;
        }

        // track last tick distance
        double xDist = mc.player.getX() - mc.player.prevX;
        double zDist = mc.player.getZ() - mc.player.prevZ;
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

        boolean onGround = mc.player.isOnGround();

        if (onGround) {
            groundTicks++;
            airTicks = 0;

            // low-ground bhop: reduced Y velocity for faster ground contact
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, LOW_HOP_Y, vel.z);

            // ground boost within Watchdog tolerance
            double boost = BASE_SPEED * GROUND_BOOST;
            // add tiny jitter for humanization
            boost += (random.nextDouble() - 0.5) * 0.005;

            // clamp to Watchdog speed cap
            moveSpeed = Math.min(boost, MAX_SPEED);

            // spoof ground packet at the apex for Watchdog's movement check
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(), true
            ));

            // periodic safety pause - every ~80 ticks, slow down for 2 ticks
            if (totalTicks % (75 + random.nextInt(10)) == 0) {
                safetyTicks = 2;
            }
        } else {
            airTicks++;
            groundTicks = 0;

            if (airTicks == 1) {
                // first air tick: maintain speed from ground
                moveSpeed = Math.min(lastDist, MAX_SPEED);
            } else {
                // vanilla air friction decay
                moveSpeed *= AIR_FRICTION;
            }

            moveSpeed = Math.max(moveSpeed, BASE_SPEED * 0.85);
            moveSpeed = Math.min(moveSpeed, MAX_SPEED);

            // if airborne too long, something is wrong - safety reset
            if (airTicks > 8) {
                safetyTicks = 5;
            }
        }

        wasOnGround = onGround;
        applyStrafe(forward, strafe, moveSpeed);
        mc.player.setSprinting(true);
    }

    private void applyStrafe(double forward, double strafe, double speed) {
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);

        double motionX = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
        double motionZ = Math.cos(rad) * forward + Math.sin(rad) * strafe;

        double magnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (magnitude > 0) {
            motionX = (motionX / magnitude) * speed;
            motionZ = (motionZ / magnitude) * speed;
        }

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(motionX, vel.y, motionZ);
    }
}
