package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoWeather extends Module {

    public NoWeather() {
        super("NoWeather", "Removes rain and thunder effects", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;

        mc.world.setRainGradient(0.0f);
        mc.world.setThunderGradient(0.0f);
    }
}
