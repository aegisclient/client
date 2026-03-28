package dev.aegis.client.module.premium;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class HypixelKillAura extends Module {

    // Watchdog-safe parameters
    private static final double REACH = 3.0;           // Watchdog max reach
    private static final double WALL_REACH = 2.8;       // shorter through walls
    private static final float ROTATION_SPEED = 40.0f;  // smooth rotation speed
    private static final float ROTATION_JITTER = 2.0f;  // humanization

    // attack delay randomization
    private static final int MIN_ATTACK_DELAY = 0;
    private static final int MAX_ATTACK_DELAY = 3;
    private static final double MISS_CHANCE = 0.05; // 5% miss rate

    // targeting
    private boolean targetPlayers = true;
    private boolean targetHostile = false;

    // rotation smoothing state
    private float lastServerYaw;
    private float lastServerPitch;
    private boolean rotationActive = false;
    private int rotationKeepTicks = 0;

    // attack state
    private int attackCooldown = 0;
    private int tickCounter = 0;
    private LivingEntity primaryTarget = null;

    // anti-flag
    private int consecutiveHits = 0;
    private int pauseTicks = 0;

    private final Random random = new Random();

    public HypixelKillAura() {
        super("HypixelKillAura", "Watchdog-safe killaura with 3.0 reach, rotation smoothing, and attack delay randomization", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        attackCooldown = 0;
        tickCounter = 0;
        primaryTarget = null;
        rotationActive = false;
        rotationKeepTicks = 0;
        consecutiveHits = 0;
        pauseTicks = 0;
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

        // anti-flag pause after too many consecutive hits
        if (pauseTicks > 0) {
            pauseTicks--;
            return;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        // Watchdog checks attack cooldown progress
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.9f) return;

        List<LivingEntity> targets = findTargets();
        if (targets.isEmpty()) {
            primaryTarget = null;
            consecutiveHits = 0;
            return;
        }

        primaryTarget = targets.get(0);

        // auto weapon switch
        switchToBestWeapon();

        // miss simulation
        if (random.nextDouble() < MISS_CHANCE) {
            // simulate a miss by just rotating but not attacking
            float[] angles = calculateRotation(primaryTarget);
            sendSmoothRotation(angles[0], angles[1]);
            attackCooldown = 1 + random.nextInt(2);
            return;
        }

        // rotate and attack
        float[] angles = calculateRotation(primaryTarget);
        sendSmoothRotation(angles[0], angles[1]);

        // sprint reset for extra knockback
        if (mc.player.isSprinting()) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                    mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING
            ));
            mc.interactionManager.attackEntity(mc.player, primaryTarget);
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                    mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING
            ));
        } else {
            mc.interactionManager.attackEntity(mc.player, primaryTarget);
        }

        mc.player.swingHand(Hand.MAIN_HAND);

        // track consecutive hits for anti-flag
        consecutiveHits++;
        if (consecutiveHits >= 8 + random.nextInt(4)) {
            // pause briefly after many hits to avoid combat-frequency flags
            pauseTicks = 3 + random.nextInt(3);
            consecutiveHits = 0;
        }

        // randomized attack delay
        attackCooldown = MIN_ATTACK_DELAY + random.nextInt(MAX_ATTACK_DELAY - MIN_ATTACK_DELAY + 1);

        // hold rotation after attack
        rotationKeepTicks = 3 + random.nextInt(3);
    }

    private void sendSmoothRotation(float targetYaw, float targetPitch) {
        // smooth interpolation instead of snapping
        float smoothYaw = smoothRotate(lastServerYaw, targetYaw, ROTATION_SPEED + random.nextFloat() * 10);
        float smoothPitch = smoothRotate(lastServerPitch, targetPitch, ROTATION_SPEED + random.nextFloat() * 5);

        // add micro-jitter for humanization
        smoothYaw += (random.nextFloat() - 0.5f) * ROTATION_JITTER;
        smoothPitch += (random.nextFloat() - 0.5f) * (ROTATION_JITTER * 0.6f);

        smoothPitch = MathHelper.clamp(smoothPitch, -90f, 90f);

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                smoothYaw, smoothPitch, mc.player.isOnGround()
        ));

        lastServerYaw = smoothYaw;
        lastServerPitch = smoothPitch;
        rotationActive = true;
    }

    private float smoothRotate(float current, float target, float speed) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (Math.abs(diff) <= speed) return target;
        return current + Math.signum(diff) * speed;
    }

    private List<LivingEntity> findTargets() {
        List<LivingEntity> targets = new ArrayList<>();

        Box searchBox = mc.player.getBoundingBox().expand(REACH);
        mc.world.getOtherEntities(mc.player, searchBox).stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(LivingEntity::isAlive)
                .filter(e -> !(e instanceof ArmorStandEntity))
                .filter(this::isValidTarget)
                .filter(e -> {
                    double dist = mc.player.distanceTo(e);
                    if (!mc.player.canSee(e)) {
                        return dist <= WALL_REACH;
                    }
                    return dist <= REACH;
                })
                .sorted(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
                .forEach(targets::add);

        return targets;
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            if (!targetPlayers) return false;
            if (Aegis.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) return false;
            if (mc.player.isTeammate(player)) return false;
            return true;
        }
        return false; // Hypixel-specific = players only by default
    }

    private float[] calculateRotation(Entity target) {
        double targetY = target.getY() + target.getHeight() * (0.4 + random.nextDouble() * 0.3);

        double dx = target.getX() - mc.player.getX();
        double dy = targetY - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double dz = target.getZ() - mc.player.getZ();

        // slight prediction
        if (target instanceof LivingEntity living) {
            Vec3d velocity = living.getVelocity();
            double prediction = 0.8 + random.nextDouble() * 0.6;
            dx += velocity.x * prediction;
            dz += velocity.z * prediction;
        }

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        yaw += (random.nextFloat() - 0.5f) * 1.5f;
        pitch += (random.nextFloat() - 0.5f) * 0.8f;

        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f)};
    }

    private void switchToBestWeapon() {
        int bestSlot = -1;
        double bestDamage = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) {
                double damage = getItemAttackDamage(stack);
                if (stack.getItem() instanceof SwordItem) damage += 0.5;
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private double getItemAttackDamage(ItemStack stack) {
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        var damageModifiers = modifiers.get(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double damage = 1.0;
        for (var modifier : damageModifiers) {
            damage += modifier.getValue();
        }
        return damage;
    }

    public LivingEntity getTarget() { return primaryTarget; }
}
