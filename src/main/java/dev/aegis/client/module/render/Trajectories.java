package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Trajectories extends Module {

    public Trajectories() {
        super("Trajectories", "Shows the predicted path of thrown projectiles", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // rendering handled in HUD overlay using projectile physics simulation
    }
}
