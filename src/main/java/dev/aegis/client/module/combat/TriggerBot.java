package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class TriggerBot extends Module {

    public TriggerBot() {
        super("TriggerBot", "Automatically attacks the entity you're looking at", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // wait for attack cooldown
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) mc.crosshairTarget;
            if (entityHit.getEntity() instanceof LivingEntity target) {
                if (target.isAlive() && isValidTarget(target)) {
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return true;
        if (entity instanceof Monster) return true;
        return false;
    }
}
