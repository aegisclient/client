package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoHurtCam extends Module {

    public NoHurtCam() {
        super("NoHurtCam", "Removes screen shake when taking damage", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // handled via mixin on GameRenderer.tiltViewWhenHurt
        // this module acts as a toggle flag
    }
}
