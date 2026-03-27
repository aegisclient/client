package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Radar extends Module {

    private int size = 80;
    private double range = 50.0;

    public Radar() {
        super("Radar", "Shows a minimap radar with nearby entities", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // radar rendering handled in HUD overlay
    }

    public int getSize() { return size; }
    public double getRange() { return range; }
}
