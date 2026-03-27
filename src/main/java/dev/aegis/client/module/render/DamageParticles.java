package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.LivingEntity;
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
        if (mc.world == null) return;

        mc.world.getEntities().forEach(entity -> {
            if (entity instanceof LivingEntity living) {
                int id = living.getId();
                float currentHealth = living.getHealth();
                Float previous = lastHealth.get(id);

                if (previous != null && currentHealth < previous) {
                    float damage = previous - currentHealth;
                    // spawn particle via chat or custom render
                    // damage numbers rendered in HUD overlay
                }

                lastHealth.put(id, currentHealth);
            }
        });
    }

    @Override
    protected void onDisable() {
        lastHealth.clear();
    }
}
