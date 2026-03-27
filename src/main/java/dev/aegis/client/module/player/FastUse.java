package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class FastUse extends Module {

    public FastUse() {
        super("FastUse", "Removes delay between using items (pearls, potions, etc.)", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // item use cooldown removal handled via mixin on MinecraftClient.itemUseCooldown
    }
}
