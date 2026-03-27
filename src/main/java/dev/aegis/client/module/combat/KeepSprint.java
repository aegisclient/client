package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class KeepSprint extends Module {

    public KeepSprint() {
        super("KeepSprint", "Prevents sprint from resetting when attacking", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.input.movementForward > 0) {
            mc.player.setSprinting(true);
        }
    }
}
