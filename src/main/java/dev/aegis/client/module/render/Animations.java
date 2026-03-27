package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Animations extends Module {

    private String swingMode = "1.7";
    private double swingSpeed = 1.0;
    private boolean oldSwordBlock = true;

    public Animations() {
        super("Animations", "Customizes hand and item animations (1.7 style, etc.)", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // animation modifications handled via mixins on HeldItemRenderer
    }

    public String getSwingMode() { return swingMode; }
    public double getSwingSpeed() { return swingSpeed; }
    public boolean isOldSwordBlock() { return oldSwordBlock; }
}
