package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Reach extends Module {

    private float reachDistance = 6.0f;

    public Reach() {
        super("Reach", "Extends your attack and interaction reach", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // reach is applied through MixinClientPlayerInteractionManager
        // this module just needs to be togglable
    }

    public float getReachDistance() {
        return reachDistance;
    }

    public void setReachDistance(float dist) {
        this.reachDistance = dist;
    }

    public static float getActiveReach() {
        // called from mixins and other places
        return 6.0f;
    }
}
