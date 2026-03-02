package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Nametags extends Module {

    private boolean showHealth = true;
    private boolean showDistance = true;
    private float scale = 2.0f;

    public Nametags() {
        super("Nametags", "Enhanced nametags with health and distance info", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // nametag rendering is handled in the HUD overlay
    }

    public boolean isShowHealth() { return showHealth; }
    public boolean isShowDistance() { return showDistance; }
    public float getScale() { return scale; }
}
