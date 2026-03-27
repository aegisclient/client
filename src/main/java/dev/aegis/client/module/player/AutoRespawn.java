package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.client.gui.screen.DeathScreen;
import org.lwjgl.glfw.GLFW;

public class AutoRespawn extends Module {

    public AutoRespawn() {
        super("AutoRespawn", "Automatically respawns when you die", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.currentScreen instanceof DeathScreen) {
            mc.player.requestRespawn();
            mc.setScreen(null);
        }
    }
}
