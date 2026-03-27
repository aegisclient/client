package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoFov extends Module {

    private int targetFov = 90;

    public NoFov() {
        super("NoFov", "Locks FOV to a fixed value, prevents speed/potion FOV changes", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.options == null) return;
        mc.options.getFov().setValue(targetFov);
        mc.options.getFovEffectScale().setValue(0.0);
    }

    @Override
    protected void onDisable() {
        if (mc.options != null) {
            mc.options.getFovEffectScale().setValue(1.0);
        }
    }
}
