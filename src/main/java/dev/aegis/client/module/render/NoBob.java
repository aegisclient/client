package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoBob extends Module {

    public NoBob() {
        super("NoBob", "Disables view bobbing while walking", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.options == null) return;
        mc.options.getBobView().setValue(false);
    }

    @Override
    protected void onDisable() {
        if (mc.options != null) {
            mc.options.getBobView().setValue(true);
        }
    }
}
