package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoSlow extends Module {

    public NoSlow() {
        super("NoSlow", "Prevents slowdown from using items, soul sand, honey, and webs", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // slowdown prevention handled via mixin
        // this module just acts as a toggle flag
    }

    public static boolean isActive() {
        return true;
    }
}
