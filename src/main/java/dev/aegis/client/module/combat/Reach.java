package dev.aegis.client.module.combat;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Reach extends Module {

    private float reachDistance = 4.5f;

    public Reach() {
        super("Reach", "Extends your attack and interaction reach", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // reach modification applied through mixin or via getActiveReach() calls
    }

    public float getReachDistance() {
        return reachDistance;
    }

    public void setReachDistance(float dist) {
        this.reachDistance = dist;
    }

    /**
     * Returns the active reach distance. Called from mixins and other modules.
     * Returns vanilla reach (3.0) if module is not enabled.
     */
    public static float getActiveReach() {
        try {
            Module mod = Aegis.getInstance().getModuleManager().getModule("Reach");
            if (mod != null && mod.isEnabled() && mod instanceof Reach reach) {
                return reach.reachDistance;
            }
        } catch (Exception ignored) {}
        return 3.0f; // vanilla reach
    }
}
