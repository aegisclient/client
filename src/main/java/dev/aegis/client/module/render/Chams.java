package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class Chams extends Module {

    private boolean playersOnly = false;

    public Chams() {
        super("Chams", "Makes entities visible through walls with colored overlay", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;

        // apply glowing effect to entities so they render through walls
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;

            if (playersOnly && !(entity instanceof PlayerEntity)) continue;

            living.setGlowing(true);
        }
    }

    @Override
    protected void onDisable() {
        if (mc.world == null) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity living) {
                living.setGlowing(false);
            }
        }
    }
}
