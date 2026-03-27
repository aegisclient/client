package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoSlowBreak extends Module {

    public NoSlowBreak() {
        super("NoSlowBreak", "Prevents mining speed reduction from not being on ground or underwater", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // handled via mixin - overrides the onGround and submerged checks for mining speed
    }
}
