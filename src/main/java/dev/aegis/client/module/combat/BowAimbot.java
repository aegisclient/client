package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.PlayerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class BowAimbot extends Module {

    private double aimRange = 64.0;
    private boolean targetPlayers = true;
    private boolean targetMobs = true;
    private boolean targetAnimals = false;
    private boolean predictMovement = true;

    // arrow physics constants
    private static final double ARROW_SPEED = 3.0;
    private static final double GRAVITY = 0.05;
    private static final double DRAG = 0.99;

    private LivingEntity currentTarget = null;

    public BowAimbot() {
        super("BowAimbot", "Automatically aims your bow at the nearest entity", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // only activate when holding a bow or crossbow and using it
        boolean holdingBow = mc.player.getMainHandStack().getItem() instanceof BowItem
                || mc.player.getMainHandStack().getItem() instanceof CrossbowItem;

        if (!holdingBow) {
            currentTarget = null;
            return;
        }

        // only aim while drawing the bow
        if (!mc.player.isUsingItem() && !(mc.player.getMainHandStack().getItem() instanceof CrossbowItem
                && CrossbowItem.isCharged(mc.player.getMainHandStack()))) {
            return;
        }

        currentTarget = findBestTarget();
        if (currentTarget == null) return;

        float chargeProgress = getChargeProgress();
        float[] angles = calculateAimAngles(currentTarget, chargeProgress);

        if (angles != null) {
            // smooth rotation so it doesn't snap instantly
            float currentYaw = mc.player.getYaw();
            float currentPitch = mc.player.getPitch();

            float yawDiff = MathHelper.wrapDegrees(angles[0] - currentYaw);
            float pitchDiff = angles[1] - currentPitch;

            float smoothFactor = 0.6f;
            mc.player.setYaw(currentYaw + yawDiff * smoothFactor);
            mc.player.setPitch(currentPitch + pitchDiff * smoothFactor);
        }
    }

    private LivingEntity findBestTarget() {
        LivingEntity best = null;
        double bestDist = aimRange;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (!living.isAlive()) continue;

            double dist = mc.player.distanceTo(living);
            if (dist > aimRange) continue;

            // filter by entity type
            if (living instanceof PlayerEntity && !targetPlayers) continue;
            if (living instanceof Monster && !targetMobs) continue;
            if (living instanceof AnimalEntity && !targetAnimals) continue;

            // prefer closer targets
            if (dist < bestDist) {
                bestDist = dist;
                best = living;
            }
        }

        return best;
    }

    private float getChargeProgress() {
        if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem) {
            return CrossbowItem.isCharged(mc.player.getMainHandStack()) ? 1.0f : 0.0f;
        }

        int useTime = mc.player.getItemUseTime();
        float charge = BowItem.getPullProgress(useTime);
        return MathHelper.clamp(charge, 0.0f, 1.0f);
    }

    private float[] calculateAimAngles(LivingEntity target, float charge) {
        if (charge <= 0.0f) return null;

        Vec3d playerPos = mc.player.getEyePos();
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.85, 0);

        // predict where the target will be when the arrow arrives
        if (predictMovement) {
            Vec3d targetVel = target.getVelocity();
            double dist = playerPos.distanceTo(targetPos);

            // estimate flight time based on distance and arrow speed
            double arrowVelocity = charge * ARROW_SPEED;
            if (arrowVelocity <= 0) return null;

            double flightTime = dist / (arrowVelocity * 20.0);

            // lead the target
            targetPos = targetPos.add(
                    targetVel.x * flightTime * 20.0,
                    targetVel.y * flightTime * 20.0,
                    targetVel.z * flightTime * 20.0
            );
        }

        double dx = targetPos.x - playerPos.x;
        double dy = targetPos.y - playerPos.y;
        double dz = targetPos.z - playerPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // calculate yaw
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;

        // calculate pitch with gravity compensation
        float pitch = calculatePitchForProjectile(horizontalDist, dy, charge);

        return new float[]{
                MathHelper.wrapDegrees(yaw),
                MathHelper.clamp(pitch, -90.0f, 90.0f)
        };
    }

    private float calculatePitchForProjectile(double horizontalDist, double verticalDist, float charge) {
        double velocity = charge * ARROW_SPEED;

        // use iterative approach to find the right pitch
        // simulates the arrow trajectory to find the angle that hits the target
        double bestPitch = 0;
        double bestError = Double.MAX_VALUE;

        for (double testPitch = -90; testPitch <= 90; testPitch += 0.5) {
            double rad = Math.toRadians(testPitch);
            double vx = Math.cos(rad) * velocity;
            double vy = -Math.sin(rad) * velocity;

            // simulate the arrow flight
            double simX = 0;
            double simY = 0;
            double simVx = vx;
            double simVy = vy;

            for (int tick = 0; tick < 200; tick++) {
                simX += simVx;
                simY += simVy;
                simVy += GRAVITY;
                simVx *= DRAG;
                simVy *= DRAG;

                // check if we've passed the horizontal distance
                if (simX >= horizontalDist) {
                    double error = Math.abs(simY - verticalDist);
                    if (error < bestError) {
                        bestError = error;
                        bestPitch = testPitch;
                    }
                    break;
                }
            }
        }

        return (float) bestPitch;
    }

    @Override
    protected void onDisable() {
        currentTarget = null;
    }
}
