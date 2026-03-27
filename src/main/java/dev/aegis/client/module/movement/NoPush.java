package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoPush extends Module {

    public NoPush() {
        super("NoPush", "Prevents being pushed by entities, water, and pistons", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // handled via mixin - zeroes push vector on pushAwayFrom
    }
}
