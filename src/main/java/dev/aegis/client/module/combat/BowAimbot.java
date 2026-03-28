package dev.aegis.client.module.combat;

import dev.aegis.client.Aegis;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class BowAimbot extends Module {

    private double aimRange = 80.0;
    private boolean targetPlayers = true;
    private boolean targetMobs = true;
    private boolean targetAnimals = false;
    private boolean predictMovement = true;
    private boolean accountForStrafe = true;

    // arrow physics from MC source
    private static final double BASE_ARROW_VELOCITY = 3.0;
    private static final double GRAVITY_PER_TICK = 0.05;
    private static final double AIR_DRAG = 0.99;

    private LivingEntity currentTarget = null;
    // velocity tracking with EMA
    private Vec3d lastTargetPos = null;
    private Vec3d smoothedVelocity = Vec3d.ZERO;
    private Vec3d lastSmoothedVelocity = Vec3d.ZERO;
    private int trackingTicks = 0;

    // position history for acceleration estimation
    private Vec3d[] posHistory = new Vec3d[5];
    private int historyIndex = 0;

    public BowAimbot() {
        super("BowAimbot", "3-pass iterative pitch solver with EMA velocity smoothing", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
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

        // update velocity tracking with exponential moving average
        if (newTarget == currentTarget && lastTargetPos != null) {
            Vec3d instantVelocity = newTarget.getPos().subtract(lastTargetPos);

            // adaptive alpha: higher when velocity changes direction (target is strafing)
            double alpha = 0.3;
            if (accountForStrafe) {
                double dot = smoothedVelocity.normalize().dotProduct(instantVelocity.normalize());
                if (dot < 0.7) alpha = 0.6; // direction changed, react faster
            }

            smoothedVelocity = new Vec3d(
                    smoothedVelocity.x * (1 - alpha) + instantVelocity.x * alpha,
                    smoothedVelocity.y * (1 - alpha) + instantVelocity.y * alpha,
                    smoothedVelocity.z * (1 - alpha) + instantVelocity.z * alpha
            );

            // store acceleration
            lastSmoothedVelocity = smoothedVelocity;

            // position history
            posHistory[historyIndex % posHistory.length] = newTarget.getPos();
            historyIndex++;

            trackingTicks++;
        } else {
            smoothedVelocity = newTarget.getVelocity();
            lastSmoothedVelocity = smoothedVelocity;
            trackingTicks = 0;
            historyIndex = 0;
            posHistory = new Vec3d[5];
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

            // adaptive smooth factor: snap quickly to target, fine-tune for precision
            float distance = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
            float smoothFactor;
            if (distance > 20) smoothFactor = 1.0f;
            else if (distance > 10) smoothFactor = 0.9f;
            else if (distance > 3) smoothFactor = 0.75f;
            else smoothFactor = 0.5f;

            mc.player.setYaw(currentYaw + yawDiff * smoothFactor);
            mc.player.setPitch(MathHelper.clamp(currentPitch + pitchDiff * smoothFactor, -90f, 90f));
        }
    }

    private LivingEntity findBestTarget() {
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        Box searchBox = mc.player.getBoundingBox().expand(aimRange);
        for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox)) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!living.isAlive()) continue;

            double dist = mc.player.distanceTo(living);
            if (dist > aimRange) continue;

            if (living instanceof PlayerEntity player) {
                if (!targetPlayers) continue;
                if (Aegis.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) continue;
            }
            if (living instanceof Monster && !targetMobs) continue;
            if (living instanceof AnimalEntity && !targetAnimals) continue;

            // composite scoring: distance + angle offset + HP priority
            float[] lookAngles = getAngleTo(living);
            float yawDiff = Math.abs(MathHelper.wrapDegrees(lookAngles[0] - mc.player.getYaw()));
            float pitchDiff = Math.abs(lookAngles[1] - mc.player.getPitch());
            double anglePenalty = (yawDiff + pitchDiff) * 0.5;

            // prefer low HP targets
            double hpFactor = living.getHealth() / living.getMaxHealth();

            double score = dist * 0.5 + anglePenalty * 0.3 + hpFactor * 10;

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
        // aim at center mass
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.65, 0);

        double arrowVelocity = charge * BASE_ARROW_VELOCITY;

        // iterative prediction: simulate, predict, repeat
        Vec3d predictedPos = targetPos;

        for (int iteration = 0; iteration < 5; iteration++) {
            double dx = predictedPos.x - eyePos.x;
            double dz = predictedPos.z - eyePos.z;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);

            double flightTicks = estimateFlightTime(horizontalDist, predictedPos.y - eyePos.y, arrowVelocity);

            if (flightTicks <= 0 || flightTicks > 200) return null;

            // predict target position at impact
            if (predictMovement && trackingTicks >= 1) {
                Vec3d vel = smoothedVelocity;
                predictedPos = targetPos.add(
                        vel.x * flightTicks,
                        vel.y * flightTicks,
                        vel.z * flightTicks
                );

                // account for strafe changes with acceleration estimation
                if (accountForStrafe && historyIndex >= 3) {
                    Vec3d accel = estimateAcceleration();
                    if (accel != null) {
                        // add half-acceleration term (kinematic equation)
                        predictedPos = predictedPos.add(
                                accel.x * 0.5 * flightTicks * flightTicks,
                                0,
                                accel.z * 0.5 * flightTicks * flightTicks
                        );
                    }
                }

                // gravity for airborne targets
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

    private Vec3d estimateAcceleration() {
        // estimate acceleration from position history
        int count = Math.min(historyIndex, posHistory.length);
        if (count < 3) return null;

        // use last 3 positions
        int i2 = (historyIndex - 1) % posHistory.length;
        int i1 = (historyIndex - 2) % posHistory.length;
        int i0 = (historyIndex - 3) % posHistory.length;

        if (posHistory[i0] == null || posHistory[i1] == null || posHistory[i2] == null) return null;

        Vec3d v1 = posHistory[i1].subtract(posHistory[i0]);
        Vec3d v2 = posHistory[i2].subtract(posHistory[i1]);

        return v2.subtract(v1); // acceleration = delta velocity
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

        // coarse pass
        for (double testPitch = -45; testPitch <= 90; testPitch += 1.0) {
            double error = simulateAndGetError(testPitch, horizontalDist, verticalDist, velocity);
            if (error < bestError) {
                bestError = error;
                bestPitch = testPitch;
            }
        }

        // fine pass
        for (double testPitch = bestPitch - 1.5; testPitch <= bestPitch + 1.5; testPitch += 0.05) {
            double error = simulateAndGetError(testPitch, horizontalDist, verticalDist, velocity);
            if (error < bestError) {
                bestError = error;
                bestPitch = testPitch;
            }
        }

        // ultra-fine pass
        for (double testPitch = bestPitch - 0.1; testPitch <= bestPitch + 0.1; testPitch += 0.003) {
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
                double overshoot = simX - targetHDist;
                double ratio = (vx / AIR_DRAG != 0) ? overshoot / (vx / AIR_DRAG) : 0;
                double adjustedY = simY - (vy / AIR_DRAG) * ratio;
                return Math.abs(adjustedY - targetVDist);
            }

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

    public LivingEntity getCurrentTarget() { return currentTarget; }
}
