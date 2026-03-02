package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class Tracers extends Module {

    private boolean tracePlayers = true;
    private boolean traceHostile = true;

    public Tracers() {
        super("Tracers", "Draws lines to nearby entities", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // rendering is done via HudOverlay using the entity positions
        // this module just tracks the toggle state
    }

    public boolean shouldTrace(Entity entity) {
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (entity instanceof PlayerEntity && tracePlayers) return true;
        return traceHostile;
    }
}
