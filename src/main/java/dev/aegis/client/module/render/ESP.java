package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class ESP extends Module {

    private boolean showPlayers = true;
    private boolean showHostile = true;
    private boolean showAnimals = true;

    public ESP() {
        super("ESP", "Highlights entities through walls with glowing effect", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity)) continue;

            boolean shouldGlow = false;
            if (entity instanceof PlayerEntity && showPlayers) shouldGlow = true;
            if (entity instanceof HostileEntity && showHostile) shouldGlow = true;
            if (entity instanceof AnimalEntity && showAnimals) shouldGlow = true;

            entity.setGlowing(shouldGlow);
        }
    }

    @Override
    protected void onDisable() {
        // remove glow from all entities
        if (mc.world == null) return;
        for (Entity entity : mc.world.getEntities()) {
            entity.setGlowing(false);
        }
    }
}
