package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class Speed extends Module {

    public enum Mode { STRAFE, BHOP, LOW_HOP, GROUND, VANILLA }
    private Mode mode = Mode.STRAFE;

    // strafe bhop state
    private int stage = 0;
    private double moveSpeed = 0.2873;
    private double lastDist = 0;
    private int airTicks = 0;

    // NCP limits
    private double maxGroundSpeed = 0.36;
    private double maxAirSpeed = 0.36;
    private double baseSpeed = 0.2873;
    private double jumpBoost = 0.06;           // extra speed on jump
    private double groundSpeedMultiplier = 1.18;

    // gradual acceleration
    private boolean gradualAcceleration = true;
    private double accelerationRate = 0.012;   // speed increase per tick while accelerating
    private double decelerationRate = 0.008;   // speed decrease per tick while decelerating
    private double currentAccelSpeed = 0.0;    // current accumulated acceleration
    private int groundTicks = 0;               // ticks on ground for acceleration tracking

    // low-hop
    private double lowHopMotion = 0.31;        // lower jump = faster ground return

    // vanilla mode settings
    private double vanillaSpeedMultiplier = 1.3; // conservative multiplier for vanilla mode
    private boolean vanillaJump = true;          // whether vanilla mode uses jumps

    // anti-cheat
    private boolean damageBoost = true;        // use damage speed boost when available
    private boolean timerBoost = false;        // slight timer increase (risky)
    private boolean ncpSafe = true;            // enforce NCP-safe speed limits
    private boolean potionCheck = true;        // account for speed potion effects
    private final Random random = new Random();

    // speed cap enforcement
    private int violations = 0;               // track potential violations
    private double lastMoveSpeed = 0;

    public Speed() {
        super("Speed", "NCP-safe strafe bhop within vanilla limits", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        stage = 0;
        moveSpeed = baseSpeed;
        lastDist = 0;
        airTicks = 0;
        groundTicks = 0;
        currentAccelSpeed = 0.0;
        violations = 0;
        lastMoveSpeed = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward == 0 && strafe == 0) {
            stage = 0;
            moveSpeed = baseSpeed;
            currentAccelSpeed = 0.0;
            groundTicks = 0;
            return;
        }

        // track distance
        double xDist = mc.player.getX() - mc.player.prevX;
        double zDist = mc.player.getZ() - mc.player.prevZ;
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

        // get effective max speed (accounting for potions)
        double effectiveMaxGround = getEffectiveMaxSpeed(maxGroundSpeed);
        double effectiveMaxAir = getEffectiveMaxSpeed(maxAirSpeed);

        switch (mode) {
            case STRAFE -> doStrafe(forward, strafe, effectiveMaxGround, effectiveMaxAir);
            case BHOP -> doBhop(forward, strafe, effectiveMaxGround, effectiveMaxAir);
            case LOW_HOP -> doLowHop(forward, strafe, effectiveMaxGround, effectiveMaxAir);
            case GROUND -> doGround(forward, strafe, effectiveMaxGround);
            case VANILLA -> doVanilla(forward, strafe, effectiveMaxGround);
        }

        mc.player.setSprinting(true);

        // NCP safety: check if we're moving too fast and throttle
        if (ncpSafe) {
            double actualSpeed = Math.sqrt(
                    mc.player.getVelocity().x * mc.player.getVelocity().x +
                    mc.player.getVelocity().z * mc.player.getVelocity().z
            );
            double safeLimit = mc.player.isOnGround() ? effectiveMaxGround : effectiveMaxAir;
            if (actualSpeed > safeLimit * 1.05) {
                // we're over limit, scale back
                violations++;
                if (violations > 3) {
                    // throttle hard
                    Vec3d vel = mc.player.getVelocity();
                    double scale = (safeLimit * 0.95) / actualSpeed;
                    mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
                    violations = 0;
                }
            } else {
                violations = Math.max(0, violations - 1);
            }
        }
    }

    private double getEffectiveMaxSpeed(double baseMax) {
        if (!potionCheck || mc.player == null) return baseMax;

        double multiplier = 1.0;
        // speed potion: each level adds ~20% speed
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            multiplier += 0.2 * (amplifier + 1);
        }
        // slowness: each level reduces by ~15%
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            multiplier -= 0.15 * (amplifier + 1);
        }
        return baseMax * Math.max(multiplier, 0.1);
    }

    private double applyGradualAcceleration(double targetSpeed) {
        if (!gradualAcceleration) return targetSpeed;

        if (targetSpeed > currentAccelSpeed) {
            currentAccelSpeed += accelerationRate;
            currentAccelSpeed = Math.min(currentAccelSpeed, targetSpeed);
        } else {
            currentAccelSpeed -= decelerationRate;
            currentAccelSpeed = Math.max(currentAccelSpeed, targetSpeed * 0.8);
        }
        return currentAccelSpeed;
    }

    private void doStrafe(double forward, double strafe, double maxGround, double maxAir) {
        if (mc.player.isOnGround()) {
            stage = 0;
            airTicks = 0;
            groundTicks++;

            mc.player.jump();

            // post-jump speed boost within NCP tolerance, with gradual acceleration
            double boost = baseSpeed * groundSpeedMultiplier + jumpBoost;
            double target = Math.min(boost, maxGround) + random.nextDouble() * 0.008;
            moveSpeed = applyGradualAcceleration(target);
        } else {
            stage++;
            airTicks++;
            groundTicks = 0;

            if (stage == 1) {
                // first air tick: slight speed increase allowed
                moveSpeed = Math.max(lastDist - 0.005 * (lastDist - baseSpeed), baseSpeed);
            } else {
                // vanilla air friction
                moveSpeed *= 0.9865;
            }

            moveSpeed = Math.max(moveSpeed, baseSpeed);
            moveSpeed = Math.min(moveSpeed, maxAir);
        }

        applyStrafe(forward, strafe, moveSpeed);
    }

    private void doBhop(double forward, double strafe, double maxGround, double maxAir) {
        if (mc.player.isOnGround()) {
            mc.player.jump();
            airTicks = 0;
            groundTicks++;

            // initial bhop speed with gradual ramp
            double target = baseSpeed * groundSpeedMultiplier + random.nextDouble() * 0.015;
            target = Math.min(target, maxGround);
            moveSpeed = applyGradualAcceleration(target);
        } else {
            airTicks++;
            groundTicks = 0;
            // gradual decay matching NCP expectations
            if (airTicks <= 2) {
                moveSpeed = lastDist * 0.985;
            } else {
                moveSpeed *= 0.978;
            }
            moveSpeed = Math.max(moveSpeed, baseSpeed);
        }

        applyStrafe(forward, strafe, moveSpeed);
    }

    private void doLowHop(double forward, double strafe, double maxGround, double maxAir) {
        if (mc.player.isOnGround()) {
            // low hop: reduced Y velocity for faster ground contact
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, lowHopMotion, vel.z);
            airTicks = 0;
            groundTicks++;

            double target = baseSpeed * groundSpeedMultiplier + random.nextDouble() * 0.01;
            target = Math.min(target, maxGround);
            moveSpeed = applyGradualAcceleration(target);

            // spoof ground packet at apex for NCP
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(), true
            ));
        } else {
            airTicks++;
            groundTicks = 0;
            moveSpeed *= 0.99;
            moveSpeed = Math.max(moveSpeed, baseSpeed);
        }

        applyStrafe(forward, strafe, moveSpeed);
    }

    private void doGround(double forward, double strafe, double maxGround) {
        // pure ground speed - no jumping, uses packet manipulation
        if (!mc.player.isOnGround()) {
            moveSpeed = baseSpeed;
            applyStrafe(forward, strafe, moveSpeed);
            return;
        }

        groundTicks++;

        // ground speed boost via sprint + movement optimization with gradual ramp
        double target = baseSpeed * groundSpeedMultiplier + random.nextDouble() * 0.008;
        target = Math.min(target, 0.33); // conservative ground limit
        moveSpeed = applyGradualAcceleration(target);

        applyStrafe(forward, strafe, moveSpeed);
    }

    private void doVanilla(double forward, double strafe, double maxGround) {
        // vanilla-like speed: very conservative, simply adjusts sprint speed
        if (mc.player.isOnGround()) {
            groundTicks++;

            if (vanillaJump && groundTicks >= 3 + random.nextInt(3)) {
                // periodic jumps for speed, with randomized interval
                mc.player.jump();
                groundTicks = 0;
            }

            // gentle speed boost that stays within vanilla tolerance
            double target = baseSpeed * vanillaSpeedMultiplier;
            target = Math.min(target, maxGround * 0.9); // stay well under limit
            moveSpeed = applyGradualAcceleration(target);
        } else {
            groundTicks = 0;
            // standard air friction
            moveSpeed *= 0.99;
            moveSpeed = Math.max(moveSpeed, baseSpeed);
        }

        applyStrafe(forward, strafe, moveSpeed);
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

    @Override
    protected void onDisable() {
        moveSpeed = baseSpeed;
        stage = 0;
        currentAccelSpeed = 0.0;
        groundTicks = 0;
        violations = 0;
    }

    public void setMode(Mode mode) { this.mode = mode; }
    public Mode getMode() { return mode; }
    public void setGradualAcceleration(boolean val) { this.gradualAcceleration = val; }
    public void setAccelerationRate(double rate) { this.accelerationRate = rate; }
    public void setNcpSafe(boolean val) { this.ncpSafe = val; }
    public void setPotionCheck(boolean val) { this.potionCheck = val; }
}
