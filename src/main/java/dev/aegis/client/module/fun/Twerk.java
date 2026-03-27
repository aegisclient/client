package dev.aegis.client.module.fun;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Twerk extends Module {

    private int tickCounter = 0;

    public Twerk() {
        super("Twerk", "Rapidly toggles sneak for the twerking animation", Category.FUN, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        tickCounter++;
        mc.player.setSneaking(tickCounter % 4 < 2);
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.setSneaking(false);
        }
        tickCounter = 0;
    }
}
