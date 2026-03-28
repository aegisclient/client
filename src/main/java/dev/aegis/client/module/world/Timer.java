package dev.aegis.client.module.world;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Timer extends Module {

    private float timerSpeed = 2.0f;

    public Timer() {
        super("Timer", "Changes the game tick speed for faster movement", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // timer speed applied through mixin on RenderTickCounter
    }

    public float getTimerSpeed() {
        return timerSpeed;
    }

    public void setTimerSpeed(float speed) {
        this.timerSpeed = speed;
    }

    /**
     * Returns whether Timer is active. Called from mixins.
     */
    public static boolean isActive() {
        try {
            Module mod = Aegis.getInstance().getModuleManager().getModule("Timer");
            return mod != null && mod.isEnabled();
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Returns the timer speed multiplier. Called from mixins.
     */
    public static float getActiveSpeed() {
        try {
            Module mod = Aegis.getInstance().getModuleManager().getModule("Timer");
            if (mod != null && mod.isEnabled() && mod instanceof Timer timer) {
                return timer.timerSpeed;
            }
        } catch (Exception ignored) {}
        return 1.0f;
    }
}
