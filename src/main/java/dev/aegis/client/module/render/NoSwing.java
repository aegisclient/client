package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoSwing extends Module {

    public NoSwing() {
        super("NoSwing", "Hides the hand swing animation", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.handSwinging = false;
    }
}
