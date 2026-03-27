package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class FastBreak extends Module {

    private double speedMultiplier = 1.4;

    public FastBreak() {
        super("FastBreak", "Increases block breaking speed", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // breaking speed modification handled via mixin on PlayerEntity.getBlockBreakingSpeed
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
}
