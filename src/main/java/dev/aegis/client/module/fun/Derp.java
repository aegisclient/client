package dev.aegis.client.module.fun;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class Derp extends Module {

    private final Random random = new Random();

    public Derp() {
        super("Derp", "Spins your head around randomly (visible to others)", Category.FUN, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        mc.player.setYaw(random.nextFloat() * 360 - 180);
        mc.player.setPitch(random.nextFloat() * 180 - 90);
    }
}
