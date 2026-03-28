package dev.aegis.client.module.combat;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class KillAura extends Module {

    // targeting
    private double range = 4.5;
    private double wallRange = 3.5;
    private boolean targetPlayers = true;
    private boolean targetHostile = true;
    private boolean targetAnimals = false;
    private boolean targetInvisible = true;
    private boolean ignoreTeams = true;
    private boolean throughWallCheck = true;

    // timing
    private int attackCooldown = 0;
    private int maxTargets = 1;
    private boolean autoWeaponSwitch = true;

    // attack delay randomization (jitter in ticks, ~50ms per tick)
    private int minAttackDelay = 0;
    private int maxAttackDelay = 3; // 0-3 ticks = ~0-150ms jitter window
    private long lastAttackTimeMs = 0;
    private int attackJitterMs = 50; // +/-50ms jitter on top of tick delay

    // rotation smoothing
    private boolean serverRotations = true;
    private boolean smoothRotation = true;
    private float rotationSmoothSpeed = 60.0f; // degrees per tick base
    private float rotationSmoothJitter = 25.0f; // random variation in smooth speed
    private float currentYaw;
    private float currentPitch;
    private float targetYaw;
    private float targetPitch;
    private float lastServerYaw;
    private float lastServerPitch;
    private boolean rotationActive = false;
    private int rotationKeepTicks = 0;

    // priority targeting mode
    public enum TargetPriority { DISTANCE, HEALTH, THREAT, ANGLE }
    private TargetPriority targetPriority = TargetPriority.HEALTH;

    // anti-cheat
    private boolean failRate = true;
    private double failChance = 0.03; // 3% miss rate to look human
    private boolean randomizeOrder = true;
    private boolean sprintReset = true;
    private boolean rayCastCheck = true; // verify line of sight with raycast

    private final Random random = new Random();
    private LivingEntity primaryTarget = null;
    private int tickCounter = 0;

    public KillAura() {
        super("KillAura", "Auto-attack with smooth rotation and randomized timing", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        attackCooldown = 0;
        rotationActive = false;
        primaryTarget = null;
        tickCounter = 0;
        lastAttackTimeMs = 0;
        if (mc.player != null) {
            currentYaw = mc.player.getYaw();
            currentPitch = mc.player.getPitch();
        }
    }

    @Override
    protected void onDisable() {
        primaryTarget = null;
        rotationActive = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        tickCounter++;

        // decay rotation hold
        if (rotationKeepTicks > 0) rotationKeepTicks--;
        if (rotationKeepTicks == 0) rotationActive = false;

        if (attackCooldown > 0) {
            attackCooldown--;

            // even while on cooldown, smoothly rotate toward target
            if (smoothRotation && primaryTarget != null && primaryTarget.isAlive()) {
                float[] angles = calculateRotation(primaryTarget);
                smoothStepRotation(angles[0], angles[1]);
                if (serverRotations && rotationActive) {
                    sendRotationPacket(currentYaw, currentPitch);
                }
            }
            return;
        }

        // wait for attack cooldown to be ready (NCP checks this)
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.9f) return;

        // additional ms-level jitter check
        long now = System.currentTimeMillis();
        if (lastAttackTimeMs > 0) {
            long elapsed = now - lastAttackTimeMs;
            // enforce minimum interval with jitter: base 50ms * 10 ticks + random jitter
            int jitteredMinInterval = 45 + random.nextInt(attackJitterMs * 2 + 1) - attackJitterMs;
            if (elapsed < Math.max(jitteredMinInterval, 0)) return;
        }

        List<LivingEntity> targets = findTargets();
        if (targets.isEmpty()) {
            primaryTarget = null;
            return;
        }

        primaryTarget = targets.get(0);

        // auto weapon switch - pick best weapon
        if (autoWeaponSwitch) {
            switchToBestWeapon();
        }

        // attack up to maxTargets
        int attacked = 0;
        for (LivingEntity target : targets) {
            if (attacked >= maxTargets) break;

            // human-like fail rate
            if (failRate && random.nextDouble() < failChance) continue;

            // through-wall raycast verification
            if (throughWallCheck && rayCastCheck && !canActuallyHit(target)) continue;

            // rotate to target
            float[] angles = calculateRotation(target);

            if (serverRotations) {
                if (smoothRotation) {
                    // gradually step toward target rotation
                    smoothStepRotation(angles[0], angles[1]);
                    // only attack when rotation is close enough
                    float yawDiff = Math.abs(MathHelper.wrapDegrees(currentYaw - angles[0]));
                    float pitchDiff = Math.abs(currentPitch - angles[1]);
                    if (yawDiff > 15.0f || pitchDiff > 12.0f) {
                        // still rotating, don't attack yet but send rotation packet
                        sendRotationPacket(currentYaw, currentPitch);
                        continue;
                    }
                    sendRotationPacket(currentYaw, currentPitch);
                } else {
                    // send rotation packet without actually changing client view
                    sendRotationPacket(angles[0], angles[1]);
                }
            } else {
                // smooth client-side rotation
                float rotSpeed = 45 + random.nextFloat() * 35;
                mc.player.setYaw(smoothRotate(mc.player.getYaw(), angles[0], rotSpeed));
                mc.player.setPitch(smoothRotate(mc.player.getPitch(), angles[1], rotSpeed));
            }

            // trigger criticals module if enabled
            Module critMod = dev.aegis.client.Aegis.getInstance().getModuleManager().getModule("Criticals");
            if (critMod != null && critMod.isEnabled() && critMod instanceof Criticals crit) {
                crit.doCrit();
            }

            // sprint reset for extra knockback
            if (sprintReset && mc.player.isSprinting()) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            } else {
                mc.interactionManager.attackEntity(mc.player, target);
            }

            mc.player.swingHand(Hand.MAIN_HAND);
            attacked++;
        }

        if (attacked > 0) {
            lastAttackTimeMs = now;
        }

        // randomized delay with jitter - varies between min and max extra ticks
        attackCooldown = minAttackDelay + random.nextInt(maxAttackDelay - minAttackDelay + 1);

        // keep rotation for a few ticks after attacking
        rotationKeepTicks = 3 + random.nextInt(3);
    }

    private boolean canActuallyHit(Entity target) {
        // perform a proper raycast from player eyes to target body
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.5, 0);

        HitResult result = mc.world.raycast(new RaycastContext(
                eyePos, targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        // if the raycast hits a block before reaching the target, we can't hit through the wall
        if (result.getType() == HitResult.Type.BLOCK) {
            double blockDist = result.getPos().squaredDistanceTo(eyePos);
            double targetDist = targetPos.squaredDistanceTo(eyePos);
            return blockDist >= targetDist * 0.9; // small tolerance
        }
        return true;
    }

    private void smoothStepRotation(float targetYaw, float targetPitch) {
        // gradual yaw/pitch changes instead of snapping
        float speed = rotationSmoothSpeed + (random.nextFloat() - 0.5f) * rotationSmoothJitter;
        speed = Math.max(speed, 10.0f); // minimum speed so we don't get stuck

        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        // ease-in-out: move faster when far away, slower when close
        float yawStep = yawDiff * 0.4f;
        float pitchStep = pitchDiff * 0.4f;

        // clamp to max speed
        yawStep = MathHelper.clamp(yawStep, -speed, speed);
        pitchStep = MathHelper.clamp(pitchStep, -speed * 0.7f, speed * 0.7f);

        // add micro-jitter during rotation for humanization
        yawStep += (random.nextFloat() - 0.5f) * 0.8f;
        pitchStep += (random.nextFloat() - 0.5f) * 0.4f;

        currentYaw = MathHelper.wrapDegrees(currentYaw + yawStep);
        currentPitch = MathHelper.clamp(currentPitch + pitchStep, -90f, 90f);

        this.targetYaw = targetYaw;
        this.targetPitch = targetPitch;
    }

    private List<LivingEntity> findTargets() {
        List<LivingEntity> targets = new ArrayList<>();

        Box searchBox = mc.player.getBoundingBox().expand(range);
        mc.world.getOtherEntities(mc.player, searchBox).stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(LivingEntity::isAlive)
                .filter(e -> !(e instanceof ArmorStandEntity))
                .filter(this::isValidTarget)
                .filter(e -> {
                    double dist = mc.player.distanceTo(e);
                    if (throughWallCheck && !mc.player.canSee(e)) {
                        return dist <= wallRange;
                    }
                    return dist <= range;
                })
                .sorted(getTargetComparator())
                .forEach(targets::add);

        return targets;
    }

    private Comparator<LivingEntity> getTargetComparator() {
        return (a, b) -> {
            // players always priority over mobs
            boolean aPlayer = a instanceof PlayerEntity;
            boolean bPlayer = b instanceof PlayerEntity;
            if (aPlayer && !bPlayer) return -1;
            if (!aPlayer && bPlayer) return 1;

            switch (targetPriority) {
                case HEALTH -> {
                    // lowest health first (finish them off)
                    int hpComp = Float.compare(a.getHealth(), b.getHealth());
                    if (hpComp != 0) return hpComp;
                    return Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b));
                }
                case DISTANCE -> {
                    return Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b));
                }
                case THREAT -> {
                    // threat = damage potential / distance - prioritize close, healthy enemies
                    float aThreat = a.getHealth() / (float) Math.max(mc.player.distanceTo(a), 0.5);
                    float bThreat = b.getHealth() / (float) Math.max(mc.player.distanceTo(b), 0.5);
                    // higher threat first
                    int threatComp = Float.compare(bThreat, aThreat);
                    if (threatComp != 0) return threatComp;
                    return Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b));
                }
                case ANGLE -> {
                    // prefer targets closest to current look direction (less rotation needed)
                    float[] aAngles = calculateRotation(a);
                    float[] bAngles = calculateRotation(b);
                    float aYawDiff = Math.abs(MathHelper.wrapDegrees(aAngles[0] - mc.player.getYaw()));
                    float bYawDiff = Math.abs(MathHelper.wrapDegrees(bAngles[0] - mc.player.getYaw()));
                    int angleComp = Float.compare(aYawDiff, bYawDiff);
                    if (angleComp != 0) return angleComp;
                    return Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b));
                }
                default -> {
                    return Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b));
                }
            }
        };
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            if (!targetPlayers) return false;
            // friend check
            if (Aegis.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) return false;
            // team check
            if (!ignoreTeams && mc.player.isTeammate(player)) return false;
            // invisible check
            if (!targetInvisible && player.isInvisible()) return false;
            // dead/dying check
            if (player.isDead() || player.getHealth() <= 0) return false;
            return true;
        }
        if (entity instanceof HostileEntity && targetHostile) return true;
        if (entity instanceof AnimalEntity && targetAnimals) return true;
        return false;
    }

    private float[] calculateRotation(Entity target) {
        // aim at body center with slight random offset for human-like targeting
        double targetY = target.getY() + target.getHeight() * (0.4 + random.nextDouble() * 0.3);

        double dx = target.getX() - mc.player.getX();
        double dy = targetY - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double dz = target.getZ() - mc.player.getZ();

        // predict target movement slightly (1-2 ticks ahead)
        if (target instanceof LivingEntity living) {
            Vec3d velocity = living.getVelocity();
            double prediction = 1.0 + random.nextDouble();
            dx += velocity.x * prediction;
            dz += velocity.z * prediction;
        }

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        // add micro-jitter for humanization
        yaw += (random.nextFloat() - 0.5f) * 1.5f;
        pitch += (random.nextFloat() - 0.5f) * 0.8f;

        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f)};
    }

    private void sendRotationPacket(float yaw, float pitch) {
        // server-side rotation - sends rotation without changing client view
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                yaw, pitch, mc.player.isOnGround()
        ));
        lastServerYaw = yaw;
        lastServerPitch = pitch;
        rotationActive = true;
    }

    private void switchToBestWeapon() {
        int bestSlot = -1;
        double bestDamage = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            double damage = 0;
            // prefer swords, then axes
            if (stack.getItem() instanceof SwordItem) {
                damage = getItemAttackDamage(stack) + 0.5; // sword priority bonus
            } else if (stack.getItem() instanceof AxeItem) {
                damage = getItemAttackDamage(stack);
            }
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlot = i;
            }
        }

        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private double getItemAttackDamage(ItemStack stack) {
        var modifiers = stack.getAttributeModifiers(net.minecraft.entity.EquipmentSlot.MAINHAND);
        var damageModifiers = modifiers.get(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double damage = 1.0;
        for (var modifier : damageModifiers) {
            damage += modifier.getValue();
        }
        return damage;
    }

    private float smoothRotate(float current, float target, float speed) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (Math.abs(diff) <= speed) return target;
        return current + Math.signum(diff) * speed;
    }

    public LivingEntity getTarget() { return primaryTarget; }
    public boolean isRotationActive() { return rotationActive; }
    public float getServerYaw() { return lastServerYaw; }
    public float getServerPitch() { return lastServerPitch; }

    public void setRange(double range) { this.range = range; }
    public void setTargetPlayers(boolean val) { this.targetPlayers = val; }
    public void setTargetHostile(boolean val) { this.targetHostile = val; }
    public void setTargetAnimals(boolean val) { this.targetAnimals = val; }
    public void setTargetPriority(TargetPriority priority) { this.targetPriority = priority; }
    public void setThroughWallCheck(boolean val) { this.throughWallCheck = val; }
    public void setSmoothRotation(boolean val) { this.smoothRotation = val; }
    public void setRotationSmoothSpeed(float speed) { this.rotationSmoothSpeed = speed; }
    public void setAttackJitterMs(int jitter) { this.attackJitterMs = jitter; }
}
