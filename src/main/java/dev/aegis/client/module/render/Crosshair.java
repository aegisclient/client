package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Crosshair extends Module {

    private int gapSize = 4;
    private int lineLength = 6;
    private float lineWidth = 2.0f;
    private boolean dot = true;
    private boolean dynamic = true;

    public Crosshair() {
        super("Crosshair", "Custom crosshair with configurable style", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // crosshair rendering handled in HUD overlay
    }

    public int getGapSize() { return gapSize; }
    public int getLineLength() { return lineLength; }
    public float getLineWidth() { return lineWidth; }
    public boolean hasDot() { return dot; }
    public boolean isDynamic() { return dynamic; }
}
