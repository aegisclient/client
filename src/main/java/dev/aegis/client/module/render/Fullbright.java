package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Fullbright extends Module {

    private double previousGamma = -1;

    public Fullbright() {
        super("Fullbright", "See perfectly in the dark", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.options != null) {
            previousGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(16.0);
        }
    }

    @Override
    public void onTick() {
        // keep gamma high in case something resets it
        if (mc.options != null && mc.options.getGamma().getValue() < 16.0) {
            mc.options.getGamma().setValue(16.0);
        }
    }

    @Override
    protected void onDisable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(previousGamma >= 0 ? previousGamma : 1.0);
        }
    }
}
