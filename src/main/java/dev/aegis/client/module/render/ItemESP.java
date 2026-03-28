package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

public class ItemESP extends Module {

    public ItemESP() {
        super("ItemESP", "Highlights dropped items on the ground", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        Box searchBox = mc.player.getBoundingBox().expand(128);
        for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox)) {
            if (entity instanceof ItemEntity) {
                entity.setGlowing(true);
            }
        }
    }

    @Override
    protected void onDisable() {
        if (mc.world == null || mc.player == null) return;
        Box searchBox = mc.player.getBoundingBox().expand(128);
        for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox)) {
            if (entity instanceof ItemEntity) {
                entity.setGlowing(false);
            }
        }
    }
}
