package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class SafeWalk extends Module {

    public SafeWalk() {
        super("SafeWalk", "Prevents walking off edges, like sneaking without the sneak", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // handled via mixin on clipAtLedge - this just acts as toggle
        // the mixin checks if this module is enabled and returns true for clipAtLedge
    }
}
