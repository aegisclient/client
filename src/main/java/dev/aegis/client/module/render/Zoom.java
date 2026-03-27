package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {

    private double zoomFactor = 3.0;
    private double currentFov = 0;
    private double savedFov = 0;

    public Zoom() {
        super("Zoom", "Zooms in like a spyglass or OptiFine zoom", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.options != null) {
            savedFov = mc.options.getFov().getValue();
        }
    }

    @Override
    public void onTick() {
        if (mc.options == null) return;

        double targetFov = savedFov / zoomFactor;
        currentFov = currentFov + (targetFov - currentFov) * 0.5; // smooth zoom
        mc.options.getFov().setValue((int) currentFov);
    }

    @Override
    protected void onDisable() {
        if (mc.options != null && savedFov > 0) {
            mc.options.getFov().setValue((int) savedFov);
        }
    }

    public double getZoomFactor() { return zoomFactor; }
}
