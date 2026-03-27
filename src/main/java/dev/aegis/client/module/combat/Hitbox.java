package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Hitbox extends Module {

    private double expand = 0.4;

    public Hitbox() {
        super("Hitbox", "Expands entity hitboxes to make them easier to hit", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // hitbox expansion handled via mixin on EntityRenderer
    }

    public double getExpand() {
        return expand;
    }

    public static boolean shouldExpand() {
        return true;
    }
}
