package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KillAura extends Module {

    private double range = 4.5;
    private boolean targetPlayers = true;
    private boolean targetHostile = true;
    private boolean targetAnimals = false;
    private int attackCooldown = 0;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        // only attack when attack cooldown is ready
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        // rotate towards target smoothly
        lookAt(target);

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        attackCooldown = 2; // small delay between attacks
    }

    private LivingEntity findTarget() {
        List<LivingEntity> targets = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> e != mc.player)
                .filter(e -> e.isAlive())
                .filter(e -> mc.player.distanceTo(e) <= range)
                .filter(this::isValidTarget)
                .sorted(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
                .collect(Collectors.toList());

        return targets.isEmpty() ? null : targets.get(0);
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity instanceof PlayerEntity && targetPlayers) return true;
        if (entity instanceof HostileEntity && targetHostile) return true;
        if (entity instanceof AnimalEntity && targetAnimals) return true;
        return false;
    }

    private void lookAt(Entity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + target.getStandingEyeHeight()) - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(smoothRotate(mc.player.getYaw(), yaw, 60));
        mc.player.setPitch(smoothRotate(mc.player.getPitch(), pitch, 60));
    }

    private float smoothRotate(float current, float target, float speed) {
        float diff = target - current;
        // normalize angle diff
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;

        if (Math.abs(diff) <= speed) return target;
        return current + (diff > 0 ? speed : -speed);
    }

    public void setRange(double range) { this.range = range; }
    public void setTargetPlayers(boolean val) { this.targetPlayers = val; }
    public void setTargetHostile(boolean val) { this.targetHostile = val; }
    public void setTargetAnimals(boolean val) { this.targetAnimals = val; }
}
