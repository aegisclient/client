package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class TNTTimer extends Module {

    public TNTTimer() {
        super("TNTTimer", "Shows time until TNT entities explode", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // TNT fuse rendering handled in world render overlay
    }
}
