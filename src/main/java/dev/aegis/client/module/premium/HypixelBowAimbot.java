package dev.aegis.client.module.premium;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * Hypixel-safe BowAimbot.
 *
 * Watchdog detects bow aimbot via:
 * 1. Instant rotation snaps (rotation speed too high in one tick)
 * 2. Perfect aim consistency (0 deviation across shots)
 * 3. Inhuman rotation patterns (constant angular velocity, no acceleration/deceleration)
 * 4. Aiming at targets outside FOV (>120 degree rotation in one draw)
 * 5. Consistent aim at exact center of hitbox (humans vary)
 *
 * This module counters all 5:
 * - Rotation uses cubic bezier interpolation (acceleration + deceleration like a real mouse movement)
 * - Max rotation speed capped at 35 deg/tick (human flick speed)
 * - Random aim offset ±0.5 degrees per shot (variance like real aim)
 * - Target body aim point varies between 55-75% height (not always center mass)
 * - Only engages targets within ±90 degrees of current look direction
 * - Skips ticks randomly (doesn't adjust every single tick)
 * - Adds micro-jitter that mimics hand tremor during draw
 */
public class HypixelBowAimbot extends Module {

    private static final double AIM_RANGE = 60.0;
    private static final float MAX_ROTATION_SPEED = 35.0f;  // deg/tick cap
    private static final float HUMAN_JITTER_AMPLITUDE = 0.3f; // degrees of hand tremor
    private static final double AIM_OFFSET_RANGE = 0.5; // degrees of intentional inaccuracy
    private static final int MIN_TRACKING_TICKS = 3; // wait before prediction

    // arrow physics
    private static final double BASE_ARROW_VELOCITY = 3.0;
    private static final double GRAVITY_PER_TICK = 0.05;
    private static final double AIR_DRAG = 0.99;

    private final Random random = new Random();

    // target tracking state
    private LivingEntity currentTarget = null;
    private Vec3d lastTargetPos = null;
    private Vec3d smoothedVelocity = Vec3d.ZERO;
    private int trackingTicks = 0;

    // rotation interpolation state
    private float targetYaw, targetPitch;
    private float interpProgress = 0f; // 0..1 bezier progress
    private float interpStartYaw, interpStartPitch;
    private float interpDuration; // ticks to complete rotation
    private boolean interpolating = false;

    // per-shot randomization
    private float shotAimOffsetYaw = 0f;
    private float shotAimOffsetPitch = 0f;
    private float shotBodyHeightPercent = 0.65f;

    // tick skip for humanization
    private int skipCounter = 0;

    public HypixelBowAimbot() {
        super("HypixelBowAimbot", "Watchdog-safe bow aimbot with human-like rotation", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        resetState();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        boolean holdingBow = mc.player.getMainHandStack().getItem() instanceof BowItem
                || mc.player.getMainHandStack().getItem() instanceof CrossbowItem;

        if (!holdingBow) {
            resetState();
            return;
        }

        boolean drawing = mc.player.isUsingItem();
        boolean crossbowCharged = mc.player.getMainHandStack().getItem() instanceof CrossbowItem
                && CrossbowItem.isCharged(mc.player.getMainHandStack());

        if (!drawing && !crossbowCharged) {
            // Reset per-shot randomization when not drawing
            if (currentTarget != null) {
                randomizeShotParams();
            }
            return;
        }

        // Random tick skip (skip ~15% of ticks to look human)
        skipCounter++;
        if (random.nextFloat() < 0.15f && skipCounter > 2) {
            skipCounter = 0;
            return;
        }

        LivingEntity newTarget = findBestTarget();
        if (newTarget == null) {
            resetState();
            return;
        }

        // Update velocity tracking
        if (newTarget == currentTarget && lastTargetPos != null) {
            Vec3d instantVel = newTarget.getPos().subtract(lastTargetPos);
            double alpha = 0.35;
            smoothedVelocity = new Vec3d(
                    smoothedVelocity.x * (1 - alpha) + instantVel.x * alpha,
                    smoothedVelocity.y * (1 - alpha) + instantVel.y * alpha,
                    smoothedVelocity.z * (1 - alpha) + instantVel.z * alpha
            );
            trackingTicks++;
        } else {
            smoothedVelocity = newTarget.getVelocity();
            trackingTicks = 0;
            randomizeShotParams();
        }

        currentTarget = newTarget;
        lastTargetPos = newTarget.getPos();

        float chargeProgress = getChargeProgress();
        float[] idealAngles = calculateAimAngles(currentTarget, chargeProgress);
        if (idealAngles == null) return;

        // Apply per-shot aim offset (human inaccuracy)
        idealAngles[0] += shotAimOffsetYaw;
        idealAngles[1] += shotAimOffsetPitch;

        // Add micro-jitter (hand tremor during bow draw)
        float tremor = HUMAN_JITTER_AMPLITUDE * (float) Math.sin(System.currentTimeMillis() * 0.008);
        float tremorY = HUMAN_JITTER_AMPLITUDE * (float) Math.cos(System.currentTimeMillis() * 0.006 + 1.3);
        idealAngles[0] += tremor;
        idealAngles[1] += tremorY;

        // Calculate rotation delta
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float yawDiff = MathHelper.wrapDegrees(idealAngles[0] - currentYaw);
        float pitchDiff = idealAngles[1] - currentPitch;
        float totalDelta = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

        // Bezier-interpolated rotation (human-like acceleration/deceleration)
        // Start new interpolation if target moved significantly
        if (!interpolating || totalDelta > 8.0f) {
            interpStartYaw = currentYaw;
            interpStartPitch = currentPitch;
            targetYaw = idealAngles[0];
            targetPitch = idealAngles[1];

            // Duration based on distance: larger rotations take more ticks
            interpDuration = Math.max(2, totalDelta / MAX_ROTATION_SPEED);
            // Add randomness to duration
            interpDuration += random.nextFloat() * 2.0f;
            interpProgress = 0f;
            interpolating = true;
        } else {
            // Update target for smooth tracking
            targetYaw = idealAngles[0];
            targetPitch = idealAngles[1];
        }

        if (interpolating) {
            interpProgress += 1.0f / interpDuration;
            interpProgress = Math.min(interpProgress, 1.0f);

            // Cubic bezier ease-in-out: smooth acceleration and deceleration
            float t = interpProgress;
            float easedT = cubicBezierEaseInOut(t);

            float newYaw = interpStartYaw + MathHelper.wrapDegrees(targetYaw - interpStartYaw) * easedT;
            float newPitch = interpStartPitch + (targetPitch - interpStartPitch) * easedT;

            // Clamp rotation speed per tick
            float actualYawDiff = MathHelper.wrapDegrees(newYaw - currentYaw);
            float actualPitchDiff = newPitch - currentPitch;
            float actualSpeed = (float) Math.sqrt(actualYawDiff * actualYawDiff + actualPitchDiff * actualPitchDiff);

            if (actualSpeed > MAX_ROTATION_SPEED) {
                float scale = MAX_ROTATION_SPEED / actualSpeed;
                actualYawDiff *= scale;
                actualPitchDiff *= scale;
            }

            mc.player.setYaw(currentYaw + actualYawDiff);
            mc.player.setPitch(MathHelper.clamp(currentPitch + actualPitchDiff, -90f, 90f));

            if (interpProgress >= 1.0f) {
                interpolating = false;
            }
        }
    }

    /**
     * Cubic bezier ease-in-out: mimics real mouse acceleration curve
     * Slow start -> fast middle -> slow end (like a human flick)
     */
    private float cubicBezierEaseInOut(float t) {
        if (t < 0.5f) {
            return 4 * t * t * t;
        } else {
            float f = (2 * t - 2);
            return 0.5f * f * f * f + 1;
        }
    }

    private void randomizeShotParams() {
        // Each shot gets different random offsets
        shotAimOffsetYaw = (float) (random.nextGaussian() * AIM_OFFSET_RANGE);
        shotAimOffsetPitch = (float) (random.nextGaussian() * AIM_OFFSET_RANGE * 0.7);
        // Vary aim height on body (55-75% of entity height)
        shotBodyHeightPercent = 0.55f + random.nextFloat() * 0.20f;
    }

    private LivingEntity findBestTarget() {
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        Box searchBox = mc.player.getBoundingBox().expand(AIM_RANGE);
        for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox)) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!living.isAlive()) continue;
            if (living instanceof ArmorStandEntity) continue;

            double dist = mc.player.distanceTo(living);
            if (dist > AIM_RANGE || dist < 2) continue;

            // Only target players on Hypixel
            if (!(living instanceof PlayerEntity player)) continue;
            if (Aegis.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) continue;

            // FOV check: don't engage targets behind us (Watchdog flags >90 degree instant rotations)
            float[] lookAngles = getAngleTo(living);
            float yawDiff = Math.abs(MathHelper.wrapDegrees(lookAngles[0] - mc.player.getYaw()));
            if (yawDiff > 90) continue;

            float pitchDiff = Math.abs(lookAngles[1] - mc.player.getPitch());
            double anglePenalty = (yawDiff + pitchDiff) * 0.5;
            double hpFactor = living.getHealth() / living.getMaxHealth();
            double score = dist * 0.4 + anglePenalty * 0.4 + hpFactor * 8;

            if (score < bestScore) {
                bestScore = score;
                best = living;
            }
        }

        return best;
    }

    private float[] getAngleTo(Entity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = target.getEyeY() - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, hDist));
        return new float[]{yaw, pitch};
    }

    private float getChargeProgress() {
        if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem) {
            return CrossbowItem.isCharged(mc.player.getMainHandStack()) ? 1.0f : 0.0f;
        }
        int useTime = mc.player.getItemUseTime();
        return MathHelper.clamp(BowItem.getPullProgress(useTime), 0.0f, 1.0f);
    }

    private float[] calculateAimAngles(LivingEntity target, float charge) {
        if (charge <= 0.15f) return null;

        Vec3d eyePos = mc.player.getEyePos();
        // Variable aim point (not always center mass)
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * shotBodyHeightPercent, 0);

        double arrowVelocity = charge * BASE_ARROW_VELOCITY;

        Vec3d predictedPos = targetPos;

        // Iterative prediction
        for (int iteration = 0; iteration < 4; iteration++) {
            double dx = predictedPos.x - eyePos.x;
            double dz = predictedPos.z - eyePos.z;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);

            double flightTicks = estimateFlightTime(horizontalDist, predictedPos.y - eyePos.y, arrowVelocity);
            if (flightTicks <= 0 || flightTicks > 150) return null;

            if (trackingTicks >= MIN_TRACKING_TICKS) {
                Vec3d vel = smoothedVelocity;
                predictedPos = targetPos.add(
                        vel.x * flightTicks,
                        vel.y * flightTicks,
                        vel.z * flightTicks
                );

                if (!target.isOnGround()) {
                    double gravityDrop = 0.5 * 0.08 * flightTicks * flightTicks;
                    predictedPos = predictedPos.add(0, -gravityDrop, 0);
                }
            }
        }

        double dx = predictedPos.x - eyePos.x;
        double dy = predictedPos.y - eyePos.y;
        double dz = predictedPos.z - eyePos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = solveProjectilePitch(horizontalDist, dy, arrowVelocity);

        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0f, 90.0f)};
    }

    private double estimateFlightTime(double horizontalDist, double verticalDist, double velocity) {
        double avgHorizontalSpeed = velocity * 0.85 * AIR_DRAG;
        if (avgHorizontalSpeed <= 0) return -1;

        double totalDist = 0;
        double currentSpeed = avgHorizontalSpeed;
        int ticks = 0;

        while (totalDist < horizontalDist && ticks < 200) {
            totalDist += currentSpeed;
            currentSpeed *= AIR_DRAG;
            ticks++;
        }

        return ticks;
    }

    private float solveProjectilePitch(double horizontalDist, double verticalDist, double velocity) {
        double bestPitch = 0;
        double bestError = Double.MAX_VALUE;

        for (double testPitch = -45; testPitch <= 90; testPitch += 1.0) {
            double error = simulateError(testPitch, horizontalDist, verticalDist, velocity);
            if (error < bestError) { bestError = error; bestPitch = testPitch; }
        }

        for (double testPitch = bestPitch - 1.5; testPitch <= bestPitch + 1.5; testPitch += 0.05) {
            double error = simulateError(testPitch, horizontalDist, verticalDist, velocity);
            if (error < bestError) { bestError = error; bestPitch = testPitch; }
        }

        for (double testPitch = bestPitch - 0.1; testPitch <= bestPitch + 0.1; testPitch += 0.005) {
            double error = simulateError(testPitch, horizontalDist, verticalDist, velocity);
            if (error < bestError) { bestError = error; bestPitch = testPitch; }
        }

        return (float) bestPitch;
    }

    private double simulateError(double pitchDeg, double targetHDist, double targetVDist, double velocity) {
        double rad = Math.toRadians(pitchDeg);
        double vx = Math.cos(rad) * velocity;
        double vy = -Math.sin(rad) * velocity;
        double simX = 0, simY = 0;

        for (int tick = 0; tick < 300; tick++) {
            simX += vx;
            simY += vy;
            vy += GRAVITY_PER_TICK;
            vx *= AIR_DRAG;
            vy *= AIR_DRAG;

            if (simX >= targetHDist) {
                double overshoot = simX - targetHDist;
                double ratio = (vx / AIR_DRAG != 0) ? overshoot / (vx / AIR_DRAG) : 0;
                double adjustedY = simY - (vy / AIR_DRAG) * ratio;
                return Math.abs(adjustedY - targetVDist);
            }
            if (simY > targetVDist + 50) return Double.MAX_VALUE;
        }

        return Double.MAX_VALUE;
    }

    private void resetState() {
        currentTarget = null;
        lastTargetPos = null;
        smoothedVelocity = Vec3d.ZERO;
        trackingTicks = 0;
        interpolating = false;
        skipCounter = 0;
    }

    @Override
    protected void onDisable() {
        resetState();
    }

    public LivingEntity getCurrentTarget() { return currentTarget; }
}
