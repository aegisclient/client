package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.ItemEntity;
import org.lwjgl.glfw.GLFW;

public class ItemESP extends Module {

    public ItemESP() {
        super("ItemESP", "Highlights dropped items on the ground", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;

        // apply glowing to all item entities
        mc.world.getEntities().forEach(entity -> {
            if (entity instanceof ItemEntity) {
                entity.setGlowing(true);
            }
        });
    }

    @Override
    protected void onDisable() {
        if (mc.world == null) return;
        mc.world.getEntities().forEach(entity -> {
            if (entity instanceof ItemEntity) {
                entity.setGlowing(false);
            }
        });
    }
}
