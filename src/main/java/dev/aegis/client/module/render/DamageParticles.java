package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class DamageParticles extends Module {

    private final Map<Integer, Float> lastHealth = new HashMap<>();

    public DamageParticles() {
        super("DamageParticles", "Shows floating damage numbers when entities take damage", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        Box searchBox = mc.player.getBoundingBox().expand(64);
        for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox)) {
            if (entity instanceof LivingEntity living) {
                int id = living.getId();
                float currentHealth = living.getHealth();
                Float previous = lastHealth.get(id);

                if (previous != null && currentHealth < previous) {
                    float damage = previous - currentHealth;
                    // damage numbers rendered in HUD overlay
                }

                lastHealth.put(id, currentHealth);
            }
        }
    }

    @Override
    protected void onDisable() {
        lastHealth.clear();
    }
}
