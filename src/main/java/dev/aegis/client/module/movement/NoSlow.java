package dev.aegis.client.module.movement;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoSlow extends Module {

    public NoSlow() {
        super("NoSlow", "Prevents slowdown from using items, soul sand, honey, and webs", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // item use slowdown prevention: when using items, maintain sprint speed
        if (mc.player == null) return;
        if (mc.player.isUsingItem()) {
            // keep sprint active while using items
            if (mc.player.input.movementForward > 0) {
                mc.player.setSprinting(true);
            }
        }
    }

    /**
     * Called from mixins to check if NoSlow should prevent movement penalty.
     */
    public static boolean isActive() {
        try {
            Module mod = Aegis.getInstance().getModuleManager().getModule("NoSlow");
            return mod != null && mod.isEnabled();
        } catch (Exception ignored) {}
        return false;
    }
}
