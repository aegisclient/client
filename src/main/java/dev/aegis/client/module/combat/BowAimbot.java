package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class BowAimbot extends Module {

    private double aimRange = 80.0;
    private boolean targetPlayers = true;
    private boolean targetMobs = true;
    private boolean targetAnimals = false;
    private boolean predictMovement = true;

    // actual arrow physics from MC source
    private static final double BASE_ARROW_VELOCITY = 3.0;
    private static final double GRAVITY_PER_TICK = 0.05;
    private static final double AIR_DRAG = 0.99;
    private static final double WATER_DRAG = 0.6;

    private LivingEntity currentTarget = null;
    // store previous positions for velocity smoothing
    private Vec3d lastTargetPos = null;
    private Vec3d smoothedVelocity = Vec3d.ZERO;
    private int trackingTicks = 0;

    public BowAimbot() {
        super("BowAimbot", "Automatically aims your bow at the nearest entity", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        boolean holdingBow = mc.player.getMainHandStack().getItem() instanceof BowItem
                || mc.player.getMainHandStack().getItem() instanceof CrossbowItem;

        if (!holdingBow) {
            currentTarget = null;
            lastTargetPos = null;
            trackingTicks = 0;
            return;
        }

        boolean drawing = mc.player.isUsingItem();
        boolean crossbowCharged = mc.player.getMainHandStack().getItem() instanceof CrossbowItem
                && CrossbowItem.isCharged(mc.player.getMainHandStack());

        if (!drawing && !crossbowCharged) return;

        LivingEntity newTarget = findBestTarget();
        if (newTarget == null) {
            currentTarget = null;
            lastTargetPos = null;
            trackingTicks = 0;
            return;
        }

        // update velocity tracking
        if (newTarget == currentTarget && lastTargetPos != null) {
            Vec3d instantVelocity = newTarget.getPos().subtract(lastTargetPos);
            // exponential moving average for smoother prediction
            double alpha = 0.3;
            smoothedVelocity = new Vec3d(
                    smoothedVelocity.x * (1 - alpha) + instantVelocity.x * alpha,
                    smoothedVelocity.y * (1 - alpha) + instantVelocity.y * alpha,
                    smoothedVelocity.z * (1 - alpha) + instantVelocity.z * alpha
            );
            trackingTicks++;
        } else {
            smoothedVelocity = newTarget.getVelocity();
            trackingTicks = 0;
        }

        currentTarget = newTarget;
        lastTargetPos = newTarget.getPos();

        float chargeProgress = getChargeProgress();
        float[] angles = calculateAimAngles(currentTarget, chargeProgress);

        if (angles != null) {
            float currentYaw = mc.player.getYaw();
            float currentPitch = mc.player.getPitch();

            float yawDiff = MathHelper.wrapDegrees(angles[0] - currentYaw);
            float pitchDiff = angles[1] - currentPitch;

            // faster rotation when far off, precise when close
            float distance = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
            float smoothFactor = distance > 10 ? 0.8f : distance > 3 ? 0.5f : 0.35f;

            mc.player.setYaw(currentYaw + yawDiff * smoothFactor);
            mc.player.setPitch(MathHelper.clamp(currentPitch + pitchDiff * smoothFactor, -90f, 90f));
        }
    }

    private LivingEntity findBestTarget() {
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (!living.isAlive()) continue;
            if (living.isInvisible()) continue;

            double dist = mc.player.distanceTo(living);
            if (dist > aimRange || dist < 3) continue;

            if (living instanceof PlayerEntity && !targetPlayers) continue;
            if (living instanceof Monster && !targetMobs) continue;
            if (living instanceof AnimalEntity && !targetAnimals) continue;

            // score by distance + angle offset (prefer targets we're already looking at)
            float[] lookAngles = getAngleTo(living);
            float yawDiff = Math.abs(MathHelper.wrapDegrees(lookAngles[0] - mc.player.getYaw()));
            float pitchDiff = Math.abs(lookAngles[1] - mc.player.getPitch());
            double anglePenalty = (yawDiff + pitchDiff) * 0.5;

            double score = dist * 0.7 + anglePenalty * 0.3;

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
        if (charge <= 0.1f) return null;

        Vec3d eyePos = mc.player.getEyePos();
        // aim at center mass, slightly above center for better hit chance
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.7, 0);

        double arrowVelocity = charge * BASE_ARROW_VELOCITY;

        // iterative prediction: simulate arrow, adjust, repeat
        Vec3d predictedPos = targetPos;

        for (int iteration = 0; iteration < 4; iteration++) {
            double dx = predictedPos.x - eyePos.x;
            double dz = predictedPos.z - eyePos.z;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);

            // estimate flight time via simulation
            double flightTicks = estimateFlightTime(horizontalDist, predictedPos.y - eyePos.y, arrowVelocity);

            if (flightTicks <= 0 || flightTicks > 200) return null;

            // predict target position at impact time
            if (predictMovement && trackingTicks >= 2) {
                Vec3d vel = smoothedVelocity;
                predictedPos = targetPos.add(
                        vel.x * flightTicks,
                        vel.y * flightTicks,
                        vel.z * flightTicks
                );
                // account for target gravity if they're airborne
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

        return new float[]{
                MathHelper.wrapDegrees(yaw),
                MathHelper.clamp(pitch, -90.0f, 90.0f)
        };
    }

    private double estimateFlightTime(double horizontalDist, double verticalDist, double velocity) {
        // binary search for the right pitch, then compute time from that
        // simplified: use horizontal velocity component estimate
        // at 45 degrees, horizontal component is velocity * cos(45) * 20 tps
        double avgHorizontalSpeed = velocity * 0.85 * AIR_DRAG; // rough average accounting for drag
        if (avgHorizontalSpeed <= 0) return -1;

        // each tick the arrow moves roughly this much horizontally
        // account for drag reducing speed over time
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
        // simulate arrow trajectory at many angles, find the one that hits closest
        double bestPitch = 0;
        double bestError = Double.MAX_VALUE;

        // coarse pass
        for (double testPitch = -45; testPitch <= 90; testPitch += 1.0) {
            double error = simulateAndGetError(testPitch, horizontalDist, verticalDist, velocity);
            if (error < bestError) {
                bestError = error;
                bestPitch = testPitch;
            }
        }

        // fine pass around best coarse result
        double fineStart = bestPitch - 1.5;
        double fineEnd = bestPitch + 1.5;
        for (double testPitch = fineStart; testPitch <= fineEnd; testPitch += 0.05) {
            double error = simulateAndGetError(testPitch, horizontalDist, verticalDist, velocity);
            if (error < bestError) {
                bestError = error;
                bestPitch = testPitch;
            }
        }

        // ultra-fine pass
        fineStart = bestPitch - 0.1;
        fineEnd = bestPitch + 0.1;
        for (double testPitch = fineStart; testPitch <= fineEnd; testPitch += 0.005) {
            double error = simulateAndGetError(testPitch, horizontalDist, verticalDist, velocity);
            if (error < bestError) {
                bestError = error;
                bestPitch = testPitch;
            }
        }

        return (float) bestPitch;
    }

    private double simulateAndGetError(double pitchDeg, double targetHDist, double targetVDist, double velocity) {
        double rad = Math.toRadians(pitchDeg);
        double vx = Math.cos(rad) * velocity;
        double vy = -Math.sin(rad) * velocity;

        double simX = 0;
        double simY = 0;

        for (int tick = 0; tick < 300; tick++) {
            simX += vx;
            simY += vy;
            vy += GRAVITY_PER_TICK;
            vx *= AIR_DRAG;
            vy *= AIR_DRAG;

            if (simX >= targetHDist) {
                // interpolate to exact horizontal distance
                double overshoot = simX - targetHDist;
                double ratio = overshoot / (vx / AIR_DRAG); // approximate
                double adjustedY = simY - (vy / AIR_DRAG) * ratio;
                return Math.abs(adjustedY - targetVDist);
            }

            // arrow fell below target by too much, no solution at this angle
            if (simY > targetVDist + 50) return Double.MAX_VALUE;
        }

        return Double.MAX_VALUE;
    }

    @Override
    protected void onDisable() {
        currentTarget = null;
        lastTargetPos = null;
        smoothedVelocity = Vec3d.ZERO;
        trackingTicks = 0;
    }
}
