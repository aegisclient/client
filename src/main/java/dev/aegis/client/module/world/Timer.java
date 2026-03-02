package dev.aegis.client.module.world;

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
        // timer speed is applied through the render loop tick delta
        // this functions as a simple multiplier on all game actions
    }

    public float getTimerSpeed() {
        return timerSpeed;
    }

    public void setTimerSpeed(float speed) {
        this.timerSpeed = speed;
    }

    public static boolean isActive() {
        return false; // placeholder
    }
}
